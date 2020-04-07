/*
 *
 */

/*
 *
 */

package com.kruize.query.runtimes.java.openj9;

public class NonHeapQuery
{
    public String getMiscellaneous(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"miscellaneous%20non-heap%20storage\"," +
                "kubernetes_name=\"" + name + "}";
    }

    public String getClassStorage(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"class%20storage\"," +
                "kubernetes_name=\"" + name + "}";
    }

    public String getJitCodeCache(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"JIT%20code%20cache\"," +
                "kubernetes_name=\"" + name + "}";
    }

    public String getJitDataCache(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"JIT%20data%20cache\"," +
                "kubernetes_name=\"" + name + "}";
    }
}
