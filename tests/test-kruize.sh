#!/bin/bash
#
# Copyright (c) 2020, 2020 IBM Corporation, Red Hat and others.
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
# Main script to execute kruize tests
#

KRUIZE_VERSION=$(cat ../.kruize-version)

# Set the default benchmarks location repo
APP_REPO="$HOME/benchmarks/"

KRUIZE_DOCKER_REPO="kruize/kruize"
KRUIZE_DOCKER_IMAGE=${KRUIZE_DOCKER_REPO}:${KRUIZE_VERSION}
KRUIZE_PORT="31313"

ROOT_DIR="${PWD}"
SCRIPTS_DIR="${ROOT_DIR}/scripts"
KRUIZE_REPO="${ROOT_DIR}/.."

# source all the test scripts
. ${SCRIPTS_DIR}/functional-tests.sh

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

tctype="functional"
resultsdir="$HOME/kruize-results"

# usage function
function usage() {
	echo
	echo "Usage: $0 [-a] [-k kurl ] [-c [docker|icp|minikube|openshift]] [-i docker-image] [-s|t] [-u user] [-p password] [-n namespace] [--timeout=x, x in seconds, for docker only]"
	echo " -r [location of benchmarks] [--resultsdir=results directory] [ --tctype=functional/sanity]   -s = start(default), -t = terminate"
	exit -1
}


# Check if the cluster_type is one of the supported types
function check_cluster_type() {
	case "${cluster_type}" in
	docker|icp|minikube|openshift)
		;;
	*)
		echo "Error: unsupported cluster type: ${cluster_type}"
		exit -1
	esac
}

# Check if the test case type is supported
function check_testcase_type() {
	case "${tctype}" in
	functional|sanity)
		;;
	*)
		echo "Error: unsupported test case type: ${tctype}"
		exit -1
	esac
}


# Iterate through the commandline options
while getopts ac:i:k:n:p:stu:r:-: gopts
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
			resultsdir=*)
				resultsdir=${OPTARG#*=}
				;;
			tctype=*)
				tctype=${OPTARG#*=}
				check_testcase_type
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
	r)
		APP_REPO="${OPTARG}"
		;;
	[?])
		usage
	esac
done

# Call the proper test function based on the test type
if [ ${setup} == 1 ]; then
	if [ $tctype == "functional" ]; then
		functional_test
	elif [ $tctype == "sanity" ]; then
		sanity_test
	fi
else
	cleanup
fi

