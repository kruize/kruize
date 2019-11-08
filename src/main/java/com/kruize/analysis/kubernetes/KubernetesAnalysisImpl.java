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

import com.kruize.analysis.AnalysisImpl;
import com.kruize.analysis.Statistics;
import com.kruize.metrics.MetricCollector;
import com.kruize.metrics.PodMetrics;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class KubernetesAnalysisImpl extends AnalysisImpl<PodMetrics>
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
}
