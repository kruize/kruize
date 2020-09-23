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

package com.kruize.analysis;

import com.kruize.metrics.MetricsImpl;

public interface Analysis
{
    void calculateCpuLimit(MetricsImpl metrics);
    void calculateMemLimit(MetricsImpl metrics);
    void calculateCpuRequests(MetricsImpl metrics);
    void calculateMemRequests(MetricsImpl metrics, int referenceIndex, int targetIndex);
    void finalizeY2DRecommendations(MetricsImpl metrics);
}
