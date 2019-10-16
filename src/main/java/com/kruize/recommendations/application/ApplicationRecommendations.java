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
import com.kruize.metrics.Metrics;

import java.util.ArrayList;
import java.util.HashMap;

public interface ApplicationRecommendations
{
    HashMap< String, ArrayList<Metrics>> applicationMap = null;

    double getCpuRequests(String applicationName) throws NoSuchApplicationException;
    double getCpuLimit(String applicationName) throws NoSuchApplicationException;
    double getRssRequests(String applicationName) throws NoSuchApplicationException;
    double getRssLimits(String applicationName) throws NoSuchApplicationException;
}
