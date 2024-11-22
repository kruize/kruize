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

package com.kruize.environment.kubernetes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kruize.analysis.AnalysisImpl;
import com.kruize.environment.DeploymentInfo;
import com.kruize.environment.EnvTypeImpl;
import com.kruize.environment.SupportedTypes;
import com.kruize.exceptions.InvalidValueException;
import com.kruize.exceptions.MonitoringAgentMissingException;
import com.kruize.exceptions.MonitoringAgentNotSupportedException;
import com.kruize.metrics.MetricsImpl;
import com.kruize.metrics.runtimes.java.JavaApplicationInfo;
import com.kruize.metrics.runtimes.java.JavaApplicationMetricsImpl;
import com.kruize.query.prometheus.PrometheusQuery;
import com.kruize.query.runtimes.java.JavaQuery;
import com.kruize.recommendations.application.ApplicationRecommendationsImpl;
import com.kruize.recommendations.runtimes.java.openj9.OpenJ9JavaRecommendations;
import com.kruize.util.HttpUtil;
import com.kruize.util.MathUtil;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class KubernetesEnvImpl extends EnvTypeImpl
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesEnvImpl.class);

    @Override
    public void setupMonitoringAgent()
    {
        try {

            if (DeploymentInfo.getMonitoringAgentEndpoint() == null
                    || DeploymentInfo.getMonitoringAgentEndpoint().equals("")) {
                if (DeploymentInfo.getMonitoringAgentService() != null) {
                    getMonitoringEndpointFromService();
                } else {
                    throw new MonitoringAgentMissingException("ERROR: No service or endpoint specified");
                }
            }
            if (!checkMonitoringAgentSupported()) {
                throw new MonitoringAgentNotSupportedException();
            }
            if (!checkMonitoringAgentRunning()) {
                throw new MonitoringAgentMissingException(DeploymentInfo.getMonitoringAgent() + " not running");
            }
            setMonitoringLabels();
        } catch (MonitoringAgentNotSupportedException |
                MonitoringAgentMissingException |
                IOException |
                ApiException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private boolean checkMonitoringAgentRunning() throws IOException, ApiException
    {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();

        V1ServiceList serviceList = api.listServiceForAllNamespaces(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        for (V1Service service : serviceList.getItems()) {
            String serviceName = service.getMetadata().getName();
            if (serviceName.toUpperCase().contains(DeploymentInfo.getMonitoringAgent()))
                return true;
        }

        return false;
    }

    private boolean checkMonitoringAgentSupported()
    {
        String monitoring_agent = DeploymentInfo.getMonitoringAgent();
        return SupportedTypes.MONITORING_AGENTS_SUPPORTED.contains(monitoring_agent);
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
        ArrayList<String> monitoredInstances = new ArrayList<>();
        getRuntimeInfo();

        ApiClient apiClient = null;
        try {
            apiClient = Config.defaultClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Configuration.setDefaultApiClient(apiClient);
        CoreV1Api api = new CoreV1Api();

        //Get all the pods in the cluster
        V1PodList podList = null;
        try {
            podList = api.listPodForAllNamespaces(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        } catch (ApiException e) {
            e.printStackTrace();
        }

        if (podList != null) {
            for (V1Pod pod : podList.getItems()) {
                try {
                    final String label = "app.kubernetes.io/name";
                    boolean containsLabel = pod.getMetadata().getLabels().containsKey(label);
                    boolean isAppsodyApplication = pod.getKind() != null && pod.getKind().equals("AppsodyApplication");

                    if (containsLabel || isAppsodyApplication) {
                        insertMetrics(pod);
                        monitoredInstances.add(pod.getMetadata().getName());
                    }
                } catch (NullPointerException ignored) {
                }
            }
        } else {
            LOGGER.debug("Insufficient RBAC permissions (list, get, watch) for pods and services.");
            System.exit(1);
        }

        updateStatus(monitoredInstances);
    }

    /**
     * Check if the application being monitored has been terminated,
     * and update status if so
     * @param monitoredInstances Arraylist of all pods currently running that Kruize is monitoring
     */
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

            Map<String, String> javaAppsQueryMap = javaQuery.fetchJavaAppsQuery();
            for (String dataSource : javaAppsQueryMap.keySet())
            {
                JsonArray javaApps = getJsonArray(new URL(DeploymentInfo.getMonitoringAgentEndpoint()
                        + prometheusQuery.getAPIEndpoint() + javaAppsQueryMap.get(dataSource)));

                if (javaApps == null) return;

                for (JsonElement jsonElement : javaApps)
                {
                    JsonObject metric = jsonElement.getAsJsonObject().get("metric").getAsJsonObject();

                    String kubernetes_name;
                    String kubernetesType = DeploymentInfo.getKubernetesType().toUpperCase();

                    if (kubernetesType.equals("OPENSHIFT") || kubernetesType.equals("MINIKUBE")) {
                        kubernetes_name = metric.get("pod").getAsString();
                    } else {
                        kubernetes_name = metric.get("kubernetes_name").getAsString();
                    }

                    String heap_id;
                    if (dataSource.equals("spring_actuator")) {
                        heap_id = metric.get("id").getAsString();
                    } else {
                        heap_id = metric.get("name").getAsString();
                    }

                    try {
                        javaQuery = JavaQuery.getInstance(heap_id);
                    } catch (InvalidValueException e) {
                        continue;
                    }

                    /* Check if already in the list */
                    if (JavaApplicationMetricsImpl.javaApplicationInfoMap.containsKey(kubernetes_name))
                        continue;

                    if (!applicationRecommendations.runtimesMap.get("java").contains(kubernetes_name))
                    {
                        LOGGER.info("{} added to java runtime collection array", kubernetes_name);
                        applicationRecommendations.runtimesMap.get("java").add(kubernetes_name);

                        String vm = javaQuery.getVm();
                        LOGGER.info("VM is {}", vm);

                        if (vm.equals("OpenJ9"))
                        {
                            JavaApplicationMetricsImpl.javaApplicationInfoMap.put(
                                    kubernetes_name,
                                    new JavaApplicationInfo(
                                            vm,
                                            javaQuery.getGcPolicy(),
                                            dataSource,
                                            new OpenJ9JavaRecommendations()));
                        }
                    }
                }

            }
        } catch (Exception e) {
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

    private void insertMetrics(V1Pod pod)
    {
        MetricsImpl metricsImpl = null;
        try {
            metricsImpl = getPodMetrics(pod);
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }

        assert metricsImpl != null;
        String applicationName = metricsImpl.getApplicationName();

        if (applicationRecommendations.applicationMap.containsKey(applicationName)) {
            applicationRecommendations.addMetricToApplication(applicationName, metricsImpl);
        } else {
            ArrayList<MetricsImpl> podMetricsArrayList = new ArrayList<>();
            podMetricsArrayList.add(metricsImpl);
            applicationRecommendations.applicationMap.put(applicationName, podMetricsArrayList);
        }
    }

    private MetricsImpl getPodMetrics(V1Pod pod) throws InvalidValueException
    {
        MetricsImpl metrics = new MetricsImpl();
        metrics.setName(pod.getMetadata().getName());
        metrics.setNamespace(pod.getMetadata().getNamespace());
        metrics.setStatus(pod.getStatus().getPhase().toLowerCase());

        String podTemplateHash;

        try {
            String kubernetesType = DeploymentInfo.getKubernetesType().toUpperCase();

            if (kubernetesType.equals("OPENSHIFT") || kubernetesType.equals("MINIKUBE")) {
                metrics.setLabelName(metrics.getName());
            } else {
                metrics.setLabelName(pod.getMetadata()
                        .getLabels().get("name"));
            }

            String podHashLabel = "pod-template-hash";
            podTemplateHash = pod.getMetadata()
                    .getLabels().get(podHashLabel);

        } catch (NullPointerException e) {
            podTemplateHash = null;
        }

        for (String runtime : applicationRecommendations.runtimesMap.keySet())
        {
            if (applicationRecommendations.runtimesMap.get(runtime).contains(metrics.getLabelName()))
            {
                metrics.setRuntime(runtime);
            }
        }

        String applicationName = parseApplicationName(metrics.getName(),
                podTemplateHash);

        metrics.setApplicationName(applicationName);

        V1ResourceRequirements resources = pod.getSpec().getContainers().get(0).getResources();

        Map podRequests = resources.getRequests();
        Map podLimits = resources.getLimits();

        if (podRequests != null) {
            if (podRequests.containsKey("memory")) {
                Quantity memoryRequests = (Quantity) podRequests.get("memory");
                double memoryRequestsValue = memoryRequests.getNumber().doubleValue();
                LOGGER.debug("Original memory requests for {}: {} MB", applicationName,
                        MathUtil.bytesToMB(memoryRequestsValue));

                metrics.setOriginalMemoryRequests(memoryRequestsValue);
            }

            if (podRequests.containsKey("cpu")) {
                Quantity cpuRequests = (Quantity) podRequests.get("cpu");
                double cpuRequestsValue = cpuRequests.getNumber().doubleValue();
                LOGGER.debug("Original CPU requests for {}: {}", applicationName,
                        cpuRequestsValue);

                metrics.setOriginalCpuRequests(cpuRequests.getNumber().doubleValue());
            }
        }

        if (podLimits != null) {
            if (podLimits.containsKey("memory")) {
                Quantity memoryLimit = (Quantity) podLimits.get("memory");
                double memoryLimitValue = memoryLimit.getNumber().doubleValue();
                LOGGER.debug("Original memory limit for {}: {} MB", applicationName,
                        MathUtil.bytesToMB(memoryLimitValue));

                metrics.setOriginalMemoryLimit(memoryLimitValue);
            }

            if (podLimits.containsKey("cpu")) {
                Quantity cpuLimit = (Quantity) podLimits.get("cpu");
                double cpuLimitValue = cpuLimit.getNumber().doubleValue();
                LOGGER.debug("Original CPU limit for {}: {}", applicationName,
                        cpuLimitValue);

                metrics.setOriginalCpuLimit(cpuLimit.getNumber().doubleValue());
            }
        }

        return metrics;
    }

    private static String parseApplicationName(String podName, String hash)
    {
        if (hash != null) {
            int index = podName.indexOf(hash);
            if (index > 0)
                return podName.substring(0, index - 1);
        }
        return parseApplicationNameFromInstanceName(podName);
    }

    private void getMonitoringEndpointFromService() throws IOException, ApiException
    {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();

        V1ServiceList serviceList = api.listServiceForAllNamespaces(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        for (V1Service service : serviceList.getItems()) {
            String serviceName = service.getMetadata().getName();
            if (serviceName.toUpperCase().equals(DeploymentInfo.getMonitoringAgentService())) {
                String clusterIP = service.getSpec().getClusterIP();
                int port = service.getSpec().getPorts().get(0).getPort();
                DeploymentInfo.setMonitoringAgentEndpoint("http://" + clusterIP + ":" + port);
            }
        }
    }

    private void setMonitoringLabels()
    {
        PrometheusQuery prometheusQuery = PrometheusQuery.getInstance();

        try {
            URL labelURL = new URL(DeploymentInfo.getMonitoringAgentEndpoint() + "/api/v1/labels");
            String result = HttpUtil.getDataFromURL(labelURL);

            if (result.contains("\"pod\"")) {
                prometheusQuery.setPodLabel("pod");
                prometheusQuery.setContainerLabel("container");
            }
        }
        /* Use the default labels */
        catch (MalformedURLException | NullPointerException ignored) {
            LOGGER.info("Using default labels");
        }
    }
}
