/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
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

package com.kruize.collection;

import com.kruize.metrics.MetricsImpl;
import com.kruize.metrics.runtimes.java.JavaApplicationMetricsImpl;
import com.kruize.metrics.runtimes.java.openj9.OpenJ9MetricCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

class CollectRuntimeMetrics
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectRuntimeMetrics.class);

    /**
     * Collect runtime metrics, if available, for an instance.
     *
     * @param metrics instance of the application
     * @param monitoringAgentEndpoint monitoring agent endpoint
     */
    static void collectRuntimeMetrics(MetricsImpl metrics, String monitoringAgentEndpoint)
    {
        if (metrics.getRuntime().equals("java"))
        {
            collectJavaMetrics(metrics, monitoringAgentEndpoint);
        }
        else if (metrics.getRuntime().equals("nodejs"))
        {
            collectNodeMetrics(metrics, monitoringAgentEndpoint);
        }
    }

    /**
     * Collect runtime metrics, if available, for an instance of a java application.
     *
     * @param metrics instance of the application
     * @param monitoringAgentEndpoint monitoring agent endpoint
     */
    private static void collectJavaMetrics(MetricsImpl metrics, String monitoringAgentEndpoint)
    {
        String labelName = metrics.getLabelName();

        if (JavaApplicationMetricsImpl.javaApplicationInfoMap.containsKey(labelName))
        {
            if (JavaApplicationMetricsImpl.javaApplicationInfoMap
                    .get(labelName)
                    .getVM()
                    .equals("OpenJ9"))
            {
                OpenJ9MetricCollector openJ9MetricCollector = new OpenJ9MetricCollector(
                        JavaApplicationMetricsImpl.javaApplicationInfoMap.get(labelName).getGcPolicy());

                openJ9MetricCollector.collectOpenJ9Metrics(metrics, monitoringAgentEndpoint, "used");

                if (!JavaApplicationMetricsImpl.javaApplicationMetricsMap.containsKey(labelName))
                {
                    JavaApplicationMetricsImpl.javaApplicationMetricsMap.put(labelName, new ArrayList<>());
                }

                JavaApplicationMetricsImpl.javaApplicationMetricsMap.get(labelName).add(openJ9MetricCollector);
            }
        }
    }

    private static void collectNodeMetrics(MetricsImpl metrics, String monitoringAgentEndpoint)
    {

    }

}
