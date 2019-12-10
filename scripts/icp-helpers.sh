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
	sed -i "s|{{ KRUIZE_DOCKER_IMAGE }}|${KRUIZE_DOCKER_IMAGE}|" ${DEPLOY_MANIFEST}
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
	# Indicate deploy failed on error
	if [ "${err}" != "0" ]; then
		exit 1
	fi
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
