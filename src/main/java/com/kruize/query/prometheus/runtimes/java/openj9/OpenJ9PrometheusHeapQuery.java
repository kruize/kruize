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
import com.kruize.query.runtimes.java.HeapQuery;

public class OpenJ9PrometheusHeapQuery implements HeapQuery
{
    private final String[] partsOfHeap = {"tenured-LOA", "tenured-SOA", "nursery-survivor", "nursery-allocate"};

    @Override
    public String[] getPartsOfHeap()
    {
        return partsOfHeap;
    }

    @Override
    public String getHeapQuery(String application, String partOfHeap, String area) throws InvalidValueException
    {
        switch (partOfHeap) {
            case "tenured-LOA":
                return getTenuredLOA(area, application);
            case "tenured-SOA":
                return getTenuredSOA(area, application);
            case "nursery-survivor":
                return getNurserySurvivor(area, application);
            case "nursery-allocate":
                return getNurseryAllocate(area, application);
            default:
                throw new InvalidValueException("No " + partOfHeap + " present in heap");
        }
    }

    public String getTenuredLOA(String area, String name)
    {
        if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
            return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"tenured-LOA\"," +
                    "job=\"" + name + "\"}";
        }

        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"tenured-LOA\"," +
                "kubernetes_name=\"" + name + "\"}";
    }

    public String getTenuredSOA(String area, String name)
    {
        if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
            return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"tenured-SOA\"," +
                    "job=\"" + name + "\"}";
        }

        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"tenured-SOA\"," +
                "kubernetes_name=\"" + name + "\"}";
    }

    public String getNurserySurvivor(String area, String name)
    {
        if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
            return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"nursery-survivor\"," +
                    "job=\"" + name + "\"}";
        }

        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"nursery-survivor\"," +
                "kubernetes_name=\"" + name + "\"}";
    }

    public String getNurseryAllocate(String area, String name)
    {
        if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
            return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"nursery-allocate\"," +
                    "job=\"" + name + "\"}";
        }

        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"nursery-allocate\"," +
                "kubernetes_name=\"" + name + "\"}";
    }

}
