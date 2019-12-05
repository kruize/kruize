#!/bin/bash
#
# Copyright (c) 2019, 2019 IBM Corporation and others.
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
KRUIZE_VERSION=$(cat .kruize-version)

DEPLOY_TEMPLATE="manifests/kruize.yaml_template"
DEPLOY_MANIFEST="manifests/kruize.yaml"
SA_TEMPLATE="manifests/kruize-sa_rbac.yaml_template"
SA_MANIFEST="manifests/kruize-sa_rbac.yaml"
SA_NAME="kruize-sa"

DOCKER_MANIFEST="manifests/docker/kruize-docker.yaml"
DOCKER_TMP_JSON="kruize-docker-tmp.json"
DOCKER_JSON="kruize-docker.json"
PROMETHEUS_MANIFEST="manifests/docker/prometheus.yaml"
GRAFANA_MANIFESTS="manifests/docker/grafana/"

CADVISOR_DOCKER_IMAGE="google/cadvisor:latest"
PROMETHEUS_DOCKER_IMAGE="prom/prometheus:latest"
GRAFANA_DOCKER_IMAGE="grafana/grafana:latest"
KRUIZE_DOCKER_REPO="dinogun/kruize"
KRUIZE_DOCKER_IMAGE=${KRUIZE_DOCKER_REPO}:${KRUIZE_VERSION}

MINIKUBE_MANIFEST="manifests/minikube/kruize-service-monitor.yaml"

cluster_type="icp"
setup=1
non_interactive=0

# Determine namespace into which kruize will be deployed
# ICP       = kube-system namespace
# openshift = openshift-monitoring namespace
kruize_ns="kube-system"
user="admin"

function usage() {
	echo
	echo "Usage: $0 [-a] [-k url] [-c [docker|icp|minikube|openshift]] [-i docker-image] [-s|t] [-u user] [-p password] [-n namespace]"
	echo "       -s = start(default), -t = terminate"
	exit -1
}

# Check error code from last command, exit on error
check_err() {
	err=$?
	if [ ${err} -ne 0 ]; then
		echo "$*"
		exit -1
	fi
}

# Check if the cluster_type is one of icp or openshift
function check_cluster_type() {
	case "${cluster_type}" in
	docker|icp|minikube|openshift)
		;;
	*)
		echo "Error: unsupported cluster type: ${cluster_type}"
		exit -1
	esac
}

function check_running() {
	check_pod=$1

	echo "Info: Waiting for ${check_pod} to come up..."
	while true;
	do
		sleep 2
		${kubectl_cmd} get pods | grep ${check_pod}
		pod_stat=$(${kubectl_cmd} get pods | grep ${check_pod} | awk '{ print $3 }' | grep -v 'Terminating')
		case "${pod_stat}" in
			"ContainerCreating"|"Terminating"|"Pending")
				sleep 2
				;;
			"Running")
				echo "Info: ${check_pod} deploy succeeded: ${pod_stat}"
				err=0
				break;
				;;
			*)
				echo "Error: ${check_pod} deploy failed: ${pod_stat}"
				err=-1
				break;
				;;
		esac
	done

	${kubectl_cmd} get pods | grep ${check_pod}
	echo
}

################################  v Docker v ##################################

# Read the docker manifest and build a list of containers to be monitored
function get_all_monitored_containers() {
	all_monitored_containers=$(cat ${DOCKER_MANIFEST} | grep "name" | grep -v -P "^\s?#" | awk -F '"' '{ print $2 }')
	all_monitored_containers="${all_monitored_containers} $(cat ${DOCKER_MANIFEST} | grep "name" | grep -v -P "^\s?#" | grep -v '"' | awk -F ':' '{ print $2 }')"
	all_monitored_containers=$(echo ${all_monitored_containers} | sort | uniq)

	echo ${all_monitored_containers}
}

function create_dummy_json_file() {
	printf '{\n  "containers": [' > ${DOCKER_JSON}
	printf '     { "name": "kruize", "cpu_limit": "0", "mem_limit": "0" }' >> ${DOCKER_JSON}
	printf '  ]\n}\n' >> ${DOCKER_JSON}
}

function create_json_file() {
	printf '{\n  "containers": [' > ${DOCKER_TMP_JSON}
}

function close_json_file() {
	printf '\n  ]\n}' >> ${DOCKER_TMP_JSON}
	sed -i "$(sed -n '/name/ =' ${DOCKER_TMP_JSON} | tail -n 1)"' s/,$//' ${DOCKER_TMP_JSON}
}

function create_json_entry() {
	printf "\n    { \"name\": \"$1\", \"cpu_limit\": \"$2\", \"mem_limit\": \"$3\" }," >> ${DOCKER_TMP_JSON}
}

function get_container_info() {
	create_json_file
	for ctnr in $(get_all_monitored_containers)
	do
		docker inspect ${ctnr} >/dev/null 2>/dev/null
		if [ $? -ne 0 ]; then
			echo " ${ctnr}: not found running. Ignoring..."
			continue;
		else
			echo " ${ctnr}: found. Adding to list of containers to be monitored."
		fi
		# Get the container id from docker inspect
		cont_id=$(docker inspect ${ctnr} | grep '\"Id\":' | awk -F'"' '{ print $4 }')
		# Get quota and period
		cont_cpu_quota=$(docker inspect ${ctnr} | grep -P '"CpuQuota":' | awk '{ print $2 }' | awk -F',' '{ print $1 }')
		cont_cpu_period=$(docker inspect ${ctnr} | grep -P '"CpuPeriod":' | awk '{ print $2 }' | awk -F',' '{ print $1 }')
		cont_mem_limit=$(docker inspect ${ctnr} | grep -P '"Memory":' | awk '{ print $2 }' | awk -F',' '{ print $1 }')

		# Calculate the cpu_limit using the period and the quota
		# If the period is not set, assume a default period of 100000
		if [ ${cont_cpu_period} -eq 0 ]; then
			cont_cpu_limit=$(( ${cont_cpu_quota} / 100000 ))
		else
			cont_cpu_limit=$(( ${cont_cpu_quota} / ${cont_cpu_period} ))
		fi

		create_json_entry ${ctnr} ${cont_cpu_limit} ${cont_mem_limit}
	done
	close_json_file
	cp ${DOCKER_TMP_JSON} ${DOCKER_JSON}
}

#
function app_monitor_loop() {
	echo "########################     Starting App Monitor loop    #########################"
	echo "Kruize recommendations available on the grafana dashboard at: http://localhost:3000"
	echo "Info: Press CTRL-C to exit"
	while true
	do
		get_container_info
		sleep 10
	done
}

#
function docker_prereq() {
	echo
	echo "Info: Checking pre requisites for Docker..."

	docker pull ${CADVISOR_DOCKER_IMAGE} 2>/dev/null
	check_err "Error: Unable to pull prometheus docker image: ${CADVISOR_DOCKER_IMAGE}"

	docker pull ${PROMETHEUS_DOCKER_IMAGE} 2>/dev/null
	check_err "Error: Unable to pull prometheus docker image: ${PROMETHEUS_DOCKER_IMAGE}"

	docker pull ${GRAFANA_DOCKER_IMAGE} 2>/dev/null
	check_err "Error: Unable to pull grafana docker image: ${GRAFANA_DOCKER_IMAGE}"

	docker pull ${KRUIZE_DOCKER_IMAGE} 2>/dev/null
	if [ $? != 0 ]; then
		# Check if the image is available locally. Eg testing a image built locally
		DOCKER_REPO=$(echo ${KRUIZE_DOCKER_IMAGE} | awk -F":" '{ print $1 }')
		DOCKER_TAG=$(echo ${KRUIZE_DOCKER_IMAGE} | awk -F":" '{ print $2 }')
		if [ -z "${DOCKER_TAG}" ]; then
			DOCKER_TAG="latest"
		fi
		IMAGE_AVAIL=$(docker images | grep "${DOCKER_REPO}" | grep "${DOCKER_TAG}")
		if [ -z "${IMAGE_AVAIL}" ]; then
			echo "Error: Unable to locate kruize docker image: ${KRUIZE_DOCKER_IMAGE}"
		fi
	fi
}

#
function docker_setup() {
	echo "Starting cadvisor container"
	docker run -d --rm --name=cadvisor   -p 8000:8080   --net=host   --cpus=1   --volume=/:/rootfs:ro  --volume=/var/run:/var/run:ro   --volume=/sys:/sys:ro   --volume=/var/lib/docker/:/var/lib/docker:ro   --volume=/dev/disk/:/dev/disk:ro  ${CADVISOR_DOCKER_IMAGE}
	check_err "Error: cadvisor did not start up"

	echo "Starting prometheus container"
	docker run -d --rm --name=prometheus -p 9090:9090   --net=host -v ${PWD}/${PROMETHEUS_MANIFEST}:/etc/prometheus/prometheus.yml ${PROMETHEUS_DOCKER_IMAGE}
	check_err "Error: prometheus did not start up"

	echo "Starting grafana container"
	docker run -d --rm --name=grafana    -p 3000:3000   --net=host -v ${PWD}/${GRAFANA_MANIFESTS}:/etc/grafana/provisioning/ ${GRAFANA_DOCKER_IMAGE}
	check_err "Error: grafana did not start up"
}

#
function docker_deploy() {
	echo 
	create_dummy_json_file
	echo "Info: Waiting for prometheus/grafana/cadvisor to be up and running"
	sleep 2
	echo "Starting kruize container"
	docker run -d --rm --name=kruize     -p 31313:31313 --net=host --env CLUSTER_TYPE="DOCKER" --env MONITORING_AGENT_ENDPOINT="http://localhost:9090" --env MONITORING_AGENT="Prometheus" -v ${PWD}/kruize-docker.json:/opt/app/kruize-docker.json ${KRUIZE_DOCKER_IMAGE}
	check_err "Error: kruize did not start up"
	echo "Waiting for kruize container to come up"
	sleep 10
	app_monitor_loop
}

# 
function docker_start() {
	echo
	echo "###   Installing kruize for docker..."
	echo
	docker_prereq
	docker_setup
	docker_deploy
}

function docker_terminate() {
	echo -n "###   Uninstalling kruize for docker..."
	docker stop kruize grafana prometheus cadvisor 2>/dev/null
	rm -f ${DOCKER_TMP_JSON} ${DOCKER_JSON}
	echo "done"
}

################################  ^ Docker ^ ##################################

##################################  v ICP v ###################################

# Check if the cloudctl tool is installed
function icp_prereq() {
	# Check if cloudctl and kubectl are present
	echo
	echo -n "Info: Checking pre requisites for ICP..."
	kubectl_tool=$(which kubectl)
	check_err "Error: Please install the kubectl tool"
	cloudctl_tool=$(which cloudctl)
	check_err "Error: Please install the cloudctl tool"
	echo "done"
}

# Create a SA and RBAC for deploying kruize onto ICP
function icp_first() {
	kruize_ns="kube-system"
	kubectl_cmd="kubectl -n ${kruize_ns}"
	# Login to the cluster
	echo "Info: Logging in to ICP cluster..."
	if [ ${non_interactive} == 1 ]; then
		cloudctl login -u ${user} -p ${password} -n ${kruize_ns} -a ${kurl}
	elif [ ! -z ${kurl} ]; then
		cloudctl login -a ${kurl}
	else
		cloudctl login
	fi
	check_err "Error: cloudctl login failed."

	# Check if the service account already exists
	sa_exists=$(${kubectl_cmd} get sa | grep ${SA_NAME})
	check_err "Error: cloudctl login failed."
	if [ "${sa_exists}" != "" ]; then
		return;
	fi
	echo "Info: One time setup - Create a service account to deploy kruize"
	sed "s/{{ KRUIZE_NAMESPACE }}/${kruize_ns}/" ${SA_TEMPLATE} > ${SA_MANIFEST}
	${kubectl_cmd} apply -f ${SA_MANIFEST}
	check_err "Error: Failed to create service account and RBAC"
}

# Update yaml with the current ICP instance specific details
function icp_setup() {
	# Get the bearer token
	br_token=$(cloudctl tokens | grep "Bearer" | cut -d" " -f4-5)
	pservice=""
	# Get the cloud endpoint url
	kurl=$(cloudctl api | grep "Endpoint" | awk '{ print $3 }')
	# Prometheus should be accessible at ${kurl}/prometheus
	purl="${kurl}/prometheus"
	echo
	echo "Info: Setting Prometheus URL as ${purl}"
	sleep 1

	sed "s/{{ K8S_TYPE }}/ICP/" ${DEPLOY_TEMPLATE} > ${DEPLOY_MANIFEST}
	sed -i "s/{{ KRUIZE_VERSION }}/${KRUIZE_VERSION}/" ${DEPLOY_MANIFEST}
	sed -i "s/{{ BEARER_AUTH_TOKEN }}/${br_token}/" ${DEPLOY_MANIFEST}
	sed -i "s/{{ MONITORING_SERVICE }}/${pservice}/" ${DEPLOY_MANIFEST}
	sed -i "s|{{ MONITORING_AGENT_ENDPOINT }}|${purl}|" ${DEPLOY_MANIFEST}
}

# For ICP, you can deploy using kubectl
function icp_deploy() {
	echo "Info: Deploying kruize yaml to ICP cluster"
	${kubectl_cmd} apply -f ${DEPLOY_MANIFEST}
	sleep 2
	check_running kruize
}

# Deploy kruize to IBM Cloud Private
function icp_start() {
	echo
	echo "###   Installing kruize for ICP"
	echo
	icp_prereq
	icp_first
	icp_setup
	icp_deploy
}

function icp_terminate() {
	# Add ICP cleanup code
	echo 
}

##################################  ^ ICP ^ ###################################

###############################  v OpenShift v ################################

# Check if the oc tool is installed
function openshift_prereq() {
	# Check if oc tool is installed
	echo
	echo -n "Info: Checking pre requisites for OpenShift..."
	oc_tool=$(which oc)
	check_err "Error: Please install the oc tool"
	echo "done"
}

# Create a service account for kruize to be deployed into and setup the proper RBAC for it
function openshift_first() {
	kruize_ns="openshift-monitoring"
	oc_cmd="oc -n ${kruize_ns}"
	kubectl_cmd="kubectl -n ${kruize_ns}"
	# Login to the cluster
	echo "Info: Logging in to OpenShift cluster..."
	if [ ! -z ${kurl} ]; then
		oc login ${kurl}
	else
		oc login
	fi
	check_err "Error: oc login failed."

	# Check if the service account already exists
	sa_exists=$(${oc_cmd} get sa | grep ${SA_NAME})
	if [ "${sa_exists}" != "" ]; then
		return;
	fi
	echo "Info: One time setup - Create a service account to deploy kruize"
	sed "s/{{ KRUIZE_NAMESPACE }}/${kruize_ns}/" ${SA_TEMPLATE} > ${SA_MANIFEST}
	${oc_cmd} apply -f ${SA_MANIFEST}
	check_err "Error: Failed to create service account and RBAC"
}

# Update yaml with the current OpenShift instance specific details
function openshift_setup() {
	# Get the bearer token
	br_token=$(oc whoami --show-token)
	br_token="Bearer ${br_token}"
	pservice=""
	prom_path=$(oc get route --all-namespaces=true | grep prometheus-k8s | awk '{ print $3 }')
	purl="https://${prom_path}"
	echo
	echo "Info: Setting Prometheus URL as ${purl}"
	sleep 1

	sed "s/{{ K8S_TYPE }}/OpenShift/" ${DEPLOY_TEMPLATE} > ${DEPLOY_MANIFEST}
	sed -i "s/{{ KRUIZE_VERSION }}/${KRUIZE_VERSION}/" ${DEPLOY_MANIFEST}
	sed -i "s/{{ BEARER_AUTH_TOKEN }}/${br_token}/" ${DEPLOY_MANIFEST}
	sed -i "s/{{ MONITORING_SERVICE }}/${pservice}/" ${DEPLOY_MANIFEST}
	sed -i "s|{{ MONITORING_AGENT_ENDPOINT }}|${purl}|" ${DEPLOY_MANIFEST}
}

# Deploy to the openshift-monitoring namespace for OpenShift
function openshift_deploy() {
	echo "Info: Deploying kruize yaml to OpenShift cluster"
	# Deploy into the "openshift-monitoring" namespace/project
	oc project ${kruize_ns}
	${oc_cmd} apply -f ${DEPLOY_MANIFEST}
	sleep 2
	check_running kruize
}

function openshift_start() {
	echo
	echo "OpenShift support coming soon!"
	exit -1;

	openshift_prereq
	openshift_first
	openshift_setup
	openshift_deploy
}

function openshift_terminate() {
	# Add OpenShift cleanup code
	echo
}

###############################  ^ OpenShift ^ ################################

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
	sed "s/{{ KRUIZE_NAMESPACE }}/${kruize_ns}/" ${SA_TEMPLATE} > ${SA_MANIFEST}
	${kubectl_cmd} apply -f ${SA_MANIFEST}
	check_err "Error: Failed to create service account and RBAC"
	${kubectl_cmd} apply -f ${MINIKUBE_MANIFEST}
	check_err "Error: Failed to create service monitor for Prometheus"
}

# Update yaml with the current ICP instance specific details
function minikube_setup() {
	pservice="prometheus-k8s"
	purl=""
	br_token=""

	sed "s|extensions/v1beta1|apps/v1|" ${DEPLOY_TEMPLATE} > ${DEPLOY_MANIFEST}
	sed -i "s/replicas: 1/replicas: 1\n  selector:\n    matchLabels:\n      app: kruize/" ${DEPLOY_MANIFEST}
	sed -i "s/{{ KRUIZE_VERSION }}/${KRUIZE_VERSION}/" ${DEPLOY_MANIFEST}
	sed -i "s/{{ K8S_TYPE }}/Minikube/" ${DEPLOY_TEMPLATE} ${DEPLOY_MANIFEST}
	sed -i "s/{{ BEARER_AUTH_TOKEN }}/${br_token}/" ${DEPLOY_MANIFEST}
	sed -i "s/{{ MONITORING_SERVICE }}/${pservice}/" ${DEPLOY_MANIFEST}
	sed -i "s|{{ MONITORING_AGENT_ENDPOINT }}|${purl}|" ${DEPLOY_MANIFEST}
	sed -i "/nodeSelector/d" ${DEPLOY_MANIFEST}
	sed -i "/node-role/d" ${DEPLOY_MANIFEST}
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
	# Add minikube cleanup code
	echo
}

###############################  ^ MiniKube ^ #################################

# Iterate through the commandline options
while getopts ac:i:k:n:p:stu: gopts
do
	case ${gopts} in
	a)
		non_interactive=1
		;;
	c)
		cluster_type="${OPTARG}"
		check_cluster_type
		;;
	i)
		KRUIZE_DOCKER_IMAGE="${OPTARG}"		
		;;
	k)
		kurl="${OPTARG}"
		;;
	n)
		kruize_ns="${OPTARG}"
		;;
	p)
		password="${OPTARG}"
		;;
	s)
		setup=1
		;;
	t)
		setup=0
		;;
	u)
		user="${OPTARG}"
		;;
	[?])
		usage
	esac
done

# Call the proper setup function based on the cluster_type
if [ ${setup} == 1 ]; then
	${cluster_type}_start
else
	${cluster_type}_terminate
fi
