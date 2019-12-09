/*******************************************************************************
 * Copyright (c) 2019, 2019 IBM Corporation and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.kruize.initialize;

import com.kruize.environment.AbstractDefaults;
import com.kruize.environment.Defaults;
import com.kruize.environment.DeploymentInfo;
import com.kruize.environment.SupportedTypes;
import com.kruize.exceptions.MonitoringAgentNotSupportedException;
import com.kruize.exceptions.env.ClusterTypeNotSupportedException;
import com.kruize.exceptions.env.K8sTypeNotSupportedException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Initialize
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Initialize.class);

    public static void setup_deployment_info() throws Exception
    {
        String cluster_type = System.getenv("CLUSTER_TYPE").toUpperCase();
        String monitoring_agent_endpoint = System.getenv("MONITORING_AGENT_ENDPOINT");
        String monitoring_agent_service = System.getenv("MONITORING_SERVICE");

        Defaults defaults  = AbstractDefaults.getInstance(cluster_type);

        String k8S_type = getEnv("K8S_TYPE", defaults.getK8sType());
        String auth_type = getEnv("AUTH_TYPE", defaults.getAuthType());
        String monitoring_agent = getEnv("MONITORING_AGENT", defaults.getMonitoringAgent());
        String logging_level = getEnv("LOGGING_LEVEL", defaults.getDebugLevel());

        if (SupportedTypes.CLUSTER_TYPES_SUPPORTED.contains(cluster_type)) {
            DeploymentInfo.setClusterType(cluster_type);
        } else {
            LOGGER.error("Cluster type {} is not supported", cluster_type);
            throw new ClusterTypeNotSupportedException();
        }

        if (SupportedTypes.K8S_TYPES_SUPPORTED.contains(k8S_type)) {
            DeploymentInfo.setKubernetesType(k8S_type);
        } else {
            LOGGER.error("k8s type {} is not suppported", k8S_type);
            throw new K8sTypeNotSupportedException();
        }

        if (SupportedTypes.AUTH_TYPES_SUPPORTED.contains(auth_type)) {
            DeploymentInfo.setAuthType(auth_type);
        }

        if (SupportedTypes.MONITORING_AGENTS_SUPPORTED.contains(monitoring_agent)) {
            DeploymentInfo.setMonitoringAgent(monitoring_agent);
        } else {
            LOGGER.error("Monitoring agent {}  is not supported", monitoring_agent);
            throw new MonitoringAgentNotSupportedException();
        }

        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.toLevel(logging_level));

        String auth_token = System.getenv("AUTH_TOKEN");
        DeploymentInfo.setAuthToken((auth_token == null) ? "" : auth_token);

        DeploymentInfo.setMonitoringAgentEndpoint(monitoring_agent_endpoint);

        if (monitoring_agent_service != null)
            DeploymentInfo.setMonitoringAgentService(monitoring_agent_service.toUpperCase());

        logDeploymentInfo();
    }

    private static String getEnv(String env, String defaults)
    {
        return (System.getenv(env) != null)
                ? System.getenv(env).toUpperCase()
                : defaults;
    }

    private static void logDeploymentInfo()
    {
        LOGGER.info("Cluster Type: {}", DeploymentInfo.getClusterType());
        LOGGER.info("Kubernetes Type: {}", DeploymentInfo.getKubernetesType());
        LOGGER.info("Auth Type: {}", DeploymentInfo.getAuthType());
        LOGGER.info("Monitoring Agent: {}", DeploymentInfo.getMonitoringAgent());
        LOGGER.info("Monitoring agent service: {}\n\n", DeploymentInfo.getMonitoringAgentService());
    }
}
