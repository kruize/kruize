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
KRUIZE_VERSION=$(cat .kruize-version)

DEPLOY_TEMPLATE="manifests/kruize.yaml_template"
DEPLOY_MANIFEST="manifests/kruize.yaml"
SA_TEMPLATE="manifests/kruize-sa_rbac.yaml_template"
SA_MANIFEST="manifests/kruize-sa_rbac.yaml"
SA_NAME="kruize-sa"
SERVICE_MONITOR_MANIFEST="manifests/servicemonitor/kruize-service-monitor.yaml"

KRUIZE_DOCKER_REPO="kruize/kruize"
KRUIZE_DOCKER_IMAGE=${KRUIZE_DOCKER_REPO}:${KRUIZE_VERSION}
KRUIZE_PORT="31313"

ROOT_DIR="${PWD}"
SCRIPTS_DIR="${ROOT_DIR}/scripts"

# source all the helpers scripts
. ${SCRIPTS_DIR}/docker-helpers.sh
. ${SCRIPTS_DIR}/icp-helpers.sh
. ${SCRIPTS_DIR}/minikube-helpers.sh
. ${SCRIPTS_DIR}/openshift-helpers.sh

# Defaults for the script
# ICP is the default cluster type
cluster_type="icp"
# Call setup by default (and not terminate)
setup=1
# Default mode is interactive
non_interactive=0
# Default namespace is kube-system
kruize_ns="kube-system"
# Default userid is "admin"
user="admin"
# docker: loop timeout is turned off by default
timeout=-1

function ctrlc_handler () {
    # Check if cluster type is docker
    if [[ "$cluster_type" == "docker" ]]; then
        # Terminate the containers [kruize && grafana && prometheus && cadvisor]
        docker_terminate
    fi
    # Exiting gracefully
    exit 2
}

# Handle SIGHUP(1), SIGINT(2), SIGQUIT(3) for cleaning up containers in docker case
trap "ctrlc_handler" 1 2 3 

function usage() {
	echo
	echo "Usage: $0 [-a] [-k url] [-c [docker|icp|minikube|openshift]] [-i docker-image] [-s|t] [-u user] [-p password] [-n namespace] [--timeout=x, x in seconds, for docker only]"
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

# Iterate through the commandline options
while getopts ac:i:k:n:p:stu:-: gopts
do
	case ${gopts} in
	-)
		case "${OPTARG}" in
			timeout=*)
				timeout=${OPTARG#*=}
				if [ -z "${timeout}" ]; then
					usage
				fi
				;;
			*)
				if [ "${OPTERR}" == 1 ] && [ "${OPTSPEC:0:1}" != ":" ]; then
					echo "Unknown option --${OPTARG}" >&2
					usage
				fi
				;;
		esac
		;;
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
