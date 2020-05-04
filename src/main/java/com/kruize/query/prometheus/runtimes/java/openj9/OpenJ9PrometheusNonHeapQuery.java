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

import com.kruize.environment.DeploymentInfo;
import com.kruize.exceptions.InvalidValueException;
import com.kruize.query.runtimes.java.NonHeapQuery;

public class OpenJ9PrometheusNonHeapQuery implements NonHeapQuery
{
    private final String[] partsOfNonHeap = {"miscellaneous", "class storage", "JIT code cache", "JIT data cache"};

    @Override
    public String[] getPartsOfNonHeap()
    {
        return partsOfNonHeap;
    }

    @Override
    public String getNonHeapQuery(String application, String partOfNonHeap, String area) throws InvalidValueException
    {
        switch (partOfNonHeap) {
            case "miscellaneous":
                return getMiscellaneous(area, application);
            case "class storage":
                return getClassStorage(area, application);
            case "JIT code cache":
                return getJitCodeCache(area, application);
            case "JIT data cache":
                return getJitDataCache(area, application);
            default:
                throw new InvalidValueException("No " + partOfNonHeap + " present in non-heap");
        }
    }

    public String getMiscellaneous(String area, String name)
    {
        if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
            return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"miscellaneous%20non-heap%20storage\"," +
                    "job=\"" + name + "\"}";
        } else if (DeploymentInfo.getKubernetesType().toUpperCase().equals("OPENSHIFT")) {
            return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"miscellaneous%20non-heap%20storage\"," +
                    "pod=\"" + name + "\"}";
        }

        return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"miscellaneous%20non-heap%20storage\"," +
                "kubernetes_name=\"" + name + "\"}";
    }

    public String getClassStorage(String area, String name)
    {
        if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
            return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"class%20storage\"," +
                    "job=\"" + name + "\"}";
        } else if (DeploymentInfo.getKubernetesType().toUpperCase().equals("OPENSHIFT")) {
            return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"class%20storage\"," +
                    "pod=\"" + name + "\"}";
        }

        return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"class%20storage\"," +
                "kubernetes_name=\"" + name + "\"}";
    }

    public String getJitCodeCache(String area, String name)
    {
        if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
            return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"JIT%20code%20cache\"," +
                    "job=\"" + name + "\"}";
        } else if (DeploymentInfo.getKubernetesType().toUpperCase().equals("OPENSHIFT")) {
            return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"JIT%20code%20cache\"," +
                    "pod=\"" + name + "\"}";
        }

        return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"JIT%20code%20cache\"," +
                "kubernetes_name=\"" + name + "\"}";
    }

    public String getJitDataCache(String area, String name)
    {
        if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
            return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"JIT%20data%20cache\"," +
                    "job=\"" + name + "\"}";
        } else if (DeploymentInfo.getKubernetesType().toUpperCase().equals("OPENSHIFT")) {
            return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"JIT%20data%20cache\"," +
                    "pod=\"" + name + "\"}";
        }

        return "jvm_memory_" + area + "_bytes{area=\"nonheap\",id=\"JIT%20data%20cache\"," +
                "kubernetes_name=\"" + name + "\"}";
    }
}
