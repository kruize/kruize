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

import com.kruize.analysis.docker.DockerAnalysisImpl;
import com.kruize.environment.EnvTypeImpl;
import com.kruize.metrics.ContainerMetrics;
import com.kruize.query.PrometheusQuery;
import com.kruize.recommendations.application.DockerApplicationRecommendations;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DockerEnvImpl extends EnvTypeImpl
{
    @Override
    public void setupMonitoringAgent()
    {

    }

    @Override
    public void setupApplicationRecommendations()
    {
        this.applicationRecommendations = DockerApplicationRecommendations.getInstance();
    }

    @Override
    public void setupAnalysis()
    {
        this.analysis = DockerAnalysisImpl.getInstance();
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

        if(containerList != null && containerList.size() > 0) {
            for(JsonElement container : containerList) {
                if(container != null && container.getAsJsonObject().size() > 0)
                    insertContainerMetrics(container);
            }
        }
        else {
            System.out.println("Looks like you do not have containers to monitor.");
            System.exit(1);
        }

    }

    private void insertContainerMetrics(JsonElement container)
    {
        ContainerMetrics containerMetrics = getContainerMetrics(container);
        String containerName = containerMetrics.getApplicationName();

        if (applicationRecommendations.applicationMap.containsKey(containerName)) {
            applicationRecommendations.addMetricToApplication(containerName, containerMetrics);
        } else {
            ArrayList<ContainerMetrics> containerMetricsArrayList = new ArrayList<>();
            containerMetricsArrayList.add(containerMetrics);
            applicationRecommendations.applicationMap.put(containerName, containerMetricsArrayList);
        }
    }

    private static ContainerMetrics getContainerMetrics(JsonElement container) throws NullPointerException
    {
        ContainerMetrics containerMetrics = new ContainerMetrics();
        String containerName = container.getAsJsonObject().get("name").getAsString();
        containerMetrics.setName(containerName);
        containerMetrics.setNamespace("local");
        containerMetrics.setStatus("Running");
        containerMetrics.setApplicationName(containerName);

        if (container.getAsJsonObject().has("mem_limit"))
        {
            containerMetrics.setOriginalMemoryLimit(container.getAsJsonObject().get("mem_limit").getAsDouble());
        }

        if (container.getAsJsonObject().has("cpu_limit"))
        if (container.getAsJsonObject().has("cpu_limit"))
        {
            containerMetrics.setOriginalMemoryLimit(container.getAsJsonObject().get("cpu_limit").getAsDouble());
        }

        return containerMetrics;
    }
}
