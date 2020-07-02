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
    /*
        Triplet of rss, java heap and non-heap
     */
    private static class JavaTriplet
    {
        double rss;
        double heap;
        double nonHeap;

        JavaTriplet(double rss, double heap, double nonHeap)
        {
            this.rss = rss;
            this.heap = heap;
            this.nonHeap = nonHeap;
        }

        JavaTriplet()
        {
            this.rss = 0;
            this.heap = 0;
            this.nonHeap = 0;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenJ9AnalysisImpl.class);

    private static JavaTriplet heapMax = new JavaTriplet();
    private static JavaTriplet nonHeapMax = new JavaTriplet();

    /**
     * Get the heap recommendation for an instance
     *
     * @param metrics instance of the application
     */
    public static void analyseHeapRecommendation(MetricsImpl metrics)
    {
        for (String application : JavaApplicationMetricsImpl.javaApplicationMetricsMap.keySet())
        {
            for (JavaMetricCollector metricCollector :
                    JavaApplicationMetricsImpl.javaApplicationMetricsMap.get(application))
            {
                OpenJ9MetricCollector openJ9MetricCollector = (OpenJ9MetricCollector) metricCollector;

                if (openJ9MetricCollector.getHeap() > heapMax.heap)
                {
                    double rss = openJ9MetricCollector.getRss();
                    double heap = openJ9MetricCollector.getHeap();
                    double nonHeap = openJ9MetricCollector.getNonHeap();

                    heapMax = new JavaTriplet(rss, heap, nonHeap);
                }
            }

            double heapRecommendation = heapMax.heap;

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
        for (String application : JavaApplicationMetricsImpl.javaApplicationMetricsMap.keySet())
        {
            for (JavaMetricCollector metricCollector :
                    JavaApplicationMetricsImpl.javaApplicationMetricsMap.get(application))
            {
                OpenJ9MetricCollector openJ9MetricCollector = (OpenJ9MetricCollector) metricCollector;

                if (openJ9MetricCollector.getNonHeap() > nonHeapMax.nonHeap)
                {
                    double rss = openJ9MetricCollector.getRss();
                    double heap = openJ9MetricCollector.getHeap();
                    double nonHeap = openJ9MetricCollector.getNonHeap();

                    nonHeapMax = new JavaTriplet(rss, heap, nonHeap);
                }

            }

            double nonHeapRecommendation = nonHeapMax.nonHeap;

            JavaApplicationMetricsImpl.javaApplicationInfoMap
                    .get(application)
                    .getJavaRecommendations()
                    .setNonHeapRecommendation(nonHeapRecommendation);

            LOGGER.info("Non-heap recommendation for {} is {}MB", application,
                    MathUtil.bytesToMB(nonHeapRecommendation));

        }
    }

    /**
     * Use obtained heap and non-heap recommendations to get improved rss sizing recommendations,
     * which can in turn be used to improve requests sizing.
     *
     * @param metrics
     */
    public static void analyseRssMax(MetricsImpl metrics)
    {
        for (String application : JavaApplicationMetricsImpl.javaApplicationMetricsMap.keySet())
        {
            double rssMax = 0;
            double openJ9MemMax = 0;

            for (JavaMetricCollector javaMetricCollector :
                    JavaApplicationMetricsImpl.javaApplicationMetricsMap.get(application))
            {
                OpenJ9MetricCollector openJ9MetricCollector = (OpenJ9MetricCollector) javaMetricCollector;
                openJ9MemMax = Math.max(openJ9MemMax,
                        openJ9MetricCollector.getRss() -
                                (openJ9MetricCollector.getHeap() + openJ9MetricCollector.getNonHeap()));
            }

            rssMax = Math.max(heapMax.rss, nonHeapMax.rss);
            rssMax = Math.max(rssMax, (heapMax.heap + nonHeapMax.nonHeap + openJ9MemMax));

            JavaApplicationMetricsImpl.javaApplicationInfoMap
                    .get(application)
                    .getJavaRecommendations()
                    .setRssMax(rssMax);
        }
    }
}
