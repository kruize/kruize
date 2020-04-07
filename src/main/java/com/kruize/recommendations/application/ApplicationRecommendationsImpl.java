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

import com.kruize.exceptions.InvalidValueException;
import com.kruize.exceptions.NoSuchApplicationException;
import com.kruize.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kruize.metrics.MetricsImpl;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ApplicationRecommendationsImpl implements ApplicationRecommendations
{
    private ApplicationRecommendationsImpl() { }

    private static ApplicationRecommendationsImpl applicationRecommendations = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRecommendations.class);

    private DecimalFormat oneDecimalPlace = new DecimalFormat("#.#");

    public HashMap< String, ArrayList<MetricsImpl>> applicationMap = new HashMap<>();
    public HashMap< String, ArrayList<String>> runtimesMap = new HashMap<>();

    static {
        getInstance();
    }

    public static ApplicationRecommendationsImpl getInstance() {
        if (applicationRecommendations == null)
            applicationRecommendations = new ApplicationRecommendationsImpl();

        return applicationRecommendations;
    }

    public void addMetricToApplication(String applicationName, MetricsImpl metrics)
    {
        /* Checking if the pod has already been added before */
        for (MetricsImpl metric : applicationMap.get(applicationName)) {
            if (metric.getName().equals(metrics.getName())) {
                try {
                    metric.setStatus(metrics.getStatus());
                } catch (InvalidValueException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        applicationMap.get(applicationName).add(metrics);
        LOGGER.debug("Application {} added for monitoring", applicationName);
    }

    @Override
    public double getRssLimits(String applicationName) throws NoSuchApplicationException
    {
        oneDecimalPlace.setRoundingMode(RoundingMode.CEILING);

        if (applicationMap.containsKey(applicationName)) {
            double weightedRssLimits = 0;
            double totalValues = 0;

            for (MetricsImpl metrics : applicationMap.get(applicationName)) {
                int numberOfValues = metrics.metricCollector.size();
                weightedRssLimits += metrics.getRssLimits() * numberOfValues;

                totalValues += numberOfValues;
            }

            if (totalValues == 0) {
                return 0;
            } else {
                return Double.parseDouble(oneDecimalPlace.format(
                        MathUtil.bytesToMB(weightedRssLimits / totalValues)));
            }

        } else {
            throw new NoSuchApplicationException();
        }
    }

    @Override
    public double getCpuLimit(String applicationName) throws NoSuchApplicationException
    {
        oneDecimalPlace.setRoundingMode(RoundingMode.CEILING);

        if (applicationMap.containsKey(applicationName)) {
            double weightedCpuLimits = 0;
            double totalValues = 0;

            for (MetricsImpl metrics : applicationMap.get(applicationName)) {
                int numberOfValues = metrics.metricCollector.size();
                weightedCpuLimits += metrics.getCpuLimit() * numberOfValues;

                totalValues += numberOfValues;
            }

            if (totalValues == 0) {
                return 0;
            } else {
                return Double.parseDouble(oneDecimalPlace.format(weightedCpuLimits / totalValues));
            }

        } else {
            throw new NoSuchApplicationException();
        }
    }

    @Override
    public double getRssRequests(String applicationName) throws NoSuchApplicationException
    {
        oneDecimalPlace.setRoundingMode(RoundingMode.CEILING);

        if (applicationMap.containsKey(applicationName)) {
            double weightedRssRequests = 0;
            double totalValues = 0;

            for (MetricsImpl metrics : applicationMap.get(applicationName)) {
                int numberOfValues = metrics.metricCollector.size();
                weightedRssRequests += metrics.getRssRequests() * numberOfValues;

                totalValues += numberOfValues;
            }

            if (totalValues == 0) {
                return 0;
            } else {
                return Double.parseDouble(oneDecimalPlace.format(
                        MathUtil.bytesToMB(weightedRssRequests / totalValues)));
            }

        } else {
            throw new NoSuchApplicationException();
        }
    }

    @Override
    public double getCpuRequests(String applicationName) throws NoSuchApplicationException
    {
        oneDecimalPlace.setRoundingMode(RoundingMode.CEILING);

        if (applicationMap.containsKey(applicationName)) {
            double weightedCpuRequests = 0;
            double totalValues = 0;

            for (MetricsImpl metrics : applicationMap.get(applicationName)) {
                int numberOfValues = metrics.metricCollector.size();
                weightedCpuRequests += metrics.getCpuRequests() * numberOfValues;

                totalValues += numberOfValues;
            }

            if (totalValues == 0) {
                return 0;
            } else {
                return Double.parseDouble(oneDecimalPlace.format(weightedCpuRequests / totalValues));
            }

        } else {
            throw new NoSuchApplicationException();
        }
    }

    /* If any instances of the application is running, return running */
    public String getStatus(String applicationName)
    {
        boolean applicationIdle = false;
        for (MetricsImpl metric: applicationMap.get(applicationName))
        {
            /*
                If any instance of the application is running, return running
             */
            if (metric.getStatus().equals("running"))
                return "running";

            if (metric.getStatus().equals("idle"))
                applicationIdle = true;
        }

        if (applicationIdle)
            return "idle";

        /*
            No instance of the application is running or idle, return
            the status of the application as reported by Kubernetes.

            Will be one of {"Pending", "Succeeded", "Failed", "Unknown"}
         */
        return applicationMap.get(applicationName).get(0).getStatus();
    }

    public String getRuntime(String applicationName)
    {
        for (MetricsImpl metric : applicationMap.get(applicationName))
        {
            if (metric.getRuntime() != null)
            {
                return metric.getRuntime();
            }
        }

        return null;
    }

    public double getHeapRecommendation(String applicationName)
    {
        for (MetricsImpl metric : applicationMap.get(applicationName))
        {
            if (metric.getRuntime() != null)
            {
                return metric.getJavaRecommendations().getHeapRecommendation();
            }
        }

        return 0;
    }

    public double getNonHeapRecommendation(String applicationName)
    {
        for (MetricsImpl metric : applicationMap.get(applicationName))
        {
            if (metric.getRuntime() != null)
            {
                return metric.getJavaRecommendations().getNonHeapRecommendation();
            }
        }
        return 0;
    }

    public boolean isRuntimeInfoAvailable(String applicationName)
    {
        for (MetricsImpl metric : applicationMap.get(applicationName))
        {
            if (metric.getRuntime() != null)
            {
                return true;
            }
        }

        return false;
    }


}
