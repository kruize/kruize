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

package com.kruize.util;

import com.kruize.metrics.MetricCollector;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

public class MathUtil
{
    @SuppressWarnings("SameParameterValue")
    public static double getPercentile(DescriptiveStatistics descriptiveStatistics, double percentile)
    {
        return descriptiveStatistics.getPercentile(percentile);
    }

    public static double getMode(ArrayList<MetricCollector> metricCollector, int targetIndex)
    {
        double modeValue = 0;
        int maxCount = 0;
        for (int i = 0; i < metricCollector.size(); ++i) {
            int count = 0;
            for (MetricCollector metric : metricCollector) {
                if (metric.getFromIndex(targetIndex) == metricCollector.get(i).getFromIndex(targetIndex))
                    ++count;
            }

            if (count > maxCount) {
                maxCount = count;
                modeValue = metricCollector.get(i).getFromIndex(targetIndex);
            }
        }
        System.out.println("Mode is: " + modeValue);
        return modeValue;
    }
}
