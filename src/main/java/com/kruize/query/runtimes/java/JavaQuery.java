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

package com.kruize.query.runtimes.java;

import com.kruize.environment.DeploymentInfo;
import com.kruize.exceptions.InvalidValueException;
import com.kruize.query.prometheus.runtimes.java.openj9.OpenJ9JavaQuery;

import java.util.HashMap;
import java.util.Map;

public class JavaQuery
{
    public String vm = null;
    public String gcPolicy = null;
    public HeapQuery heapQuery = null;
    public NonHeapQuery nonHeapQuery = null;

    private static String podLabel = null;
    private static Map<String, String> getJavaAppsQuery = null;

    static {
        if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER"))
        {
            podLabel = "job";
        } else if (DeploymentInfo.getKubernetesType().toUpperCase().equals("OPENSHIFT")
                || DeploymentInfo.getKubernetesType().toUpperCase().equals("MINIKUBE")) {
            podLabel = "pod";
        } else {
            podLabel = "kubernetes_name";
        }

        getJavaAppsQuery = new HashMap<>();

        getJavaAppsQuery.put("spring_actuator", "jvm_memory_used_bytes{area=\"heap\"}");
        getJavaAppsQuery.put("quarkus", "vendor_memoryPool_usage_bytes");
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
        return null;
    }

    /**
     * @return String for a generic Java query that will fetch all applications exporting Java metrics
     */
    public Map<String, String> fetchJavaAppsQuery() throws InvalidValueException
    {
        if (DeploymentInfo.getMonitoringAgent().toUpperCase().equals("PROMETHEUS"))
            return getJavaAppsQuery;

        throw new InvalidValueException("JavaQuery not supported");
    }

    /**
     * @param areaOfHeap
     * @return JavaQuery supporting the VM-GC policy of the application
     * @throws InvalidValueException if not supported
     */
    public static JavaQuery getInstance(String areaOfHeap) throws InvalidValueException
    {
        String gcPolicy = null;

        System.out.println("Area of heap is: " + areaOfHeap);
        if (DeploymentInfo.getMonitoringAgent().toUpperCase().equals("PROMETHEUS"))
        {
            if ((gcPolicy = OpenJ9JavaQuery.getGcPolicyForHeap(areaOfHeap)) != null)
            {
                return new OpenJ9JavaQuery(gcPolicy, podLabel);
            }
        }

        throw new InvalidValueException("JavaQuery not supported");
    }

    public String getVm()
    {
        return vm;
    }

    public String getGcPolicy()
    {
        return gcPolicy;
    }

    public static String getPodLabel() { return podLabel; }
}
