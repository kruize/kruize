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

package com.kruize.query.kubernetes;

import com.kruize.query.PrometheusQuery;

public class KubernetesPrometheusQuery extends PrometheusQuery {

    @Override
    public String getCpuQuery(String instanceName)
    {
        return "rate(container_cpu_usage_seconds_total{" +
                "pod_name=~\"" + instanceName + "\",container_name!=\"POD\"}[1m])";
    }

    @Override
    public String getRssQuery(String instanceName)
    {
        return "container_memory_working_set_bytes{container_name=\"\",pod_name=\"" + instanceName + "\"}";
    }

    @Override
    public String getNetworkBytesTransmitted(String instanceName)
    {
        return "container_network_transmit_bytes_total{container_name=\"\",pod_name=\"" + instanceName + "\"}";
    }

    @Override
    public String getNetworkBytesReceived(String instanceName)
    {
        return "container_network_receive_bytes_total{container_name=\"\",pod_name=\"" + instanceName + "\"}";
    }

    @Override
    public String getMemoryRequests(String instanceName)
    {
        return "container_spec_memory_reservation_limit_bytes{container_name=\"\"," +
                "pod_name=~\"" + instanceName + ".*\"}";
    }

    @Override
    public String getMemoryLimit(String instanceName)
    {
        return "container_spec_memory_limit_bytes{container_name=\"\"," + "pod_name=~\"" + instanceName + ".*\"}";
    }

    @Override
    public String getPreviousCpuQuery(String instanceName)
    {
        return "rate(container_cpu_usage_seconds_total{" +
                "pod_name=~\"" + instanceName + "\",container_name!=\"POD\"}[1m])[5h]";
    }

    @Override
    public String getPreviousRssQuery(String instanceName)
    {
        return "container_memory_working_set_bytes{container_name=\"\",pod_name=\"" + instanceName + "\"}[5h]";
    }
}
