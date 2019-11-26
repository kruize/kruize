/*******************************************************************************
 * Copyright (c) 2019, 2019 IBM Corporation and others.
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

package com.kruize.metrics;

import java.util.ArrayList;

public abstract class AbstractMetrics implements Metrics
{
    String name;
    String namespace;
    String status;

    private double originalMemoryLimit = 0;
    private double originalMemoryRequests = 0;

    private double originalCpuLimit = 0;
    private double originalCpuRequests = 0;

    public ArrayList<MetricCollector> metricCollector = new ArrayList<>();

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    @Override
    public String getStatus()
    {
        return status;
    }

    @Override
    public void setStatus(String status)
    {
        this.status = status;
    }

    public double getOriginalMemoryRequests()
    {
        return originalMemoryRequests;
    }

    public void setOriginalMemoryRequests(double originalMemoryRequests)
    {
        this.originalMemoryRequests = originalMemoryRequests;
    }

    public double getOriginalMemoryLimit()
    {
        return originalMemoryLimit;
    }

    public void setOriginalMemoryLimit(double originalMemoryLimit)
    {
        this.originalMemoryLimit = originalMemoryLimit;
    }

    public double getOriginalCpuRequests()
    {
        return originalCpuRequests;
    }

    public void setOriginalCpuRequests(double originalCpuRequests)
    {
        this.originalCpuRequests = originalCpuRequests;
    }

    public double getOriginalCpuLimit()
    {
        return originalCpuLimit;
    }

    public void setOriginalCpuLimit(double originalCpuLimit)
    {
        this.originalCpuLimit = originalCpuLimit;
    }
}
