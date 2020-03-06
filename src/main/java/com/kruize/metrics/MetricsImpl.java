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

import com.kruize.exceptions.InvalidValueException;
import com.kruize.recommendations.instance.Recommendations;
import com.kruize.recommendations.instance.RecommendationsImpl;

import java.util.ArrayList;

public class MetricsImpl implements Metrics
{
    private String name;
    private String status;
    private String namespace;
    private String applicationName;

    private double originalMemoryLimit = 0;
    private double originalMemoryRequests = 0;

    private double originalCpuLimit = 0;
    private double originalCpuRequests = 0;

    private Recommendations y2dRecommendations = new RecommendationsImpl();
    private Recommendations currentRecommendations = new RecommendationsImpl();

    public ArrayList<MetricCollector> metricCollector = new ArrayList<>();

    @Override
    public String getName() { return name; }

    @Override
    public void setName(String name) throws InvalidValueException
    {
        if (name == null)
            throw new InvalidValueException("Instance name cannot be null");

        this.name = name;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public void setNamespace(String namespace) throws InvalidValueException
    {
        if (namespace == null)
            throw new InvalidValueException("Application namespace cannot be null");

        this.namespace = namespace;
    }

    @Override
    public String getStatus()
    {
        return status;
    }

    @Override
    public void setStatus(String status) throws InvalidValueException
    {
        if (status == null)
            throw new InvalidValueException("Application status cannot be null");

        this.status = status;
    }

    public String getApplicationName()
    {
        return applicationName;
    }

    public void setApplicationName(String applicationName) throws InvalidValueException
    {
        if (applicationName == null)
            throw new InvalidValueException("Application name cannot be null");

        this.applicationName = applicationName;
    }

    public double getOriginalMemoryRequests()
    {
        return originalMemoryRequests;
    }

    public void setOriginalMemoryRequests(double originalMemoryRequests)
            throws InvalidValueException
    {
        if (originalMemoryRequests < 0)
            throw new InvalidValueException("Original memory requests cannot be negative");

        this.originalMemoryRequests = originalMemoryRequests;
    }

    public double getOriginalMemoryLimit()
    {
        return originalMemoryLimit;
    }

    public void setOriginalMemoryLimit(double originalMemoryLimit) throws InvalidValueException
    {
        if (originalMemoryLimit < 0)
            throw new InvalidValueException("Original memory limit cannot be negative");

        this.originalMemoryLimit = originalMemoryLimit;
    }

    public double getOriginalCpuRequests()
    {
        return originalCpuRequests;
    }

    public void setOriginalCpuRequests(double originalCpuRequests) throws InvalidValueException
    {
        if (originalCpuRequests < 0)
            throw new InvalidValueException("Original CPU requests cannot be negative");

        this.originalCpuRequests = originalCpuRequests;
    }

    public double getOriginalCpuLimit()
    {
        return originalCpuLimit;
    }

    public void setOriginalCpuLimit(double originalCpuLimit) throws InvalidValueException
    {
        if (originalCpuLimit < 0)
            throw new InvalidValueException("Original CPU limit cannot be negative");

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
    public void setCpuRequests(double cpuRequests) throws InvalidValueException
    {
        if (cpuRequests < 0)
            throw new InvalidValueException("CPU requests cannot be negative");

        y2dRecommendations.setCpuRequest(cpuRequests);
    }

    @Override
    public void setCpuLimit(double cpuLimit) throws InvalidValueException
    {
        if (cpuLimit < 0)
            throw new InvalidValueException("CPU limit cannot be negative");

        y2dRecommendations.setCpuLimit(cpuLimit);
    }

    @Override
    public void setRssRequests(double rssRequests) throws InvalidValueException
    {
        if (rssRequests < 0)
            throw new InvalidValueException("RSS requests cannot be negative");

        y2dRecommendations.setRssRequest(rssRequests);
    }

    @Override
    public void setRssLimit(double rssLimit) throws InvalidValueException
    {
        if (rssLimit < 0)
            throw new InvalidValueException("RSS limit cannot be negative");

        y2dRecommendations.setRssLimit(rssLimit);
    }

    @Override
    public void setCurrentCpuRequests(double currentCpuRequests) throws InvalidValueException
    {
        if (currentCpuRequests < 0)
            throw new InvalidValueException("Current CPU requests cannot be negative");

        currentRecommendations.setCpuRequest(currentCpuRequests);
    }

    @Override
    public void setCurrentRssRequests(double currentRssRequests) throws InvalidValueException
    {
        if (currentRssRequests < 0)
            throw new InvalidValueException("Current RSS requests cannot be negative");

        currentRecommendations.setRssRequest(currentRssRequests);
    }

    @Override
    public void setCurrentCpuLimit(double currentCpuLimit) throws InvalidValueException
    {
        if (currentCpuLimit < 0)
            throw new InvalidValueException("Current CPU limit cannot be negative");

        currentRecommendations.setCpuLimit(currentCpuLimit);
    }

    @Override
    public void setCurrentRssLimit(double currentRssLimit) throws InvalidValueException
    {
        if (currentRssLimit < 0)
            throw new InvalidValueException("Current RSS limit cannot be negative");

        currentRecommendations.setRssLimit(currentRssLimit);
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
