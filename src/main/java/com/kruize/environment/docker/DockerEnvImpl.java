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

package com.kruize.environment.docker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kruize.analysis.AnalysisImpl;
import com.kruize.environment.DeploymentInfo;
import com.kruize.environment.EnvTypeImpl;
import com.kruize.exceptions.InvalidValueException;
import com.kruize.metrics.MetricsImpl;
import com.kruize.metrics.runtimes.java.JavaApplicationInfo;
import com.kruize.metrics.runtimes.java.JavaApplicationMetricsImpl;
import com.kruize.query.prometheus.PrometheusQuery;
import com.kruize.query.runtimes.java.JavaQuery;
import com.kruize.recommendations.application.ApplicationRecommendationsImpl;
import com.kruize.recommendations.runtimes.java.openj9.OpenJ9JavaRecommendations;
import com.kruize.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

        getRuntimeInfo();

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

    /**
     * Obtain applications exporting runtime specific information
     */
    private void getRuntimeInfo()
    {
        try {
            getJavaApps();
            getNodeApps();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get applications exporting java metrics, and add them to list of java applications monitored.
     * @throws MalformedURLException
     */
    private void getJavaApps() throws MalformedURLException
    {
        if (!applicationRecommendations.runtimesMap.containsKey("java"))
        {
            applicationRecommendations.runtimesMap.put("java", new ArrayList<>());
        }

        try
        {
            PrometheusQuery prometheusQuery = PrometheusQuery.getInstance();
            JavaQuery javaQuery = new JavaQuery();

            JsonArray javaApps = getJsonArray(new URL(DeploymentInfo.getMonitoringAgentEndpoint()
                    + prometheusQuery.getAPIEndpoint() + javaQuery.fetchJavaAppsQuery()));

            if (javaApps == null) return;

            for (JsonElement jsonElement : javaApps)
            {
                JsonObject metric = jsonElement.getAsJsonObject().get("metric").getAsJsonObject();
                String job = metric.get("job").getAsString();
                String heap_id = metric.get("id").getAsString();

                javaQuery = JavaQuery.getInstance(heap_id);

                /* Check if already in the list */
                if (JavaApplicationMetricsImpl.javaApplicationInfoMap.containsKey(job))
                    continue;

                if (!applicationRecommendations.runtimesMap.get("java").contains(job))
                {
                    applicationRecommendations.runtimesMap.get("java").add(job);

                    String vm = javaQuery.getVm();

                    if (vm.equals("OpenJ9"))
                    {
                        JavaApplicationMetricsImpl.javaApplicationInfoMap.put(
                                job,
                                new JavaApplicationInfo(
                                        vm, javaQuery.getGcPolicy(),
                                        new OpenJ9JavaRecommendations()));
                    }
                }
            }
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }

    }

    private void getNodeApps()
    {

    }

    private JsonArray getJsonArray(URL url)
    {
        String response = HttpUtil.getDataFromURL(url);

        return new JsonParser()
                .parse(response)
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject()
                .get("result")
                .getAsJsonArray();
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

    private MetricsImpl getMetrics(JsonElement container) throws NullPointerException, InvalidValueException
    {
        MetricsImpl containerMetrics = new MetricsImpl();
        String containerName = container.getAsJsonObject().get("name").getAsString();
        containerMetrics.setName(containerName);
        containerMetrics.setStatus("running");
        containerMetrics.setNamespace("local");
        containerMetrics.setLabelName(containerName);
        containerMetrics.setApplicationName(containerName);

        if (container.getAsJsonObject().has("mem_limit")) {
            containerMetrics.setOriginalMemoryLimit(container.getAsJsonObject().get("mem_limit").getAsDouble());
        }

        if (container.getAsJsonObject().has("cpu_limit")) {
            containerMetrics.setOriginalCpuLimit(container.getAsJsonObject().get("cpu_limit").getAsDouble());
        }

        for (String runtime : applicationRecommendations.runtimesMap.keySet())
        {
            if (applicationRecommendations.runtimesMap.get(runtime).contains(containerMetrics.getLabelName()))
            {
                containerMetrics.setRuntime(runtime);
            }
        }

        return containerMetrics;
    }
}
