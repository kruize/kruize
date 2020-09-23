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

package com.kruize.metrics;

public class MetricCollector
{
    private double rssValue;
    private double cpuValue;
    private double networkValue;

    public final static int CPU_INDEX = 0;
    public final static int RSS_INDEX = 1;
    public final static int NETWORK_INDEX = 2;

    public MetricCollector(double rssValue, double cpuValue, double networkValue)
    {
        this.rssValue = rssValue;
        this.cpuValue = cpuValue;
        this.networkValue = networkValue;
    }

    private MetricCollector(MetricCollector metricCollector)
    {
        this.rssValue = metricCollector.rssValue;
        this.cpuValue = metricCollector.cpuValue;
        this.networkValue = metricCollector.networkValue;
    }

    public static MetricCollector Copy(MetricCollector metricCollector)
    {
        return new MetricCollector(metricCollector);
    }

    public double getRssValue()
    {
        return rssValue;
    }

    public double getCpuValue()
    {
        return cpuValue;
    }

    public double getNetworkValue()
    {
        return networkValue;
    }

    public void setForIndex(double value, int index)
    {
        if (index == MetricCollector.CPU_INDEX)
            this.cpuValue = value;
        else if (index == MetricCollector.RSS_INDEX)
            this.rssValue = value;
        else
            this.networkValue = value;
    }

    public double getFromIndex(int index)
    {
        if (index == MetricCollector.CPU_INDEX)
            return cpuValue;

        if ( index == MetricCollector.RSS_INDEX)
            return rssValue;

        return networkValue;
    }

    @Override
    public String toString()
    {
        return "CPU: " + cpuValue + "\tRSS: " + rssValue + "\tNetwork bytes: " + networkValue;
    }
}
