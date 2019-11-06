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

package com.kruize.analysis.kubernetes;

import com.kruize.analysis.Analysis;
import com.kruize.analysis.Statistics;
import com.kruize.metrics.MetricCollector;
import com.kruize.metrics.PodMetrics;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class KubernetesAnalysisImpl implements Analysis<PodMetrics>
{
    private KubernetesAnalysisImpl() { }

    private static KubernetesAnalysisImpl kubernetesAnalysis = null;

    static {
        getInstance();
    }

    public static KubernetesAnalysisImpl getInstance()
    {
        if (kubernetesAnalysis == null)
            kubernetesAnalysis = new KubernetesAnalysisImpl();

        return kubernetesAnalysis;
    }

    public void calculateCpuLimit(PodMetrics pod)
    {
        double maxCpu = 0;
        final double BUFFER = 1.15;

        ArrayList<MetricCollector> metrics = pod.metricCollector;

        if (metrics.size() == 0) {
            pod.setCurrentCpuLimit(-1);
            return;
        }

        for (MetricCollector metricCollector : metrics) {
            double cpu = metricCollector.getFromIndex(MetricCollector.CPU_INDEX);
            if (maxCpu < cpu)
                maxCpu = cpu;
        }

        double cpuLimit = maxCpu * BUFFER;
        DecimalFormat singleDecimalPlace = new DecimalFormat("#.#");

        cpuLimit = Double.parseDouble(singleDecimalPlace.format(cpuLimit));
        pod.setCurrentCpuLimit(cpuLimit);
    }

    public void calculateMemLimit(PodMetrics pod)
    {
        double spike;
        double maxMem = 0;
        final double BUFFER = 1.2;

        ArrayList<Double> rssValues = new ArrayList<>();
        ArrayList<MetricCollector> metrics = pod.metricCollector;

        if (metrics.size() == 0) {
            pod.setCurrentRssLimit(-1);
            return;
        }

        for (MetricCollector metricCollector : metrics) {
            double mem = metricCollector.getFromIndex(MetricCollector.CPU_INDEX);
            rssValues.add(mem);
            if (maxMem < mem)
                maxMem = mem;
        }

        spike = Analysis.getLargestSpike(rssValues);
        System.out.println("Spike for " + MetricCollector.CPU_INDEX + " is " + spike + "\n\n");

        double memRequests = pod.getRssRequests();

        // If spike is very low
        double memLimit = Math.max(memRequests + spike, maxMem * BUFFER);
        pod.setCurrentRssLimit(memLimit);
    }

    public void calculateCpuRequests(PodMetrics pod)
    {
        final int PERCENTILE = 80;
        final int INDEX = MetricCollector.CPU_INDEX;

        ArrayList<MetricCollector> metrics = pod.metricCollector;
        ArrayList<MetricCollector> percentileList = new ArrayList<>();

        if (metrics.size() == 0) {
            pod.setCurrentCpuRequests(-1);
            return;
        }

        DescriptiveStatistics referenceValues = new DescriptiveStatistics();

        for (MetricCollector metric : metrics) {
            double value = metric.getFromIndex(INDEX);
            referenceValues.addValue(value);
            System.out.print(value + "\t");
        }

        double percentileValue = Statistics.getPercentile(referenceValues, PERCENTILE);
        System.out.println(PERCENTILE + "th percentile is " + percentileValue);

        for (MetricCollector metric : metrics) {
            if (metric.getFromIndex(INDEX) >= percentileValue) {
                MetricCollector temp = MetricCollector.Copy(metric);
                DecimalFormat singleDecimalPlace = new DecimalFormat("#.#");
                singleDecimalPlace.setRoundingMode(RoundingMode.CEILING);
                double targetValue = temp.getFromIndex(INDEX);
                temp.setForIndex((Double.parseDouble(singleDecimalPlace.format(targetValue))), INDEX);

                percentileList.add(temp);
                System.out.println(temp.toString());
            }
        }

        double cpuRequests = Statistics.getMode(percentileList, INDEX);
        pod.setCurrentCpuRequests(cpuRequests);
    }

    public void calculateMemRequests(PodMetrics pod, int referenceIndex, int targetIndex)
    {
        final int PERCENTILE = 80;
        final int ROUND_TO_MUL_OF = 5;

        ArrayList<MetricCollector> metrics = pod.metricCollector;
        ArrayList<MetricCollector> percentileList = new ArrayList<>();

        if (metrics.size() == 0) {
            pod.setCurrentRssRequests(-1);
            return;
        }

        DescriptiveStatistics referenceValues = new DescriptiveStatistics();

        for (MetricCollector metric : metrics) {
            double value = metric.getFromIndex(referenceIndex);
            referenceValues.addValue(value);
            System.out.print(value + "\t");
        }

        double percentileValue = Statistics.getPercentile(referenceValues, PERCENTILE);
        System.out.println(PERCENTILE + "th percentile is " + percentileValue);

        for (MetricCollector metric : metrics) {
            if (metric.getFromIndex(referenceIndex) >= percentileValue) {
                MetricCollector temp = MetricCollector.Copy(metric);
                temp.setForIndex(roundToNearestMultiple(temp.getFromIndex(targetIndex), ROUND_TO_MUL_OF), targetIndex);
                percentileList.add(temp);
                System.out.println(temp.toString());
            }
        }

        double memRequests = Statistics.getMode(percentileList, targetIndex);
        pod.setCurrentRssRequests(memRequests);
    }

    // function to round the number to multiple of 5
    @SuppressWarnings("SameParameterValue")
    private static double roundToNearestMultiple(double number, double multipleOf)
    {
        return multipleOf * (Math.ceil(Math.abs(number/multipleOf)));
    }

    public void finalizeY2DRecommendations(PodMetrics pod)
    {
        double currentCpuLimit = pod.getCurrentCpuLimit();
        double currentCpuRequests = pod.getCurrentCpuRequests();
        double currentRssLimit = pod.getCurrentRssLimit();
        double currentRssRequests = pod.getCurrentRssRequests();

        pod.setRssLimit(Math.max(pod.getRssLimits(), currentRssLimit));
        pod.setRssRequests(Math.max(pod.getRssRequests(), currentRssRequests));
        pod.setCpuLimit(Math.max(pod.getCpuLimit(), currentCpuLimit));
        pod.setCpuRequests(Math.max(pod.getCpuRequests(), currentCpuRequests));
    }
}
