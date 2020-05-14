/*
 *
 */

package com.kruize.query.prometheus.kubernetes.openshift.runtimes.java.openj9;

import com.kruize.exceptions.InvalidValueException;
import com.kruize.query.prometheus.kubernetes.openshift.runtimes.java.openj9.heap.OpenshiftBalancedHeapQuery;
import com.kruize.query.prometheus.kubernetes.openshift.runtimes.java.openj9.heap.OpenshiftGenconHeapQuery;
import com.kruize.query.runtimes.java.JavaQuery;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class OpenJ9JavaQuery extends JavaQuery
{
    private static final Set<String> noGcHeap =
            new HashSet<>(Collections.singletonList("tenured"));

    private static final Set<String> metronomeHeap =
            new HashSet<>(Collections.singletonList("JavaHeap"));

    private static final Set<String> genConHeap =
            new HashSet<>(Arrays.asList("tenured-LOA", "tenured-SOA", "nursery-survivor", "nursery-allocate"));

    private static final Set<String> balancedHeap =
            new HashSet<>(Arrays.asList("balanced-old", "balanced-eden", "balanced-survivor", "balanced-reserved"));

    /**
     * @return Generic Java query that will fetch all applications exporting Java metrics
     */
    @Override
    public String fetchAppsQuery() throws InvalidValueException
    {
        return "jvm_memory_used_bytes{area=\"heap\"}";
    }

    public OpenJ9JavaQuery(String gcPolicy)
    {
        vm = "OpenJ9";
        this.gcPolicy = gcPolicy;
        nonHeapQuery = new OpenJ9NonHeapQuery();

        switch (gcPolicy) {
            case "gencon":
                heapQuery = new OpenshiftGenconHeapQuery();
                break;
            case "balanced":
                heapQuery = new OpenshiftBalancedHeapQuery();
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
}
