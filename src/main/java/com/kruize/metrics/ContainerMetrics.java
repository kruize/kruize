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

public class ContainerMetrics extends AbstractMetrics
{
    @Override
    public double getOriginalMemoryRequests()
    {
        return 0;
    }

    @Override
    public double getOriginalMemoryLimit()
    {
        return 0;
    }

    @Override
    public double getOriginalCpuRequests()
    {
        return 0;
    }

    @Override
    public double getOriginalCpuLimit()
    {
        return 0;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public String getStatus()
    {
        return null;
    }

    @Override
    public void setName(String name)
    {

    }

    @Override
    public void setStatus(String status)
    {

    }

    @Override
    public double getCpuRequests()
    {
        return 0;
    }

    @Override
    public double getCpuLimit()
    {
        return 0;
    }

    @Override
    public double getRssRequests()
    {
        return 0;
    }

    @Override
    public double getRssLimits()
    {
        return 0;
    }

    @Override
    public void setCpuRequests(double value)
    {

    }

    @Override
    public void setCpuLimit(double value)
    {

    }

    @Override
    public void setRssRequests(double value)
    {

    }

    @Override
    public void setRssLimit(double value)
    {

    }

    @Override
    public void setCurrentCpuRequests(double value)
    {

    }

    @Override
    public void setCurrentRssRequests(double value)
    {

    }

    @Override
    public void setCurrentCpuLimit(double value)
    {

    }

    @Override
    public void setCurrentRssLimit(double value)
    {

    }

    @Override
    public double getCurrentCpuRequests()
    {
        return 0;
    }

    @Override
    public double getCurrentRssRequests()
    {
        return 0;
    }

    @Override
    public double getCurrentCpuLimit()
    {
        return 0;
    }

    @Override
    public double getCurrentRssLimit()
    {
        return 0;
    }

    @Override
    public String getNamespace()
    {
        return null;
    }
}
