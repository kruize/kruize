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

public class Initialize
{
    public static void setup_deployment_info() throws Exception
    {
        String cluster_type = System.getenv("CLUSTER_TYPE").toUpperCase();
        String monitoring_agent_endpoint = System.getenv("MONITORING_AGENT_ENDPOINT");
        String monitoring_agent_service = System.getenv("MONITORING_SERVICE");

        Defaults defaults  = AbstractDefaults.getInstance(cluster_type);

        String k8S_type = getEnv("K8S_TYPE", defaults.getK8sType());
        String auth_type = getEnv("AUTH_TYPE", defaults.getAuthType());
        String monitoring_agent = getEnv("MONITORING_AGENT", defaults.getMonitoringAgent());

        if (SupportedTypes.CLUSTER_TYPES_SUPPORTED.contains(cluster_type)) {
            DeploymentInfo.setClusterType(cluster_type);
        } else {
            throw new ClusterTypeNotSupportedException();
        }

        if (SupportedTypes.K8S_TYPES_SUPPORTED.contains(k8S_type)) {
            DeploymentInfo.setKubernetesType(k8S_type);
        } else {
            throw new K8sTypeNotSupportedException();
        }

        if (SupportedTypes.AUTH_TYPES_SUPPORTED.contains(auth_type)) {
            DeploymentInfo.setAuthType(auth_type);
        }

        if (SupportedTypes.MONITORING_AGENTS_SUPPORTED.contains(monitoring_agent)) {
            DeploymentInfo.setMonitoringAgent(monitoring_agent);
        } else {
            throw new MonitoringAgentNotSupportedException();
        }

        String auth_token = System.getenv("AUTH_TOKEN");
        DeploymentInfo.setAuthToken((auth_token == null) ? "" : auth_token);

        DeploymentInfo.setMonitoringAgentEndpoint(monitoring_agent_endpoint);

        if (monitoring_agent_service != null)
            DeploymentInfo.setMonitoringAgentService(monitoring_agent_service.toUpperCase());

        printDeploymentInfo();
    }

    private static String getEnv(String env, String defaults)
    {
        return (System.getenv(env) != null)
                ? System.getenv(env).toUpperCase()
                : defaults;
    }

    private static void printDeploymentInfo()
    {
        System.out.println(DeploymentInfo.getClusterType());
        System.out.println(DeploymentInfo.getKubernetesType());
        System.out.println(DeploymentInfo.getAuthType());
        System.out.println(DeploymentInfo.getMonitoringAgent());
        System.out.println(DeploymentInfo.getAuthToken());
        System.out.println(DeploymentInfo.getMonitoringAgentEndpoint());
        System.out.println(DeploymentInfo.getMonitoringAgentService());
    }
}
