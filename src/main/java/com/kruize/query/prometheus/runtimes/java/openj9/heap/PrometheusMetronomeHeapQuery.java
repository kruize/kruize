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

import com.kruize.environment.DeploymentInfo;
import com.kruize.query.runtimes.java.openj9.heap.MetronomeHeapQuery;

public class PrometheusMetronomeHeapQuery extends MetronomeHeapQuery
{
    @Override
    public String getJavaHeap(String area, String name)
    {
        if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
            return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"JavaHeap\"," +
                    "job=\"" + name + "\"}";
        }

        return "jvm_memory_" + area + "_bytes{area=\"heap\",id=\"JavaHeap\"," +
                "kubernetes_name=\"" + name + "\"}";
    }
}
