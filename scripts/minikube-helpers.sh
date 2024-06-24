#!/bin/bash
#
# Copyright (c) 2019, 2020 IBM Corporation and others.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

###############################  v MiniKube v #################################

function minikube_prereq() {
	echo
	echo "Info: Checking pre requisites for minikube..."
	kubectl_tool=$(which kubectl)
	check_err "Error: Please install the kubectl tool"
	# Check to see if kubectl supports kustomize
	kubectl kustomize --help >/dev/null 2>/dev/null
	check_err "Error: Please install a newer version of kubectl tool that supports the kustomize option (>=v1.12)"

	kruize_ns="monitoring"
	kubectl_cmd="kubectl -n ${kruize_ns}"

	if [ ${non_interactive} == 0 ]; then
		echo "Info: kruize needs cadvisor/prometheus/grafana to be installed in minikube"
		echo -n "Download and install these software to minikube(y/n)? "
		read inst
		linst=$(echo ${inst} | tr A-Z a-z)
		if [ ${linst} == "n" ]; then
			echo "Info: kruize not installed"
			exit 0
		fi
	fi

	mkdir minikube_downloads 2>/dev/null
	pushd minikube_downloads >/dev/null
		echo "Info: Downloading cadvisor git"
		git clone https://github.com/google/cadvisor.git 2>/dev/null
		pushd cadvisor/deploy/kubernetes/base >/dev/null
		echo
		echo "Info: Installing cadvisor"
		kubectl kustomize . | kubectl apply -f-
		check_err "Error: Unable to install cadvisor"
		popd >/dev/null
		echo
		echo "Info: Downloading prometheus git"
		git clone https://github.com/coreos/kube-prometheus.git 2>/dev/null
		pushd kube-prometheus/manifests >/dev/null
		echo
		echo "Info: Installing prometheus"
		kubectl apply -f setup
		check_err "Error: Unable to setup prometheus"
		kubectl apply -f .
		check_err "Error: Unable to install prometheus"
		popd >/dev/null
	popd >/dev/null

	echo -n "Info: Waiting for all Prometheus Pods to get spawned..."
	while true;
	do
		# Wait for prometheus docker images to get downloaded and spawn the main pod
		pod_started=$(${kubectl_cmd} get pods | grep "prometheus-k8s-1")
		if [ "${pod_started}" == "" ]; then
			# prometheus-k8s-1 not yet spawned
			echo -n "."
			sleep 5
		else
			echo "done"
			break;
		fi
	done
	check_running prometheus-k8s-1
	sleep 2
}

function minikube_first() {

	# Check if the service account already exists
	sa_exists=$(${kubectl_cmd} get sa | grep ${SA_NAME})
	if [ "${sa_exists}" != "" ]; then
		return;
	fi
	echo
	echo "Info: One time setup - Create a service account to deploy kruize"
	sed -e "s/{{ KRUIZE_NAMESPACE }}/${kruize_ns}/" ${SA_TEMPLATE} > ${SA_MANIFEST}
	${kubectl_cmd} apply -f ${SA_MANIFEST}
	check_err "Error: Failed to create service account and RBAC"
	${kubectl_cmd} apply -f ${SERVICE_MONITOR_MANIFEST}
	check_err "Error: Failed to create service monitor for Prometheus"
}

# Update yaml with the current ICP instance specific details
function minikube_setup() {
	pservice="prometheus-k8s"
	purl=""
	br_token=""

	sed -e "s|extensions/v1beta1|apps/v1|" ${DEPLOY_TEMPLATE} > ${DEPLOY_MANIFEST}
	sed -ie "s/replicas: 1/replicas: 1\n  selector:\n    matchLabels:\n      app: kruize/" ${DEPLOY_MANIFEST}
	sed -ie "s|{{ KRUIZE_DOCKER_IMAGE }}|${KRUIZE_DOCKER_IMAGE}|" ${DEPLOY_MANIFEST}
	sed -ie "s/{{ K8S_TYPE }}/Minikube/" ${DEPLOY_MANIFEST}
	sed -ie "s/{{ BEARER_AUTH_TOKEN }}/${br_token}/" ${DEPLOY_MANIFEST}
	sed -ie "s/{{ MONITORING_SERVICE }}/${pservice}/" ${DEPLOY_MANIFEST}
	sed -ie "s|{{ MONITORING_AGENT_ENDPOINT }}|${purl}|" ${DEPLOY_MANIFEST}
}

# You can deploy using kubectl
function minikube_deploy() {
	echo
	echo "Info: Deploying kruize yaml to minikube cluster"
	${kubectl_cmd} apply -f ${DEPLOY_MANIFEST}
	sleep 2
	check_running kruize
	if [ "${err}" == "0" ]; then
		grafana_pod=$(${kubectl_cmd} get pods | grep grafana | awk '{ print $1 }')
		echo "Info: Access grafana dashboard to see kruize recommendations at http://localhost:3000"
		echo "Info: Run the following command first to access grafana port"
		echo "      $ kubectl port-forward -n monitoring ${grafana_pod} 3000:3000"
		echo
	else
		# Indicate deploy failed on error
		exit 1
	fi
}

function minikube_start() {
	echo
	echo "###   Installing kruize for minikube"
	echo
	minikube_prereq
	minikube_first
	minikube_setup
	minikube_deploy
}

function minikube_terminate() {
	echo -n "###   Removing kruize for minikube"

	kruize_ns="monitoring"
	kubectl_cmd="kubectl -n ${kruize_ns}"

	echo
	echo "Removing kruize"
	${kubectl_cmd} delete -f ${DEPLOY_MANIFEST} 2>/dev/null

	echo
	echo "Removing kruize service account"
	${kubectl_cmd} delete -f ${SA_MANIFEST} 2>/dev/null

	echo
	echo "Removing kruize serviceMonitor"
	${kubectl_cmd} delete -f ${SERVICE_MONITOR_MANIFEST} 2>/dev/null

	pushd minikube_downloads > /dev/null
		echo
		echo "Removing cadvisor"
		pushd cadvisor/deploy/kubernetes/base > /dev/null
		kubectl kustomize . | kubectl delete -f-
		popd > /dev/null
		
		echo
		echo "Removing prometheus"
		pushd kube-prometheus/manifests > /dev/null
		kubectl delete -f . 2>/dev/null
		kubectl delete -f setup 2>/dev/null
		popd > /dev/null
	popd > /dev/null

	rm ${DEPLOY_MANIFEST}
	rm ${SA_MANIFEST}
	
	rm -rf minikube_downloads
}
