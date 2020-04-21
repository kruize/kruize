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

package com.kruize.query.runtimes.java;

import com.kruize.environment.DeploymentInfo;
import com.kruize.exceptions.InvalidValueException;
import com.kruize.query.prometheus.runtimes.java.openj9.OpenJ9PrometheusJavaQuery;

public class JavaQuery
{
    /**
     * @return String for a generic Java query that will fetch all applications exporting Java metrics
     */
    public String fetchJavaAppsQuery()
    {
        return null;
    };

    public HeapQuery heapQuery = null;
    public NonHeapQuery nonHeapQuery = null;

    public static JavaQuery getInstance(String vm) throws InvalidValueException
    {
        if (DeploymentInfo.getMonitoringAgent().toUpperCase().equals("PROMETHEUS"))
        {
            if (vm.equals("OpenJ9"))
                return new OpenJ9PrometheusJavaQuery();
        }

        throw new InvalidValueException("JavaQuery not supported");
    }
}