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

import com.kruize.recommendations.instance.Recommendations;
import com.kruize.recommendations.instance.RecommendationsImpl;

import java.util.ArrayList;

public class MetricsImpl implements Metrics
{
    private String name;
    private String status;
    private String namespace;
    private String applicationName;

    private double originalMemoryLimit = -1;
    private double originalMemoryRequests = -1;

    private double originalCpuLimit = -1;
    private double originalCpuRequests = -1;

    private Recommendations y2dRecommendations = new RecommendationsImpl();
    private Recommendations currentRecommendations = new RecommendationsImpl();

    public ArrayList<MetricCollector> metricCollector = new ArrayList<>();

    @Override
    public String getName() { return name; }

    @Override
    public void setName(String name)
    {
        if (name != null)
            this.name = name;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public void setNamespace(String namespace)
    {
        if (namespace != null)
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
        if (status != null)
            this.status = status;
    }

    public String getApplicationName()
    {
        return applicationName;
    }

    public void setApplicationName(String applicationName)
    {
        if (applicationName != null)
            this.applicationName = applicationName;
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

    @Override
    public double getCpuRequests()
    {
        return y2dRecommendations.getCpuRequest();
    }

    @Override
    public double getCpuLimit()
    {
        return y2dRecommendations.getCpuLimit();
    }

    @Override
    public double getRssRequests()
    {
        return y2dRecommendations.getRssRequest();
    }

    @Override
    public double getRssLimits()
    {
        return y2dRecommendations.getRssLimit();
    }

    @Override
    public void setCpuRequests(double value)
    {
        y2dRecommendations.setCpuRequest(value);
    }

    @Override
    public void setCpuLimit(double value)
    {
        y2dRecommendations.setCpuLimit(value);
    }

    @Override
    public void setRssRequests(double value)
    {
        y2dRecommendations.setRssRequest(value);
    }

    @Override
    public void setRssLimit(double value)
    {
        y2dRecommendations.setRssLimit(value);
    }

    @Override
    public void setCurrentCpuRequests(double value)
    {
        currentRecommendations.setCpuRequest(value);
    }

    @Override
    public void setCurrentRssRequests(double value)
    {
        currentRecommendations.setRssRequest(value);
    }

    @Override
    public void setCurrentCpuLimit(double value)
    {
        currentRecommendations.setCpuLimit(value);
    }

    @Override
    public void setCurrentRssLimit(double value)
    {
        currentRecommendations.setRssLimit(value);
    }

    @Override
    public double getCurrentCpuRequests()
    {
        return currentRecommendations.getCpuRequest();
    }

    @Override
    public double getCurrentRssRequests()
    {
        return currentRecommendations.getRssRequest();
    }

    @Override
    public double getCurrentCpuLimit()
    {
        return currentRecommendations.getCpuLimit();
    }

    @Override
    public double getCurrentRssLimit() { return currentRecommendations.getRssLimit(); }

    @Override
    public boolean getCurrentStatus()
    {
        return (this.getStatus().equals("Running")
                || this.getStatus().equals("Succeeded")) ;
    }
}
