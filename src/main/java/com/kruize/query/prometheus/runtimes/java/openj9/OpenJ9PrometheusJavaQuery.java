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

package com.kruize.query.prometheus.runtimes.java.openj9;

import com.kruize.query.prometheus.runtimes.java.openj9.heap.PrometheusBalancedHeapQuery;
import com.kruize.query.prometheus.runtimes.java.openj9.heap.PrometheusGenconHeapQuery;
import com.kruize.query.prometheus.runtimes.java.openj9.heap.PrometheusMetronomeHeapQuery;
import com.kruize.query.prometheus.runtimes.java.openj9.heap.PrometheusNoGcHeapQuery;
import com.kruize.query.runtimes.java.JavaQuery;
import com.kruize.recommendations.runtimes.java.JavaRecommendations;
import com.kruize.recommendations.runtimes.java.openj9.OpenJ9JavaRecommendations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class OpenJ9PrometheusJavaQuery extends JavaQuery
{
    private static final Set<String> noGcHeap =
            new HashSet<>(Collections.singletonList("tenured"));

    private static final Set<String> metronomeHeap =
            new HashSet<>(Collections.singletonList("JavaHeap"));

    private static final Set<String> genConHeap =
            new HashSet<>(Arrays.asList("tenured-LOA", "tenured-SOA", "nursery-survivor", "nursery-allocate"));

    private static final Set<String> balancedHeap =
            new HashSet<>(Arrays.asList("balanced-old", "balanced-eden", "balanced-survivor", "balanced-reserved"));

    public OpenJ9PrometheusJavaQuery(String gcPolicy)
    {
        vm = "OpenJ9";
        this.gcPolicy = gcPolicy;
        nonHeapQuery = new OpenJ9PrometheusNonHeapQuery();

        switch (gcPolicy) {
            case "gencon":
                heapQuery = new PrometheusGenconHeapQuery();
                break;
            case "balanced":
                heapQuery = new PrometheusBalancedHeapQuery();
                break;
            case "metronome":
                heapQuery = new PrometheusMetronomeHeapQuery();
                break;
            case "nogc":
                heapQuery = new PrometheusNoGcHeapQuery();
        }
    }

    /**
     * Each GC policy is associated with specific areas of heap.
     * Returns the set GC policy of the application based on the area
     *
     * @param areaOfHeap Area of heap of an application
     * @return String containing the GC policy of the application
     */
    public static String getGcPolicyForHeap(String areaOfHeap)
    {
        if (genConHeap.contains(areaOfHeap))
            return "gencon";

        if (balancedHeap.contains(areaOfHeap))
            return "balanced";

        if (metronomeHeap.contains(areaOfHeap))
            return "metronome";

        if (noGcHeap.contains(areaOfHeap))
            return "nogc";

        return null;
    }

    @Override
    public JavaRecommendations getJavaRecommendations()
    {
        return new OpenJ9JavaRecommendations();
    }
}

