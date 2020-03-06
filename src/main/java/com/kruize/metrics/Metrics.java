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

import com.kruize.exceptions.InvalidValueException;

public interface Metrics
{
    /* Get original application deployment parameters */
    double getOriginalMemoryRequests();
    double getOriginalMemoryLimit();
    double getOriginalCpuRequests();
    double getOriginalCpuLimit();

    void setOriginalMemoryRequests(double originalMemoryRequests) throws InvalidValueException;
    void setOriginalMemoryLimit(double originalMemoryLimit) throws InvalidValueException;
    void setOriginalCpuRequests(double originalCpuRequests) throws InvalidValueException;
    void setOriginalCpuLimit(double originalCpuLimit) throws InvalidValueException;

    String getName();
    String getApplicationName();
    String getStatus();
    String getNamespace();

    void setName(String name) throws InvalidValueException;
    void setApplicationName(String applicationName) throws InvalidValueException;
    void setStatus(String status) throws InvalidValueException;
    void setNamespace(String namespace) throws InvalidValueException;

    double getCpuRequests();
    double getCpuLimit();
    double getRssRequests();
    double getRssLimits();

    void setCpuRequests(double value) throws InvalidValueException;
    void setCpuLimit(double value) throws InvalidValueException;
    void setRssRequests(double value) throws InvalidValueException;
    void setRssLimit(double value) throws InvalidValueException;

    void setCurrentCpuRequests(double value) throws InvalidValueException;
    void setCurrentRssRequests(double value) throws InvalidValueException;
    void setCurrentCpuLimit(double value) throws InvalidValueException;
    void setCurrentRssLimit(double value) throws InvalidValueException;

    double getCurrentCpuRequests();
    double getCurrentRssRequests();
    double getCurrentCpuLimit();
    double getCurrentRssLimit();

    boolean getCurrentStatus();
}
