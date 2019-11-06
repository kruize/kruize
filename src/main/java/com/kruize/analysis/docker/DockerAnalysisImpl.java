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

package com.kruize.analysis.docker;

import com.kruize.analysis.Analysis;
import com.kruize.metrics.ContainerMetrics;
import com.kruize.metrics.MetricCollector;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DockerAnalysisImpl implements Analysis<ContainerMetrics>
{
    private DockerAnalysisImpl() { }

    private static DockerAnalysisImpl dockerAnalysis = null;

    static {
        getInstance();
    }

    public static DockerAnalysisImpl getInstance()
    {
        if (dockerAnalysis == null)
            dockerAnalysis = new DockerAnalysisImpl();

        return dockerAnalysis;
    }

    @Override
    public void calculateCpuLimit(ContainerMetrics container)
    {
        double maxCpu = 0;
        final double BUFFER = 1.15;

        ArrayList<MetricCollector> metrics = container.metricCollector;

        if (metrics.size() == 0) {
            container.setCurrentCpuLimit(-1);
            return;
        }

        for (MetricCollector metricCollector : metrics)
        {
            double cpu = metricCollector.getFromIndex(MetricCollector.CPU_INDEX);
            if (maxCpu < cpu) {
                maxCpu = cpu;
            }
        }

        double cpuLimit = maxCpu * BUFFER;
        DecimalFormat singleDecimalPlace = new DecimalFormat("#.#");

        cpuLimit = Double.parseDouble(singleDecimalPlace.format(cpuLimit));
        container.setCurrentCpuLimit(cpuLimit);
    }

    @Override
    public void calculateMemLimit(ContainerMetrics container)
    {
        double spike;
        double maxMem = 0;
        final double BUFFER = 1.2;

        ArrayList<Double> rssValues = new ArrayList<>();
        ArrayList<MetricCollector> metrics = container.metricCollector;

        if (metrics.size() == 0) {
            container.setCurrentRssLimit(-1);
            return;
        }

        for (MetricCollector metricCollector : metrics)
        {
            double mem = metricCollector.getFromIndex(MetricCollector.CPU_INDEX);
            rssValues.add(mem);
            if (maxMem < mem)
                maxMem = mem;
        }

        spike = Analysis.getLargestSpike(rssValues);
        System.out.println("Spike for " + MetricCollector.CPU_INDEX + " is " + spike + "\n\n");

        double memRequests = container.getRssRequests();

        // If spike is very low
        double memLimit = Math.max(memRequests + spike, maxMem * BUFFER);
        container.setCurrentRssLimit(memLimit);
    }

    @Override
    public void calculateCpuRequests(ContainerMetrics container)
    {
        // Docker cannot enforce cpu requests
        double cpuRequests = -1;
        container.setCurrentCpuRequests(cpuRequests);
    }

    @Override
    public void calculateMemRequests(ContainerMetrics container, int referenceIndex, int targetIndex)
    {
        // Docker cannot enforce memory requests
        double memRequests = -1;
        container.setCurrentRssRequests(memRequests);
    }

    @Override
    public void finalizeY2DRecommendations(ContainerMetrics container)
    {
        double currentCpuLimit = container.getCurrentCpuLimit();
        double currentCpuRequests = container.getCurrentCpuRequests();
        double currentRssLimit = container.getCurrentRssLimit();
        double currentRssRequests = container.getCurrentRssRequests();

        container.setRssLimit(Math.max(container.getRssLimits(), currentRssLimit));
        container.setRssRequests(Math.max(container.getRssRequests(), currentRssRequests));
        container.setCpuLimit(Math.max(container.getCpuLimit(), currentCpuLimit));
        container.setCpuRequests(Math.max(container.getCpuRequests(), currentCpuRequests));
    }
}
