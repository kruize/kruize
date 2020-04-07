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

package com.kruize.environment.docker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kruize.environment.EnvTypeImpl;
import com.kruize.exceptions.InvalidValueException;
import com.kruize.metrics.MetricsImpl;
import com.kruize.query.prometheus.PrometheusQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kruize.analysis.AnalysisImpl;
import com.kruize.recommendations.application.ApplicationRecommendationsImpl;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DockerEnvImpl extends EnvTypeImpl
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerEnvImpl.class);

    @Override
    public void setupMonitoringAgent()
    {

    }

    @Override
    public void setupApplicationRecommendations()
    {
        this.applicationRecommendations = ApplicationRecommendationsImpl.getInstance();
    }

    @Override
    public void setupAnalysis()
    {
        this.analysis = AnalysisImpl.getInstance();
    }

    @Override
    public void setupQuery()
    {
        this.query = PrometheusQuery.getInstance();
    }

    @Override
    public void getAllApps()
    {
        JsonArray containerList = null;
        ArrayList<String> monitoredInstances = new ArrayList<>();
        /*
         * kruize-docker.json contains the details of the containers.
         * For each container, the details are expressed in the form of key-value pairs
         * where the keys are name, cpu_limit and mem_limit.
         * Example file:
         * {
         *   "containers": [
         *     { "name": "kruize", "cpu_limit": "0.5", "mem_limit": "70m" },
         *     { "name": "acmeair-mono", "cpu_limit": "3.3", "mem_limit": "350m" },
         *     { "name": "acmeair-db1", "cpu_limit": "5", "mem_limit": "2.3g" }
         *   ]
         * }
         */
        try (FileReader reader = new FileReader("/opt/app/kruize-docker.json"))
        {
            containerList = new JsonParser()
                    .parse(reader)
                    .getAsJsonObject()
                    .get("containers")
                    .getAsJsonArray();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        if (containerList != null && containerList.size() > 0) {
            for (JsonElement container : containerList) {
                if (container != null && container.getAsJsonObject().size() > 0)
                    insertMetrics(container);
                    monitoredInstances.add(container.getAsJsonObject().get("name").getAsString());
            }
        } else {
            LOGGER.error("No containers to monitor.");
            System.exit(1);
        }

        updateStatus(monitoredInstances);
    }

    @SuppressWarnings("unchecked")
    private void insertMetrics(JsonElement container)
    {
        MetricsImpl containerMetrics = null;
        try {
            containerMetrics = getMetrics(container);
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }

        assert containerMetrics != null;
        String containerName = containerMetrics.getApplicationName();

        if (applicationRecommendations.applicationMap.containsKey(containerName)) {
            applicationRecommendations.addMetricToApplication(containerName, containerMetrics);
        } else {
            ArrayList<MetricsImpl> containerMetricsArrayList = new ArrayList<>();
            containerMetricsArrayList.add(containerMetrics);
            applicationRecommendations.applicationMap.put(containerName, containerMetricsArrayList);
        }
    }

    private void updateStatus(ArrayList<String> monitoredInstances)
    {
        for (String application : applicationRecommendations.applicationMap.keySet())
        {
            for (MetricsImpl instance : applicationRecommendations.applicationMap.get(application))
            {
                if (!monitoredInstances.contains(instance.getName()))
                {
                    try {
                        instance.setStatus("terminated");
                    } catch (InvalidValueException ignored) { }
                }
            }
        }
    }

    private static MetricsImpl getMetrics(JsonElement container) throws NullPointerException, InvalidValueException
    {
        MetricsImpl containerMetrics = new MetricsImpl();
        String containerName = container.getAsJsonObject().get("name").getAsString();
        containerMetrics.setName(containerName);
        containerMetrics.setNamespace("local");
        containerMetrics.setStatus("running");
        containerMetrics.setApplicationName(containerName);

        if (container.getAsJsonObject().has("mem_limit")) {
            containerMetrics.setOriginalMemoryLimit(container.getAsJsonObject().get("mem_limit").getAsDouble());
        }

        if (container.getAsJsonObject().has("cpu_limit")) {
            containerMetrics.setOriginalMemoryLimit(container.getAsJsonObject().get("cpu_limit").getAsDouble());
        }

        if (containerName.equals("springboot_application"))
        {
            containerMetrics.setRuntimeInfoAvailable(true);
        }

        return containerMetrics;
    }
}
