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

package com.kruize.query;

public class PrometheusQuery implements Query
{
    private PrometheusQuery() {}

    private static PrometheusQuery prometheusQuery = null;

    static {
        getInstance();
    }

    public static PrometheusQuery getInstance()
    {
        if (prometheusQuery == null)
            prometheusQuery = new PrometheusQuery();

        return prometheusQuery;
    }

    @Override
    public String getCpuQuery(String podName)
    {
        return "rate(container_cpu_usage_seconds_total{" +
                "pod_name=~\"" + podName + "\",container_name!=\"POD\"}[1m])";
    }

    @Override
    public String getRssQuery(String podName)
    {
        return "container_memory_working_set_bytes{container_name=\"\",pod_name=\"" + podName + "\"}";
    }

    @Override
    public String getNetworkBytesTransmitted(String podName)
    {
        return "container_network_transmit_bytes_total{container_name=\"\",pod_name=\"" + podName + "\"}";
    }

    @Override
    public String getNetworkBytesReceived(String podName)
    {
        return "container_network_receive_bytes_total{container_name=\"\",pod_name=\"" + podName + "\"}";
    }

    @Override
    public String getMemoryRequests(String podName)
    {
        return "container_spec_memory_reservation_limit_bytes{container_name=\"\"," +
                "pod_name=~\"" + podName + ".*\"}";
    }

    @Override
    public String getMemoryLimit(String podName)
    {
        return "container_spec_memory_limit_bytes{container_name=\"\"," +
                "pod_name=~\"" + podName + ".*\"}";
    }

    @Override
    public String getAPIEndpoint()
    {
        return "/api/v1/query?query=";
    }

    @Override
    public String getPreviousCpuQuery(String podName)
    {
        return "rate(container_cpu_usage_seconds_total{" +
                "pod_name=~\"" + podName + "\",container_name!=\"POD\"}[1m])[5h]";
    }

    @Override
    public String getPreviousRssQuery(String podName)
    {
        return "container_memory_working_set_bytes{container_name=\"\",pod_name=\"" + podName + "\"}[5h]";
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
}
