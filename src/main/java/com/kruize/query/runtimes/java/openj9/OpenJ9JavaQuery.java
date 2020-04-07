/*
 *
 */

/*
 *
 */

package com.kruize.query.runtimes.java.openj9;

public class OpenJ9JavaQuery
{
    public final static String getAppsQuery = "jvm_memory_used_bytes{area=\"heap\",id=\"tenured-SOA\"}";

    /* TODO Add checks for area */
    public HeapQuery heapQuery = new HeapQuery();
    public NonHeapQuery nonHeapQuery = new NonHeapQuery();
}

