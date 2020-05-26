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

import com.kruize.query.runtimes.java.openj9.heap.GenconHeapQuery;

public class OpenJ9GenconHeapQuery extends GenconHeapQuery
{
    private String podLabel = null;

    public OpenJ9GenconHeapQuery(String podLabel)
    {
        this.podLabel = podLabel;
    }

    @Override
    public String getTenuredLOA(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"tenured-LOA\"," +
                podLabel + "=\"" + name + "\"}";
    }

    @Override
    public String getTenuredSOA(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"tenured-SOA\"," +
                podLabel + "=\"" + name + "\"}";
    }

    @Override
    public String getNurserySurvivor(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"nursery-survivor\"," +
                podLabel + "=\"" + name + "\"}";
    }

    @Override
    public String getNurseryAllocate(String area, String name)
    {
        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"nursery-allocate\"," +
                podLabel + "=\"" + name + "\"}";
    }
}
