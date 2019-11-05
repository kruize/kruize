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

package com.kruize.recommendations.application;

import com.kruize.exceptions.NoSuchApplicationException;
import com.kruize.metrics.ContainerMetrics;

import java.util.HashMap;

public class DockerApplicationRecommendations extends AbstractApplicationRecommendations<ContainerMetrics>
{
    private DockerApplicationRecommendations()
    {
        applicationMap = new HashMap<>();
    }

    private static DockerApplicationRecommendations dockerApplicationRecommendations = null;

    static {
        getInstance();
    }

    public static DockerApplicationRecommendations getInstance()
    {
        if (dockerApplicationRecommendations == null) {
            dockerApplicationRecommendations = new DockerApplicationRecommendations();
        }

        return dockerApplicationRecommendations;
    }

    @Override
    public double getCpuRequests(String applicationName) throws NoSuchApplicationException
    {
        return 0;
    }

    @Override
    public double getCpuLimit(String applicationName) throws NoSuchApplicationException
    {
        if(applicationMap.containsKey(applicationName))
        {
            double weightedCpuRequests = 0;
            double totalValues = 0;

            for(ContainerMetrics containerMetrics : applicationMap.get(applicationName))
            {
                int numberOfValues = containerMetrics.metricCollector.size();
                weightedCpuRequests += containerMetrics.getCpuLimit() * numberOfValues;

                totalValues += numberOfValues;
            }

            return weightedCpuRequests / totalValues;

        }
        else
        {
            throw new NoSuchApplicationException();
        }
    }

    @Override
    public double getRssRequests(String applicationName) throws NoSuchApplicationException
    {
        return 0;
    }

    @Override
    public double getRssLimits(String applicationName) throws NoSuchApplicationException
    {
        if(applicationMap.containsKey(applicationName))
        {
            double weightedCpuRequests = 0;
            double totalValues = 0;

            for(ContainerMetrics containerMetrics : applicationMap.get(applicationName))
            {
                int numberOfValues = containerMetrics.metricCollector.size();
                weightedCpuRequests += containerMetrics.getRssLimits() * numberOfValues;

                totalValues += numberOfValues;
            }

            return weightedCpuRequests / totalValues;
        }
        else
        {
            throw new NoSuchApplicationException();
        }
    }
}
