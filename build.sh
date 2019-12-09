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
KRUIZE_DOCKER_IMAGE="kruize:${KRUIZE_VERSION}"

function usage() {
	echo "Usage: $0 [-v version_string] [-t docker_image_name]"
	exit -1
}

# Check error code from last command, exit on error
function check_err() {
	err=$?
	if [ ${err} -ne 0 ]; then
		echo "$*"
		exit -1
	fi
}

# Remove any previous images of kruize
function cleanup() {
	docker rmi $(docker images | grep kruize | awk '{ print $3 }') >/dev/null 2>/dev/null
	docker rmi $(docker images | grep kruize | awk '{ printf "%s:%s\n", $1, $2 }') >/dev/null 2>/dev/null
}

# Iterate through the commandline options
while getopts t:v: gopts
do
	case ${gopts} in
	v)
		KRUIZE_VERSION="${OPTARG}"
		;;
	t)
		KRUIZE_DOCKER_IMAGE="${OPTARG}"
		;;
	[?])
		usage
	esac
done

git pull
cleanup

DOCKER_REPO=$(echo ${KRUIZE_DOCKER_IMAGE} | awk -F":" '{ print $1 }')
DOCKER_TAG=$(echo ${KRUIZE_DOCKER_IMAGE} | awk -F":" '{ print $2 }')
if [ -z "${DOCKER_TAG}" ]; then
	DOCKER_TAG="latest"
fi

# Build the docker image with the given version string
docker build --pull --no-cache --build-arg KRUIZE_VERSION=${DOCKER_TAG} -t ${KRUIZE_DOCKER_IMAGE} .
check_err "Docker build of ${KRUIZE_DOCKER_IMAGE} failed."

docker images | grep -e "TAG" -e "${DOCKER_REPO}" | grep "${DOCKER_TAG}"
