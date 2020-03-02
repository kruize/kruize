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

import com.kruize.recommendations.instance.PodRecommendations;
import com.kruize.recommendations.instance.Recommendations;

public class PodMetrics extends AbstractMetrics
{
    private String podTemplateHash;

    private Recommendations y2dRecommendations = new PodRecommendations();
    private Recommendations currentRecommendations = new PodRecommendations();

    public String getPodTemplateHash()
    {
        return podTemplateHash;
    }

    public void setPodTemplateHash(String podTemplateHash)
    {
        this.podTemplateHash = podTemplateHash;
    }

    public double getCpuRequests()
    {
        return y2dRecommendations.getCpuRequest();
    }

    public double getCpuLimit()
    {
        return y2dRecommendations.getCpuLimit();
    }

    public double getRssRequests()
    {
        return y2dRecommendations.getRssRequest();
    }

    public double getRssLimits()
    {
        return y2dRecommendations.getRssLimit();
    }

    public void setCpuRequests(double value)
    {
        y2dRecommendations.setCpuRequest(value);
    }

    public void setRssRequests(double value)
    {
        y2dRecommendations.setRssRequest(value);
    }

    public void setCpuLimit(double value)
    {
        y2dRecommendations.setCpuLimit(value);
    }

    public void setRssLimit(double value)
    {
        y2dRecommendations.setRssLimit(value);
    }

    public void setCurrentCpuRequests(double value)
    {
        currentRecommendations.setCpuRequest(value);
    }

    public void setCurrentRssRequests(double value)
    {
        currentRecommendations.setRssRequest(value);
    }

    public void setCurrentCpuLimit(double value)
    {
        currentRecommendations.setCpuLimit(value);
    }

    public void setCurrentRssLimit(double value)
    {
        currentRecommendations.setRssLimit(value);
    }

    public double getCurrentCpuRequests()
    {
        return currentRecommendations.getCpuRequest();
    }

    public double getCurrentRssRequests()
    {
        return currentRecommendations.getRssRequest();
    }

    public double getCurrentCpuLimit()
    {
        return currentRecommendations.getCpuLimit();
    }

    public double getCurrentRssLimit() { return currentRecommendations.getRssLimit(); }



}
