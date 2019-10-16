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

package com.kruize.recommendations.instance;

public class PodRecommendations extends AbstractRecommendations
{
    private double cpuLimit;
    private double cpuRequest;

    private double rssLimit;
    private double rssRequest;

    public double getCpuLimit()
    {
        return cpuLimit;
    }

    public void setCpuLimit(double cpuLimit)
    {
        this.cpuLimit = cpuLimit;
    }

    public double getCpuRequest()
    {
        return cpuRequest;
    }

    public void setCpuRequest(double cpuRequest)
    {
        this.cpuRequest = cpuRequest;
    }

    public double getRssLimit()
    {
        return rssLimit;
    }

    public void setRssLimit(double rssLimit)
    {
        this.rssLimit = rssLimit;
    }

    public double getRssRequest()
    {
        return rssRequest;
    }

    public void setRssRequest(double rssRequest)
    {
        this.rssRequest = rssRequest;
    }
}
