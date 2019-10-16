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

public class ContainerRecommendations extends AbstractRecommendations
{
    @Override
    public double getCpuLimit()
    {
        return 0;
    }

    @Override
    public void setCpuLimit(double cpuLimit)
    {

    }

    @Override
    public double getCpuRequest()
    {
        return 0;
    }

    @Override
    public void setCpuRequest(double cpuRequest)
    {

    }

    @Override
    public double getRssLimit()
    {
        return 0;
    }

    @Override
    public void setRssLimit(double rssLimit)
    {

    }

    @Override
    public double getRssRequest()
    {
        return 0;
    }

    @Override
    public void setRssRequest(double rssRequest)
    {

    }
}
