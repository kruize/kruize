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

package com.kruize.analysis;

import com.kruize.metrics.AbstractMetrics;
import com.kruize.metrics.MetricCollector;

import java.text.DecimalFormat;
import java.util.ArrayList;

public abstract class AnalysisImpl<T extends AbstractMetrics> implements Analysis<T> {
    public void calculateCpuLimit(T instance)
    {
        double maxCpu = 0;

        ArrayList<MetricCollector> metrics = instance.metricCollector;

        if (metrics.size() == 0) {
            instance.setCurrentCpuLimit(-1);
            return;
        }

        for (MetricCollector metricCollector : metrics) {
            double cpu = metricCollector.getFromIndex(MetricCollector.CPU_INDEX);
            if (maxCpu < cpu)
                maxCpu = cpu;
        }

        double cpuLimit = maxCpu * cpuBUFFER;
        DecimalFormat singleDecimalPlace = new DecimalFormat("#.#");

        cpuLimit = Double.parseDouble(singleDecimalPlace.format(cpuLimit));
        instance.setCurrentCpuLimit(cpuLimit);
    }

    public void calculateMemLimit(T instance)
    {
        double spike;
        double maxMem = 0;

        ArrayList<Double> rssValues = new ArrayList<>();
        ArrayList<MetricCollector> metrics = instance.metricCollector;

        if (metrics.size() == 0) {
            instance.setCurrentRssLimit(-1);
            return;
        }

        for (MetricCollector metricCollector : metrics) {
            double mem = metricCollector.getFromIndex(MetricCollector.CPU_INDEX);
            rssValues.add(mem);
            if (maxMem < mem)
                maxMem = mem;
        }

        spike = getLargestSpike(rssValues);
        System.out.println("Spike for " + MetricCollector.CPU_INDEX + " is " + spike + "\n\n");

        double memRequests = instance.getRssRequests();

        // If spike is very low
        double memLimit = Math.max(memRequests + spike, maxMem * memBUFFER);
        instance.setCurrentRssLimit(memLimit);
    }

    private static double getLargestSpike(ArrayList<Double> arrayList)
    {
        final double ONE_MB = 1024 * 1024;
        double largestSpike = 50 * ONE_MB;

        for (int i = 1; i < arrayList.size(); i++) {
            double difference = (arrayList.get(i) - arrayList.get(i - 1));
            if (difference > largestSpike)
                largestSpike = difference;
        }

        return largestSpike;
    }

    public void finalizeY2DRecommendations(T instance)
    {
        double currentCpuLimit = instance.getCurrentCpuLimit();
        double currentCpuRequests = instance.getCurrentCpuRequests();
        double currentRssLimit = instance.getCurrentRssLimit();
        double currentRssRequests = instance.getCurrentRssRequests();

        instance.setRssLimit(Math.max(instance.getRssLimits(), currentRssLimit));
        instance.setRssRequests(Math.max(instance.getRssRequests(), currentRssRequests));
        instance.setCpuLimit(Math.max(instance.getCpuLimit(), currentCpuLimit));
        instance.setCpuRequests(Math.max(instance.getCpuRequests(), currentCpuRequests));
    }
}
