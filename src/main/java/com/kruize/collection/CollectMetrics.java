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

package com.kruize.collection;

import com.kruize.environment.DeploymentInfo;
import com.kruize.environment.EnvTypeImpl;
import com.kruize.exceptions.ApplicationIdleStateException;
import com.kruize.main.Kruize;
import com.kruize.metrics.AbstractMetrics;
import com.kruize.metrics.MetricCollector;
import com.kruize.metrics.Metrics;
import com.kruize.query.Query;
import com.kruize.recommendations.application.AbstractApplicationRecommendations;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kruize.util.HttpUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CollectMetrics implements Runnable
{
    private EnvTypeImpl envType = EnvTypeImpl.getInstance();
    private Query query = envType.query;

    @SuppressWarnings("unchecked")
    private AbstractApplicationRecommendations<AbstractMetrics> applicationRecommendations = envType.applicationRecommendations;

    static
    {
        HttpUtil.disableSSLVertification();
    }

    @SuppressWarnings("unchecked")
    private void getMetrics(String application)
    {
        String monitoringAgentEndPoint = DeploymentInfo.getMonitoringAgentEndpoint() + query.getAPIEndpoint();

        for (AbstractMetrics metrics : (ArrayList<AbstractMetrics>) envType.applicationRecommendations.applicationMap.get(application)) {
            /* TODO add better checks to see if instance is still running */
            if (metrics.getStatus().equals("Running")) {
                String instanceName = metrics.getName();

                String rssQuery = query.getRssQuery(instanceName);

                /* TODO replace it by seconds and calculate ourselves? */
                String cpuQuery = query.getCpuQuery(instanceName);

                try {
                    CurrentMetrics currentMetrics =
                            new CurrentMetrics(monitoringAgentEndPoint, metrics, rssQuery, cpuQuery).invoke();

                    analyseMetrics(metrics);

                    setKruizeRecommendations(application, metrics, currentMetrics);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setKruizeRecommendations(String application, Metrics metrics,
                                         CurrentMetrics currentMetrics)
    {
        final double MIN_CPU_REQUEST = 0.5;
        final double MIN_CPU_LIMIT = 1;

        String namespace = metrics.getNamespace();

        Kruize.applicationCpuUsed.labels(namespace, application).set(currentMetrics.getCpu());
        Kruize.applicationMemUsed.labels(namespace, application).set(currentMetrics.getRss());

        double cpuRequests = metrics.getCpuRequests();
        double cpuLimit = metrics.getCpuLimit();

        if (cpuRequests > 0) {
            Kruize.cpuRequests.labels(namespace, application).set(Math.max(cpuRequests, MIN_CPU_REQUEST));
        } else {
            Kruize.cpuRequests.labels(namespace, application).set(-1);
        }

        if (cpuLimit > 0) {
            Kruize.cpuLimits.labels(namespace, application).set(Math.max(cpuLimit, MIN_CPU_LIMIT));
        } else {
            Kruize.cpuLimits.labels(namespace, application).set(-1);
        }

        Kruize.memoryLimits.labels(namespace, application).set(metrics.getRssLimits());
        Kruize.memoryRequests.labels(namespace, application).set(metrics.getRssRequests());

        Kruize.originalCpuLimits.labels(namespace, application).set(metrics.getOriginalCpuLimit());
        Kruize.originalCpuRequests.labels(namespace, application).set(metrics.getOriginalCpuRequests());

        Kruize.originalMemoryRequests.labels(namespace, application).set(metrics.getOriginalMemoryRequests());
        Kruize.originalMemoryLimits.labels(namespace, application).set(metrics.getOriginalMemoryLimit());
    }

    @SuppressWarnings("unchecked")
    private void analyseMetrics(Metrics metrics)
    {
        envType.analysis.calculateCpuRequests(metrics);
        envType.analysis.calculateCpuLimit(metrics);
        envType.analysis.calculateMemRequests(metrics, MetricCollector.CPU_INDEX, MetricCollector.RSS_INDEX);
        envType.analysis.calculateMemLimit(metrics);
        envType.analysis.finalizeY2DRecommendations(metrics);
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run()
    {
        try {

            for (String application : applicationRecommendations.applicationMap.keySet()) {
                getPreviousData(application);
                getPreviousKruizeRecs(application, query);
            }

            while (true) {
                for (String application : applicationRecommendations.applicationMap.keySet()) {
                    getMetrics(application);
                }
                TimeUnit.SECONDS.sleep(10);
                envType.getAllApps();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getPreviousData(String application)
    {
        for (AbstractMetrics metrics : applicationRecommendations.applicationMap.get(application)) {
            ArrayList<Double> rssList = new ArrayList<>();
            ArrayList<Double> cpuList = new ArrayList<>();

            try {
                String prometheusURL = DeploymentInfo.getMonitoringAgentEndpoint() + query.getAPIEndpoint();

                String instanceName = metrics.getName();

                JsonArray rssArray = getAsJsonArray(new URL(prometheusURL + query.getPreviousRssQuery(instanceName)), "values");
                JsonArray cpuArray = getAsJsonArray(new URL(prometheusURL + query.getPreviousCpuQuery(instanceName)), "values");

                for (JsonElement rssValue : rssArray) {
                    rssList.add(rssValue.getAsJsonArray().get(1).getAsDouble());
                }

                for (JsonElement cpuValue : cpuArray) {
                    cpuList.add(cpuValue.getAsJsonArray().get(1).getAsDouble());
                }

                if (rssList.size() == cpuList.size()) {
                    for (int i = 0; i < rssList.size(); i++) {
                        metrics.metricCollector.add(new MetricCollector(rssList.get(i), cpuList.get(i), 0));
                    }
                }
            } catch (NullPointerException | MalformedURLException e) {
                System.out.println("Could not get previous data. Please check your prometheus version");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Could not get previous data.");
            }
        }
    }

    private JsonArray getAsJsonArray(URL url, String values) throws IndexOutOfBoundsException
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
                .get(values)
                .getAsJsonArray();
    }

    private void getPreviousKruizeRecs(String application, Query query)
    {
        String prometheusURL = DeploymentInfo.getMonitoringAgentEndpoint() + query.getAPIEndpoint();

        for (Metrics metrics : applicationRecommendations.applicationMap.get(application)) {
            String applicationName  = metrics.getApplicationName();

            double previousCpuLimit = 0;
            double previousRssLimit = 0;
            double previousCpuRequests = 0;
            double previousRssRequests = 0;
            try {
                previousCpuLimit =
                        getPreviousKruizeData(prometheusURL + query.getPreviousCpuLimRec(applicationName));
                previousRssLimit =
                        getPreviousKruizeData(prometheusURL + query.getPreviousMemLimRec(applicationName));
                previousCpuRequests =
                        getPreviousKruizeData(prometheusURL + query.getPreviousCpuReqRec(applicationName));
                previousRssRequests =
                        getPreviousKruizeData(prometheusURL + query.getPreviousMemReqRec(applicationName));
            } catch (IndexOutOfBoundsException ignored) { }

            metrics.setCurrentCpuLimit(previousCpuLimit);
            metrics.setCurrentRssLimit(previousRssLimit);
            metrics.setCurrentCpuRequests(previousCpuRequests);
            metrics.setCurrentRssRequests(previousRssRequests);

            metrics.setCpuLimit(Math.max(previousCpuLimit, metrics.getCpuLimit()));
            metrics.setCpuRequests(Math.max(previousCpuRequests, metrics.getCpuRequests()));
            metrics.setRssRequests(Math.max(previousRssRequests, metrics.getRssRequests()));
            metrics.setRssLimit(Math.max(previousRssLimit, metrics.getRssLimits()));
        }
    }

    private double getPreviousKruizeData(String recommendationURL)
    {
        System.out.println(recommendationURL);
        try {
            JsonArray kruizeArray = getAsJsonArray(new URL(recommendationURL), "values");

            //get last old value
            return kruizeArray
                    .get(kruizeArray.size() - 1)
                    .getAsJsonArray()
                    .get(1)
                    .getAsDouble();
        } catch (IndexOutOfBoundsException | MalformedURLException e) {
            System.out.println("Could not get previous recommendations");
            return -1;
        }
    }

    private class CurrentMetrics
    {
        private String monitoringAgentEndPoint;
        private AbstractMetrics metrics;
        private String rssQuery;
        private String cpuQuery;
        private double rss;
        private double cpu;

        CurrentMetrics(String monitoringAgentEndPoint, AbstractMetrics metrics, String rssQuery, String cpuQuery)
        {
            this.monitoringAgentEndPoint = monitoringAgentEndPoint;
            this.metrics = metrics;
            this.rssQuery = rssQuery;
            this.cpuQuery = cpuQuery;
        }

        double getRss()
        {
            return rss;
        }

        double getCpu()
        {
            return cpu;
        }

        CurrentMetrics invoke() throws MalformedURLException
        {
            double MIN_CPU = 0.02;
            try {
                cpu = getValueForQuery(new URL(monitoringAgentEndPoint + cpuQuery));
                System.out.println("CPU: " + cpu);

                rss = getValueForQuery(new URL(monitoringAgentEndPoint + rssQuery));
                System.out.println("RSS: " + rss);

                //TODO Get network data from monitoring agent
                double network = 0;

                if (cpu < MIN_CPU)
                    throw new ApplicationIdleStateException();

                metrics.metricCollector.add(new MetricCollector(rss, cpu, network));
                return this;

            } catch (IndexOutOfBoundsException | ApplicationIdleStateException e) {
                return this;
            }

        }

        private double getValueForQuery(URL url) throws IndexOutOfBoundsException
        {
            return getAsJsonArray(url, "value")
                    .get(1)
                    .getAsDouble();
        }
    }
}
