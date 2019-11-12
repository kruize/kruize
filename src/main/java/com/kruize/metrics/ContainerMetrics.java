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

import com.kruize.recommendations.instance.ContainerRecommendations;
import com.kruize.recommendations.instance.Recommendations;

public class ContainerMetrics extends AbstractMetrics
{
    private Recommendations y2dRecommendations = new ContainerRecommendations();
    private Recommendations currentRecommendations = new ContainerRecommendations();

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
    public double getCurrentRssLimit()
    {
        return currentRecommendations.getRssLimit();
    }
}
