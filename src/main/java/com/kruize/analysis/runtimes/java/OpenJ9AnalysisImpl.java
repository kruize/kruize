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

    /**
     * Get the heap recommendation for an instance
     *
     * @param metrics instance of the application
     */
    public static void analyseHeapRecommendation(MetricsImpl metrics)
    {
        for (String application : JavaApplicationMetricsImpl.javaApplicationMetricsMap.keySet()) {
            double maxHeap = 0;

            for (JavaMetricCollector metricCollector :
                    JavaApplicationMetricsImpl.javaApplicationMetricsMap.get(application))
            {
                OpenJ9MetricCollector openJ9MetricCollector = (OpenJ9MetricCollector) metricCollector;
                maxHeap = (maxHeap > openJ9MetricCollector.getHeap()) ? maxHeap : openJ9MetricCollector.getHeap();
            }

            double heapRecommendation = maxHeap;

            /*
             If heap recommendation is greater than memory requests recommendation.
             This might happen because java VMs can only look at the limit value set, and can possibly
             expand beyond the request recommendation.
            */
            if (MathUtil.bytesToMB(heapRecommendation) > metrics.getRssRequests())
            {
                double MbToBytes = 1000*1000;
                double seventyPercent = 0.7;
                heapRecommendation = seventyPercent * metrics.getRssRequests() * MbToBytes;
            }

            JavaApplicationMetricsImpl.javaApplicationInfoMap
                    .get(application)
                    .getJavaRecommendations()
                    .setHeapRecommendation(heapRecommendation);

            LOGGER.info("Heap recommendation for {} is {}MB", application,
                    MathUtil.bytesToMB(heapRecommendation));

        }
    }

    /**
     * Get the non-heap recommendation for an instance
     *
     * @param metrics instance of the application
     */
    public static void analyseNonHeapRecommendation(MetricsImpl metrics)
    {
        for (String application : JavaApplicationMetricsImpl.javaApplicationMetricsMap.keySet()) {
            double maxNonHeap = 0;

            for (JavaMetricCollector metricCollector :
                    JavaApplicationMetricsImpl.javaApplicationMetricsMap.get(application))
            {
                OpenJ9MetricCollector openJ9MetricCollector = (OpenJ9MetricCollector) metricCollector;
                maxNonHeap = (maxNonHeap > openJ9MetricCollector.getNonHeap()) ? maxNonHeap : openJ9MetricCollector.getNonHeap();
            }

            double nonHeapRecommendation = maxNonHeap;
            
            JavaApplicationMetricsImpl.javaApplicationInfoMap
                    .get(application)
                    .getJavaRecommendations()
                    .setNonHeapRecommendation(nonHeapRecommendation);

            LOGGER.info("Non-heap recommendation for {} is {}MB", application,
                    MathUtil.bytesToMB(nonHeapRecommendation));

        }
    }
}
