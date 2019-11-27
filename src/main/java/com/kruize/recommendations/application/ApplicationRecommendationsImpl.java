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
import com.kruize.metrics.AbstractMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class ApplicationRecommendationsImpl<T extends AbstractMetrics> implements ApplicationRecommendations
{
    public HashMap< String, ArrayList<T>> applicationMap = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRecommendations.class);


    public void addMetricToApplication(String applicationName, T metrics)
    {
        /* Checking if the pod has already been added before */
        for (T metric : applicationMap.get(applicationName)) {
            if (metric.getName().equals(metrics.getName())) {
                return;
            }
        }
        applicationMap.get(applicationName).add(metrics);
        LOGGER.debug("Application {} added for monitoring", applicationName);

    }

    public double getRssLimits(String applicationName) throws NoSuchApplicationException
    {
        if (applicationMap.containsKey(applicationName)) {
            double weightedCpuRequests = 0;
            double totalValues = 0;

            for (T metrics : applicationMap.get(applicationName)) {
                int numberOfValues = metrics.metricCollector.size();
                weightedCpuRequests += metrics.getRssLimits() * numberOfValues;

                totalValues += numberOfValues;
            }
            return weightedCpuRequests / totalValues;
        } else {
            throw new NoSuchApplicationException();
        }
    }

    public double getCpuLimit(String applicationName) throws NoSuchApplicationException
    {
        if (applicationMap.containsKey(applicationName)) {
            double weightedCpuRequests = 0;
            double totalValues = 0;

            for (T metrics : applicationMap.get(applicationName)) {
                int numberOfValues = metrics.metricCollector.size();
                weightedCpuRequests += metrics.getCpuLimit() * numberOfValues;

                totalValues += numberOfValues;
            }
            return weightedCpuRequests / totalValues;
        } else {
            throw new NoSuchApplicationException();
        }
    }

    public double getCpuRequests(String applicationName) throws NoSuchApplicationException
    {
        if (applicationMap.containsKey(applicationName)) {
            double weightedCpuRequests = 0;
            double totalValues = 0;

            for (T metrics : applicationMap.get(applicationName)) {
                int numberOfValues = metrics.metricCollector.size();
                weightedCpuRequests += metrics.getCpuRequests() * numberOfValues;

                totalValues += numberOfValues;
            }

            return weightedCpuRequests / totalValues;

        } else {
            throw new NoSuchApplicationException();
        }
    }

    public double getRssRequests(String applicationName) throws NoSuchApplicationException
    {
        if (applicationMap.containsKey(applicationName)) {
            double weightedCpuRequests = 0;
            double totalValues = 0;

            for (T metrics : applicationMap.get(applicationName)) {
                int numberOfValues = metrics.metricCollector.size();
                weightedCpuRequests += metrics.getRssRequests() * numberOfValues;

                totalValues += numberOfValues;
            }
            return weightedCpuRequests / totalValues;
        } else {
            throw new NoSuchApplicationException();
        }
    }

}
