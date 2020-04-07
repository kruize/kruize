/*
 *
 */

/*
 *
 */

package com.kruize.query.runtimes.java.openj9;

public class HeapQuery
{
    public String getTenuredLOA(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"tenured-LOA\"," +
                "kubernetes_name=\"" + name + "}";
    }

    public String getTenuredSOA(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"tenured-SOA\"," +
                "kubernetes_name=\"" + name + "}";
    }

    public String getNurserySurvivor(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"nursery-survivor\"," +
                "kubernetes_name=\"" + name + "}";
    }

    public String getNurseryAllocate(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"nursery-allocate\"," +
                "kubernetes_name=\"" + name + "}";
    }

}
