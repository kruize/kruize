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

package com.kruize.metrics.runtimes.java.openj9;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.kruize.environment.DeploymentInfo;
import com.kruize.exceptions.InvalidValueException;
import com.kruize.metrics.MetricsImpl;
import com.kruize.metrics.runtimes.java.JavaHeap;
import com.kruize.metrics.runtimes.java.JavaMetricCollector;
import com.kruize.metrics.runtimes.java.JavaNonHeap;
import com.kruize.metrics.runtimes.java.openj9.heap.OpenJ9BalancedHeap;
import com.kruize.metrics.runtimes.java.openj9.heap.OpenJ9GenconHeap;
import com.kruize.metrics.runtimes.java.openj9.heap.OpenJ9MetronomeHeap;
import com.kruize.metrics.runtimes.java.openj9.heap.OpenJ9NoGcHeap;
import com.kruize.query.Query;
import com.kruize.query.prometheus.DockerPrometheusQuery;
import com.kruize.query.prometheus.KubernetesPrometheusQuery;
import com.kruize.query.prometheus.runtimes.java.openj9.OpenJ9JavaQuery;
import com.kruize.query.runtimes.java.JavaQuery;
import com.kruize.metrics.MetricsImpl;
import com.kruize.metrics.runtimes.java.JavaMetricCollector;
import com.kruize.query.runtimes.java.openj9.OpenJ9JavaQuery;
import com.kruize.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class OpenJ9MetricCollector implements JavaMetricCollector
{
    private double rss;

    private JavaHeap heap;
    private JavaNonHeap nonHeap;
    private JavaQuery javaQuery;
    private Query query;

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenJ9MetricCollector.class);

    public OpenJ9MetricCollector(String gcPolicy)
    {
        nonHeap = new OpenJ9NonHeap();
        javaQuery = new OpenJ9JavaQuery(gcPolicy, JavaQuery.getPodLabel());

        switch (gcPolicy) {
            case "gencon":
                heap = new OpenJ9GenconHeap();
                break;
            case "balanced":
                heap = new OpenJ9BalancedHeap();
                break;
            case "nogc":
                heap = new OpenJ9NoGcHeap();
                break;
            case "metronome":
                heap = new OpenJ9MetronomeHeap();
                break;
        }

        if (DeploymentInfo.getMonitoringAgent().toUpperCase().equals("PROMETHEUS"))
        {
            if (DeploymentInfo.getClusterType().toUpperCase().equals("KUBERNETES")) {
                query = new KubernetesPrometheusQuery();
            } else {
                query = new DockerPrometheusQuery();
            }
        }
    }

    @Override
    public double getHeap()
    {
        return heap.getTotalSize();
    }

    @Override
    public double getNonHeap()
    {
        return nonHeap.getTotalSize();
    }

    @Override
    public void setHeap(double heap)
    {

    }

    @Override
    public void setNonHeap(double nonHeap)
    {

    }

    public double getRss()
    {
        return rss;
    }

    public void setRss(double rss)
    {
        this.rss = rss;
    }

    public void collectOpenJ9Metrics(MetricsImpl metrics, String monitoringAgentEndPoint, String area)
    {
        String labelName = metrics.getLabelName();

        try {
            double rss = getValueForQuery(new URL(monitoringAgentEndPoint +
                    query.getRssQuery(metrics.getName())));
            setRss(rss);

            for (String partOfHeap: javaQuery.heapQuery.getPartsOfHeap())
            {
                double value = getValueForQuery(new URL(monitoringAgentEndPoint +
                        javaQuery.heapQuery.getHeapQuery(labelName, partOfHeap, area)));
                LOGGER.info("Heap value for {} is {}", partOfHeap, value);
                heap.setHeap(value, partOfHeap);
            }

            for (String partOfNonHeap : javaQuery.nonHeapQuery.getPartsOfNonHeap())
            {
                double value = getValueForQuery(new URL(monitoringAgentEndPoint +
                        javaQuery.nonHeapQuery.getNonHeapQuery(labelName, partOfNonHeap, area)));
                LOGGER.info("Non-heap value for {} is {}", partOfNonHeap, value);
                nonHeap.setNonHeap(value, partOfNonHeap);
            }

        } catch (InvalidValueException | IndexOutOfBoundsException | MalformedURLException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private double getValueForQuery(URL url) throws IndexOutOfBoundsException
    {
        try {
            return getAsJsonArray(url)
                    .get(1)
                    .getAsDouble();

        } catch (Exception e) {
            LOGGER.info(url.toString());
            e.printStackTrace();
            return 0;
        }
    }

    private JsonArray getAsJsonArray(URL url) throws IndexOutOfBoundsException
    {
        String response = HttpUtil.getDataFromURL(url);

        return new JsonParser()
                .parse(response)
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject()
                .get("result")
                .getAsJsonArray()
                .get(0)
                .getAsJsonObject()
                .get("value")
                .getAsJsonArray();
    }
}
