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

package com.kruize.analysis;

import com.kruize.metrics.Metrics;

import java.util.ArrayList;

public interface Analysis<T extends Metrics>
{
    void calculateCpuLimit(T metrics);
    void calculateMemLimit(T metrics);
    void calculateCpuRequests(T metrics);
    void calculateMemRequests(T metrics, int referenceIndex, int targetIndex);
    void finalizeY2DRecommendations(T metrics);

    static double getLargestSpike(ArrayList<Double> arrayList)
    {
        final double ONE_MB = 1024 * 1024;
        double largestSpike = 50 * ONE_MB;

        for (int i = 1; i < arrayList.size(); i++) {
            double difference = (arrayList.get(i) - arrayList.get(i - 1));
            if (difference > largestSpike)
                largestSpike = difference;
        }

        return largestSpike;
    }
}
