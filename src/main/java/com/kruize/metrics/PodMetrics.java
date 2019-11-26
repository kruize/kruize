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

package com.kruize.metrics;

import com.kruize.recommendations.instance.PodRecommendations;
import com.kruize.recommendations.instance.Recommendations;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1ResourceRequirements;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PodMetrics extends AbstractMetrics
{

    private Recommendations y2dRecommendations = new PodRecommendations();
    private Recommendations currentRecommendations = new PodRecommendations();

    private PodMetrics() { }

    public PodMetrics(V1Pod pod)
    {
        PodMetrics podMetrics = new PodMetrics();
        podMetrics.setName(pod.getMetadata().getName());
        podMetrics.setNamespace(pod.getMetadata().getNamespace());
        podMetrics.setStatus(pod.getStatus().getPhase());

        String applicationName = parseApplicationName(pod.getMetadata().getName(),
                getPodTemplateHash(pod));

        podMetrics.setApplicationName(applicationName);

        setOriginalRequestsAndLimits(pod, podMetrics);
    }

    private static void setOriginalRequestsAndLimits(V1Pod pod, PodMetrics podMetrics)
    {
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
    }

    public static String parseApplicationName(String podName, String hash)
    {
        if (hash != null) {
            return podName.substring(0, podName.indexOf(hash) - 1);
        } else {
            return parseApplicationNameFromInstanceName(podName);
        }
    }

    private static String parseApplicationNameFromInstanceName(String podName)
    {
        Pattern pattern = Pattern.compile("-[a-zA-Z]*?\\d+");
        Matcher matcher = pattern.matcher(podName);

        if (matcher.find()) {
            int index = matcher.start();
            return podName.substring(0, index);
        }
        return podName;
    }

    public static String getPodTemplateHash(V1Pod pod)
    {
        try
        {
            String podHashLabel = "pod-template-hash";

            return pod.getMetadata()
                    .getLabels().get(podHashLabel);
        }
        catch (NullPointerException e) {
            return null;
        }
    }

    public double getCpuRequests()
    {
        return y2dRecommendations.getCpuRequest();
    }

    public double getCpuLimit()
    {
        return y2dRecommendations.getCpuLimit();
    }

    public double getRssRequests()
    {
        return y2dRecommendations.getRssRequest();
    }

    public double getRssLimits()
    {
        return y2dRecommendations.getRssLimit();
    }

    public void setCpuRequests(double value)
    {
        y2dRecommendations.setCpuRequest(value);
    }

    public void setRssRequests(double value)
    {
        y2dRecommendations.setRssRequest(value);
    }

    public void setCpuLimit(double value)
    {
        y2dRecommendations.setCpuLimit(value);
    }

    public void setRssLimit(double value)
    {
        y2dRecommendations.setRssLimit(value);
    }

    public void setCurrentCpuRequests(double value)
    {
        currentRecommendations.setCpuRequest(value);
    }

    public void setCurrentRssRequests(double value)
    {
        currentRecommendations.setRssRequest(value);
    }

    public void setCurrentCpuLimit(double value)
    {
        currentRecommendations.setCpuLimit(value);
    }

    public void setCurrentRssLimit(double value)
    {
        currentRecommendations.setRssLimit(value);
    }

    public double getCurrentCpuRequests()
    {
        return currentRecommendations.getCpuRequest();
    }

    public double getCurrentRssRequests()
    {
        return currentRecommendations.getRssRequest();
    }

    public double getCurrentCpuLimit()
    {
        return currentRecommendations.getCpuLimit();
    }

    public double getCurrentRssLimit()
    {
        return currentRecommendations.getRssLimit();
    }
}
