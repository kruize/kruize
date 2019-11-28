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

import com.kruize.metrics.MetricCollector;
import com.kruize.metrics.MetricsImpl;
import com.kruize.util.MathUtil;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class AnalysisImpl implements Analysis
{
    private static AnalysisImpl analysis = null;

    private static final double ONE_MB = 1024 * 1024;
    private static final double DEFAULT_SPIKE = 50 * ONE_MB;
    private static final int CPU_PERCENTILE = 80;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisImpl.class);

    static {
        getInstance();
    }

    public static AnalysisImpl getInstance()
    {
        if (analysis == null)
            analysis = new AnalysisImpl();

        return analysis;
    }

    private AnalysisImpl() { }

    @Override
    public void calculateCpuLimit(MetricsImpl instance)
    {
        double maxCpu = 0;

        ArrayList<MetricCollector> metrics = instance.metricCollector;

        if (metrics.size() == 0) {
            instance.setCurrentCpuLimit(-1);
            LOGGER.info("{} is in idle state. Recommendations will be generated" +
                    " after it becomes active.", instance.getApplicationName());
            return;
        }

        for (MetricCollector metricCollector : metrics) {
            double cpu = metricCollector.getFromIndex(MetricCollector.CPU_INDEX);
            if (maxCpu < cpu)
                maxCpu = cpu;
        }

        double cpuLimit = maxCpu * cpuBuffer;
        DecimalFormat singleDecimalPlace = new DecimalFormat("#.#");

        cpuLimit = Double.parseDouble(singleDecimalPlace.format(cpuLimit));
        LOGGER.debug("CPU Limit for {} is {}", instance.getName(), cpuLimit);
        instance.setCurrentCpuLimit(cpuLimit);
    }

    @Override
    public void calculateCpuRequests(MetricsImpl instance)
    {
        final int INDEX = MetricCollector.CPU_INDEX;

        ArrayList<MetricCollector> metrics = instance.metricCollector;
        ArrayList<MetricCollector> percentileList = new ArrayList<>();

        if (metrics.size() == 0) {
            instance.setCurrentCpuRequests(-1);
            return;
        }

        DescriptiveStatistics referenceValues = new DescriptiveStatistics();

        for (MetricCollector metric : metrics) {
            double value = metric.getFromIndex(INDEX);
            referenceValues.addValue(value);
        }

        double percentileValue = MathUtil.getPercentile(referenceValues, CPU_PERCENTILE);

        LOGGER.debug("CPU values: {}", Arrays.toString(referenceValues.getValues()));
        LOGGER.debug("{}th percentile is {}", CPU_PERCENTILE, percentileValue);

        for (MetricCollector metric : metrics) {
            if (metric.getFromIndex(INDEX) >= percentileValue)
            {
                MetricCollector temp = MetricCollector.Copy(metric);

                DecimalFormat singleDecimalPlace = new DecimalFormat("#.#");
                singleDecimalPlace.setRoundingMode(RoundingMode.CEILING);

                double targetValue = temp.getFromIndex(INDEX);
                temp.setForIndex((Double.parseDouble(singleDecimalPlace.format(targetValue))), INDEX);

                percentileList.add(temp);
            }
        }

        double cpuRequests = MathUtil.getMode(percentileList, INDEX);
        LOGGER.debug("Current CPU Requests for {} is {}", instance.getName(), cpuRequests);
        instance.setCurrentCpuRequests(cpuRequests);
    }

    @Override
    public void calculateMemRequests(MetricsImpl instance, int referenceIndex, int targetIndex)
    {
        final int ROUND_TO_MUL_OF = 5;

        ArrayList<MetricCollector> metrics = instance.metricCollector;
        ArrayList<MetricCollector> percentileList = new ArrayList<>();

        if (metrics.size() == 0) {
            instance.setCurrentRssRequests(-1);
            return;
        }

        DescriptiveStatistics referenceValues = new DescriptiveStatistics();

        for (MetricCollector metric : metrics) {
            double value = metric.getFromIndex(referenceIndex);
            referenceValues.addValue(value);
        }

        double percentileValue = MathUtil.getPercentile(referenceValues, CPU_PERCENTILE);
        LOGGER.debug("{}th percentile is {}", CPU_PERCENTILE, percentileValue);

        for (MetricCollector metric : metrics) {
            if (metric.getFromIndex(referenceIndex) >= percentileValue)
            {
                MetricCollector temp = MetricCollector.Copy(metric);
                temp.setForIndex(roundToNearestMultiple(temp.getFromIndex(targetIndex), ROUND_TO_MUL_OF), targetIndex);
                percentileList.add(temp);
            }
        }

        LOGGER.debug("RSS values (CPU >= 80th percentile) : {}", Arrays.toString(percentileList.toArray()));

        double memRequests = MathUtil.getMode(percentileList, targetIndex);
        LOGGER.debug("Current Memory Requests for {} is {}", instance.getName(), memRequests);

        instance.setCurrentRssRequests(memRequests);
    }

    @Override
    public void calculateMemLimit(MetricsImpl instance)
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
            double mem = metricCollector.getFromIndex(MetricCollector.RSS_INDEX);
            rssValues.add(mem);
            if (maxMem < mem)
                maxMem = mem;
        }

        spike = getLargestSpike(rssValues);
        LOGGER.debug("Spike is {}" , spike);

        double memRequests = instance.getRssRequests();

        // If spike is very low
        double memLimit = Math.max(memRequests + spike, maxMem * memBuffer);
        LOGGER.debug("Current Memory Limit for {} is {}", instance.getName(), memLimit);
        instance.setCurrentRssLimit(memLimit);
    }

    private static double getLargestSpike(ArrayList<Double> arrayList)
    {
        double largestSpike = DEFAULT_SPIKE;

        for (int i = 1; i < arrayList.size(); i++) {
            double difference = (arrayList.get(i) - arrayList.get(i - 1));
            if (difference > largestSpike)
                largestSpike = difference;
        }

        return largestSpike;
    }

    @Override
    public void finalizeY2DRecommendations(MetricsImpl instance)
    {
        double currentCpuLimit = instance.getCurrentCpuLimit();
        double currentCpuRequests = instance.getCurrentCpuRequests();
        double currentRssLimit = instance.getCurrentRssLimit();
        double currentRssRequests = instance.getCurrentRssRequests();

        instance.setRssLimit(Math.max(instance.getRssLimits(), currentRssLimit));
        instance.setRssRequests(Math.max(instance.getRssRequests(), currentRssRequests));
        instance.setCpuLimit(Math.max(instance.getCpuLimit(), currentCpuLimit));
        instance.setCpuRequests(Math.max(instance.getCpuRequests(), currentCpuRequests));

        LOGGER.info("{}: CPU Limit = {}", instance.getName(), instance.getCpuLimit());
        LOGGER.info("{}: CPU Requests = {}", instance.getName(), instance.getCpuRequests());
        LOGGER.info("{}: Memory Limit = {} MB", instance.getName(),
                MathUtil.bytesToMB(instance.getRssLimits()));
        LOGGER.info("{}: Memory Requests = {} MB\n", instance.getName(),
                MathUtil.bytesToMB(instance.getRssRequests()));

    }

    // function to round the number to multiple of number specified
    @SuppressWarnings("SameParameterValue")
    private static double roundToNearestMultiple(double number, double multipleOf)
    {
        return multipleOf * (Math.ceil(Math.abs(number/multipleOf)));
    }


}
