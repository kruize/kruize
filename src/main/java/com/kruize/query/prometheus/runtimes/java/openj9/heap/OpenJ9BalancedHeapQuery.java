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

package com.kruize.query.prometheus.runtimes.java.openj9.heap;

import com.kruize.query.runtimes.java.openj9.heap.BalancedHeapQuery;

public class OpenJ9BalancedHeapQuery extends BalancedHeapQuery
{
    private String podLabel = null;

    public OpenJ9BalancedHeapQuery(String podLabel)
    {
        this.podLabel = podLabel;
    }

    @Override
    public String getBalancedOld(String area, String dataSource, String name)
    {
        if (dataSource.equals("spring_actuator")) {
            return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"balanced-old\"," +
                    podLabel + "=\"" + name + "\"}";
        } else if (dataSource.equals("quarkus")){
            return "vendor_memoryPool_usage_bytes{name=\"" + "balanced-old" + "\"," +
                    podLabel + "=\"" + name + "\"}";
        }

        return null;
    }

    @Override
    public String getBalancedEden(String area, String dataSource, String name)
    {

        if (dataSource.equals("spring_actuator")) {
            return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"balanced-eden\"," +
                    podLabel + "=\"" + name + "\"}";
        } else if (dataSource.equals("quarkus")){
            return "vendor_memoryPool_usage_bytes{name=\"" + "balanced-eden" + "\"," +
                    podLabel + "=\"" + name + "\"}";
        }

        return null;
    }

    @Override
    public String getBalancedSurvivor(String area, String dataSource, String name)
    {
        if (dataSource.equals("spring_actuator")) {
            return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"balanced-survivor\"," +
                    podLabel + "=\"" + name + "\"}";
        } else if (dataSource.equals("quarkus")){
            return "vendor_memoryPool_usage_bytes{name=\"" + "balanced-survivor" + "\"," +
                    podLabel + "=\"" + name + "\"}";
        }

        return null;

    }

    @Override
    public String getBalancedReserved(String area, String dataSource, String name)
    {
        if (dataSource.equals("spring_actuator")) {
            return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"balanced-reserved\"," +
                    podLabel + "=\"" + name + "\"}";
        } else if (dataSource.equals("quarkus")){
            return "vendor_memoryPool_usage_bytes{name=\"" + "balanced-reserved" + "\"," +
                    podLabel + "=\"" + name + "\"}";
        }

        return null;
    }
}
