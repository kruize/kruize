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
import com.kruize.query.prometheus.kubernetes.openshift.runtimes.java.openj9.OpenshiftOpenJ9JavaQuery;
import com.kruize.query.runtimes.RuntimeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaQuery implements RuntimeQuery
{
    public String vm = null;
    public String gcPolicy = null;
    public HeapQuery heapQuery = null;
    public NonHeapQuery nonHeapQuery = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaQuery.class);


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
     * @return Generic Java query that will fetch all applications exporting Java metrics
     */
    public String fetchAppsQuery() throws InvalidValueException
    {
        if (DeploymentInfo.getMonitoringAgent().toUpperCase().equals("PROMETHEUS"))
            return "jvm_memory_used_bytes{area=\"heap\"}";

        throw new InvalidValueException("JavaQuery not supported");
    }

    public static JavaQuery getInstance(String areaOfHeap) throws InvalidValueException
    {
        String gcPolicy = null;

        if (DeploymentInfo.getMonitoringAgent().toUpperCase().equals("PROMETHEUS"))
        {
            if ((gcPolicy = OpenshiftOpenJ9JavaQuery.getGcPolicyForHeap(areaOfHeap)) != null)
            {
                LOGGER.info(gcPolicy);
                if (DeploymentInfo.getKubernetesType().toUpperCase().equals("OPENSHIFT"))
                    return new OpenshiftOpenJ9JavaQuery(gcPolicy);
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
}
