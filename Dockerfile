#
# Copyright (c) 2019, 2019 IBM Corporation and others.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
FROM adoptopenjdk/maven-openjdk11-openj9:latest as mvnbuild-openj9

RUN apt-get update \
    && apt-get install -y --no-install-recommends git vim \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/app

ARG KRUIZE_VERSION

COPY src /opt/app/src
COPY pom.xml /opt/app/

RUN mvn install dependency:copy-dependencies

RUN mvn clean package

FROM adoptopenjdk:11-jre-openj9

ARG KRUIZE_VERSION

WORKDIR /opt/app

RUN useradd -u 1001 -r -g root -s /usr/sbin/nologin kruize \
    && chown -R 1001:0 /opt \
    && chmod -R g+rw /opt

USER 1001

COPY --chown=1001:0 --from=mvnbuild-openj9 /opt/app/target/kruize-monitoring-${KRUIZE_VERSION}-jar-with-dependencies.jar /opt/app/kruize-monitoring-with-dependencies.jar

CMD ["java", "-jar", "/opt/app/kruize-monitoring-with-dependencies.jar"]
