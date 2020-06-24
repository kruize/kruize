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
import com.kruize.service.HealthService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

public class Initialize
{
    public static void setup_deployment_info() throws Exception
    {
        String cluster_type = System.getenv("CLUSTER_TYPE");
        String monitoring_agent_endpoint = System.getenv("MONITORING_AGENT_ENDPOINT");
        String monitoring_agent_service = System.getenv("MONITORING_SERVICE");
        String auth_token = System.getenv("AUTH_TOKEN");

        Defaults defaults  = AbstractDefaults.getInstance(cluster_type);

        String k8S_type = getEnv("K8S_TYPE", defaults.getK8sType());
        String auth_type = getEnv("AUTH_TYPE", defaults.getAuthType());
        String monitoring_agent = getEnv("MONITORING_AGENT", defaults.getMonitoringAgent());
        String logging_level = getEnv("LOGGING_LEVEL", defaults.getDebugLevel());

        DeploymentInfo.setClusterType(cluster_type);
        DeploymentInfo.setKubernetesType(k8S_type);
        DeploymentInfo.setAuthType(auth_type);
        DeploymentInfo.setMonitoringAgent(monitoring_agent);
        DeploymentInfo.setAuthToken(auth_token);
        DeploymentInfo.setMonitoringAgentEndpoint(monitoring_agent_endpoint);
        DeploymentInfo.setMonitoringAgentService(monitoring_agent_service);
        DeploymentInfo.checkMonitoringAgentRunning();

        /* Initialization done successfully */
        HealthService.setCurrentStatus(HealthService.STATUS_UP);

        /* Update logging level from the env */
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.toLevel(logging_level));

        DeploymentInfo.logDeploymentInfo();
    }

    private static String getEnv(String env, String defaults)
    {
        return (System.getenv(env) != null)
                ? System.getenv(env).toUpperCase()
                : defaults;
    }

}
