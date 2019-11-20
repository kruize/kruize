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

function usage() {
	echo "Usage: $0 [-v version_string]"
}

# Iterate through the commandline options
while getopts v: gopts
do
	case ${gopts} in
	v)
		KRUIZE_VERSION="${OPTARG}"
		;;
	[?])
		usage
	esac
done

# Fix the pom.xml to have the version set
sed -i "/<artifactId>kruize-monitoring<\/artifactId>/{n;s/<version>.*<\/version>/<version>${KRUIZE_VERSION}<\/version>/}" pom.xml

# Build the docker image with the given version string
docker build --pull --no-cache --build-arg KRUIZE_VERSION=${KRUIZE_VERSION} -t kruize:${KRUIZE_VERSION} .

docker images | grep -e "TAG" -e "kruize"
