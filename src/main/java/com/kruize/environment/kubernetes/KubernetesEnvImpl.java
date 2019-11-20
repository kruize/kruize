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

import com.kruize.environment.DeploymentInfo;
import com.kruize.analysis.kubernetes.KubernetesAnalysisImpl;
import com.kruize.environment.EnvTypeImpl;
import com.kruize.environment.SupportedTypes;
import com.kruize.exceptions.MonitoringAgentMissingException;
import com.kruize.exceptions.MonitoringAgentNotSupportedException;
import com.kruize.metrics.PodMetrics;
import com.kruize.query.PrometheusQuery;
import com.kruize.recommendations.application.KubernetesApplicationRecommendations;
import com.kruize.util.HttpUtil;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class KubernetesEnvImpl extends EnvTypeImpl
{
    @Override
    public void setupMonitoringAgent()
    {
        try {

            if (DeploymentInfo.getMonitoringAgentEndpoint() == null
                    || DeploymentInfo.getMonitoringAgentEndpoint().equals(""))
            {
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
        this.applicationRecommendations = KubernetesApplicationRecommendations.getInstance();
    }

    @Override
    public void setupAnalysis()
    {
        this.analysis = KubernetesAnalysisImpl.getInstance();
    }

    @Override
    public void setupQuery()
    {
        this.query = PrometheusQuery.getInstance();
    }

    @Override
    public void getAllApps()
    {
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
                        insertPodMetrics(pod);
                    }
                } catch (NullPointerException ignored) { }
            }
        } else {
            System.out.println("Looks like you do not have RBAC permissions to list pods.");
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    private void insertPodMetrics(V1Pod pod)
    {
        PodMetrics podMetrics = getPodMetrics(pod);
        String applicationName = podMetrics.getApplicationName();

        if (applicationRecommendations.applicationMap.containsKey(applicationName)) {
            applicationRecommendations.addMetricToApplication(applicationName, podMetrics);
        } else {
            ArrayList<PodMetrics> podMetricsArrayList = new ArrayList<>();
            podMetricsArrayList.add(podMetrics);
            applicationRecommendations.applicationMap.put(applicationName, podMetricsArrayList);
        }
    }

    private static PodMetrics getPodMetrics(V1Pod pod)
    {
        PodMetrics podMetrics = new PodMetrics();
        podMetrics.setName(pod.getMetadata().getName());
        podMetrics.setNamespace(pod.getMetadata().getNamespace());
        podMetrics.setStatus(pod.getStatus().getPhase());

        try {
            String podHashLabel = "pod-template-hash";
            String podTemplateHash = pod.getMetadata()
                    .getLabels().get(podHashLabel);

            podMetrics.setPodTemplateHash(podTemplateHash);
        } catch (NullPointerException e) {
            podMetrics.setPodTemplateHash(null);
        }

        String applicationName = parseApplicationName(pod.getMetadata().getName(),
                podMetrics.getPodTemplateHash());

        podMetrics.setApplicationName(applicationName);

        V1ResourceRequirements resources = pod.getSpec().getContainers().get(0).getResources();

        Map podRequests = resources.getRequests();
        Map podLimits = resources.getLimits();

        if (podRequests != null) {
            if (podRequests.containsKey("memory")) {
                Quantity memoryRequests = (Quantity) podRequests.get("memory");
                System.out.println(memoryRequests.getNumber().doubleValue());
                podMetrics.setOriginalMemoryRequests(memoryRequests.getNumber().doubleValue());
            }

            if (podRequests.containsKey("cpu")) {
                Quantity cpuRequests = (Quantity) podRequests.get("cpu");
                podMetrics.setOriginalCpuRequests(cpuRequests.getNumber().doubleValue());
            }
        }

        if (podLimits != null) {
            if (podLimits.containsKey("memory")) {
                Quantity memoryLimit = (Quantity) podLimits.get("memory");
                podMetrics.setOriginalMemoryLimit(memoryLimit.getNumber().doubleValue());
            }

            if (podLimits.containsKey("cpu")) {
                Quantity cpuLimit = (Quantity) podLimits.get("cpu");
                podMetrics.setOriginalCpuLimit(cpuLimit.getNumber().doubleValue());
            }
        }

        return podMetrics;
    }

    private static String parseApplicationName(String podName, String hash)
    {
        if (hash != null) {
            return podName.substring(0, podName.indexOf(hash) - 1);
        } else {
            return parseApplicationNameFromInstanceName(podName);
        }
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
        try {
            URL labelURL = new URL(DeploymentInfo.getMonitoringAgentEndpoint() + "/api/v1/labels");
            String result = HttpUtil.getDataFromURL(labelURL);
            PrometheusQuery prometheusQuery = PrometheusQuery.getInstance();

            if (result.contains("\"pod\"")) {
                prometheusQuery.setPodLabel("pod");
                prometheusQuery.setContainerLabel("container");
            } else {
                prometheusQuery.setPodLabel("pod_name");
                prometheusQuery.setContainerLabel("container_name");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
