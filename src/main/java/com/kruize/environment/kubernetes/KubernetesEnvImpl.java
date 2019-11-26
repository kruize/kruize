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
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
                        addPodForMonitoring(pod);
                    }
                } catch (NullPointerException ignored) { }
            }
        } else {
            System.out.println("Looks like you do not have RBAC permissions to list pods.");
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    private void addPodForMonitoring(V1Pod pod)
    {
        String podName = pod.getMetadata().getName();
        String applicationName = PodMetrics.getApplicationName(pod);

        if (applicationRecommendations.applicationMap.containsKey(applicationName)) {
            applicationRecommendations.addMetricToApplication(applicationName, podName);
        } else {
            PodMetrics podMetrics = new PodMetrics(pod);
            ArrayList<PodMetrics> podMetricsArrayList = new ArrayList<>();
            podMetricsArrayList.add(podMetrics);
            applicationRecommendations.applicationMap.put(applicationName, podMetricsArrayList);
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
        catch (MalformedURLException | NullPointerException ignored) { }
    }
}
