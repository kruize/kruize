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

package com.kruize.analysis.runtimes.java;

import com.kruize.metrics.MetricsImpl;
import com.kruize.metrics.runtimes.java.JavaApplicationMetricsImpl;
import com.kruize.metrics.runtimes.java.JavaMetricCollector;
import com.kruize.metrics.runtimes.java.openj9.OpenJ9MetricCollector;
import com.kruize.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenJ9AnalysisImpl
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenJ9AnalysisImpl.class);

    public static void analyseHeapRecommendation(MetricsImpl metrics)
    {
        for (String application : JavaApplicationMetricsImpl.javaApplicationMetricsMap.keySet()) {
            double heapSum = 0;

            for (JavaMetricCollector metricCollector :
                    JavaApplicationMetricsImpl.javaApplicationMetricsMap.get(application))
            {
                OpenJ9MetricCollector openJ9MetricCollector = (OpenJ9MetricCollector) metricCollector;
                heapSum += openJ9MetricCollector.getHeap();
            }

            double numberOfMetrics = JavaApplicationMetricsImpl.javaApplicationMetricsMap
                    .get(application).size();
            double heapRecommendation = heapSum / numberOfMetrics;

            if (MathUtil.bytesToMB(heapRecommendation) > metrics.getRssRequests())
            {
                double MbToBytes = 1024*1024;
                heapRecommendation = 0.7 * metrics.getRssRequests() * MbToBytes;
            }

            JavaApplicationMetricsImpl.javaApplicationInfoMap
                    .get(application)
                    .getJavaRecommendations()
                    .setHeapRecommendation(heapRecommendation);

            LOGGER.info("Heap recommendation for {} is {}MB", application,
                    MathUtil.bytesToMB(heapRecommendation));

        }
    }

    public static void analyseNonHeapRecommendation(MetricsImpl metrics)
    {
        for (String application : JavaApplicationMetricsImpl.javaApplicationMetricsMap.keySet()) {
            double nonHeapSum = 0;

            for (JavaMetricCollector metricCollector :
                    JavaApplicationMetricsImpl.javaApplicationMetricsMap.get(application))
            {
                OpenJ9MetricCollector openJ9MetricCollector = (OpenJ9MetricCollector) metricCollector;
                nonHeapSum += openJ9MetricCollector.getNonHeap();
            }

            double numberOfMetrics = JavaApplicationMetricsImpl.javaApplicationMetricsMap
                    .get(application).size();
            double nonHeapRecommendation = nonHeapSum / numberOfMetrics;

            if (MathUtil.bytesToMB(nonHeapRecommendation) > metrics.getRssRequests())
            {
                double MbToBytes = 1024*1024;
                nonHeapRecommendation = 0.7 * metrics.getRssRequests() * MbToBytes;
            }

            JavaApplicationMetricsImpl.javaApplicationInfoMap
                    .get(application)
                    .getJavaRecommendations()
                    .setNonHeapRecommendation(nonHeapRecommendation);

            LOGGER.info("Non-heap recommendation for {} is {}MB", application,
                    MathUtil.bytesToMB(nonHeapRecommendation));

        }
    }
}
