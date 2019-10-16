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

public interface Metrics
{

    /* Get original application deployment parameters */
    double getOriginalMemoryRequests();
    double getOriginalMemoryLimit();
    double getOriginalCpuRequests();
    double getOriginalCpuLimit();

    String getName();
    String getStatus();
    String getNamespace();

    void setName(String name);
    void setStatus(String status);

    double getCpuRequests();
    double getCpuLimit();
    double getRssRequests();
    double getRssLimits();

    void setCpuRequests(double value);
    void setCpuLimit(double value);
    void setRssRequests(double value);
    void setRssLimit(double value);

    void setCurrentCpuRequests(double value);
    void setCurrentRssRequests(double value);
    void setCurrentCpuLimit(double value);
    void setCurrentRssLimit(double value);

    double getCurrentCpuRequests();
    double getCurrentRssRequests();
    double getCurrentCpuLimit();
    double getCurrentRssLimit();


}
