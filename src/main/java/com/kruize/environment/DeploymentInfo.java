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

package com.kruize.environment;

public class DeploymentInfo
{
    private static String clusterType = "kubernetes";
    private static String kubernetesType = "ICP";
    private static String authType = "OIDC";
    private static String authToken;
    private static String monitoringAgent = "prometheus";
    private static String monitoringAgentService = "prometheus-k8s";

    private static String monitoringAgentEndpoint = "";

    public static String getMonitoringAgentEndpoint()
    {
        return monitoringAgentEndpoint;
    }

    public static void setMonitoringAgentEndpoint(String monitoringAgentEndpoint)
    {
        DeploymentInfo.monitoringAgentEndpoint = monitoringAgentEndpoint;
    }

    public static String getClusterType()
    {
        return clusterType;
    }

    public static void setClusterType(String clusterType)
    {
        DeploymentInfo.clusterType = clusterType;
    }

    public static String getKubernetesType()
    {
        return kubernetesType;
    }

    public static void setKubernetesType(String kubernetesType)
    {
        DeploymentInfo.kubernetesType = kubernetesType;
    }

    public static String getAuthType()
    {
        return authType;
    }

    public static void setAuthType(String authType)
    {
        DeploymentInfo.authType = authType;
    }

    public static String getAuthToken()
    {
        return authToken;
    }

    public static void setAuthToken(String authToken)
    {
        DeploymentInfo.authToken = authToken;
    }

    public static String getMonitoringAgent()
    {
        return monitoringAgent;
    }

    public static void setMonitoringAgent(String monitoringAgent)
    {
        DeploymentInfo.monitoringAgent = monitoringAgent;
    }

    public static String getMonitoringAgentService()
    {
        return monitoringAgentService;
    }

    public static void setMonitoringAgentService(String monitoringAgentService)
    {
        DeploymentInfo.monitoringAgentService = monitoringAgentService;
    }
}
