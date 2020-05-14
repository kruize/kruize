/*
 *
 */

package com.kruize.query.prometheus;

import com.kruize.environment.DeploymentInfo;
import com.kruize.query.Query;
import com.kruize.query.prometheus.docker.*;
import com.kruize.query.prometheus.kubernetes.*;

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
