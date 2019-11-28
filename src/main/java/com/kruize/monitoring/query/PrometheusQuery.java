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

package com.kruize.monitoring.query;

import com.kruize.environment.DeploymentInfo;
import com.kruize.monitoring.query.docker.DockerPrometheusQuery;
import com.kruize.monitoring.query.kubernetes.KubernetesPrometheusQuery;

public abstract class PrometheusQuery implements Query
{
    private static PrometheusQuery prometheusQuery = null;
    protected String podLabel = "pod_name";
    protected String containerLabel = "container_name";

    static {
        getInstance();
    }

    public static PrometheusQuery getInstance()
    {
        if (prometheusQuery == null) {
            if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
                prometheusQuery = new DockerPrometheusQuery();
            } else {
                prometheusQuery = new KubernetesPrometheusQuery();
            }
        }
        return prometheusQuery;
    }

    @Override
    public String getAPIEndpoint()
    {
        return "/api/v1/query?query=";
    }

    @Override
    public String getPreviousCpuReqRec(String applicationName)
    {
        return "(kruize_exp_cpu_requests{application_name=\"" + applicationName + "\"}[2h])";
    }

    @Override
    public String getPreviousCpuLimRec(String applicationName)
    {
        return "(kruize_exp_cpu_limits{application_name=\"" + applicationName + "\"}[2h])";
    }

    @Override
    public String getPreviousMemReqRec(String applicationName)
    {
        return "(kruize_exp_memory_requests{application_name=\"" + applicationName + "\"}[2h])";
    }

    @Override
    public String getPreviousMemLimRec(String applicationName)
    {
        return "(kruize_exp_memory_limits{application_name=\"" + applicationName + "\"}[2h])";
    }

    public String getPodLabel()
    {
        return podLabel;
    }

    public void setPodLabel(String podLabel)
    {
        this.podLabel = podLabel;
    }

    public String getContainerLabel()
    {
        return containerLabel;
    }

    public void setContainerLabel(String containerLabel)
    {
        this.containerLabel = containerLabel;
    }
}
