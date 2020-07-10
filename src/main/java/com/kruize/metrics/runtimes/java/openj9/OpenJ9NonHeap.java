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

package com.kruize.metrics.runtimes.java.openj9;

import com.kruize.exceptions.InvalidValueException;
import com.kruize.metrics.runtimes.java.JavaNonHeap;

public class OpenJ9NonHeap implements JavaNonHeap
{
    private double classStorage;
    private double jitCodeCache;
    private double jitDataCache;
    private double miscellaneous;

    @Override
    public void setNonHeap(double value, String partOfNonHeap) throws InvalidValueException
    {
        switch (partOfNonHeap) {
            case "miscellaneous":
                setMiscellaneous(value);
                break;
            case "class storage":
                setClassStorage(value);
                break;
            case "JIT code cache":
                setJitCodeCache(value);
                break;
            case "JIT data cache":
                setJitDataCache(value);
                break;
            default:
                throw new InvalidValueException("No " + partOfNonHeap + " in non-heap");
        }
    }

    @Override
    public double getNonHeap(String partOfNonHeap) throws InvalidValueException
    {
        switch (partOfNonHeap) {
            case "miscellaneous":
                return getMiscellaneous();
            case "class storage":
                return getClassStorage();
            case "JIT code cache":
                return getJitCodeCache();
            case "JIT data cache":
                return getJitDataCache();
            default:
                throw new InvalidValueException("No " + partOfNonHeap + " in non-heap");
        }
    }

    double getClassStorage()
    {
        return classStorage;
    }

    void setClassStorage(double classStorage)
    {
        this.classStorage = classStorage;
    }

    double getJitCodeCache()
    {
        return jitCodeCache;
    }

    void setJitCodeCache(double jitCodeCache)
    {
        this.jitCodeCache = jitCodeCache;
    }

    double getJitDataCache()
    {
        return jitDataCache;
    }

    void setJitDataCache(double jitDataCache)
    {
        this.jitDataCache = jitDataCache;
    }

    double getMiscellaneous()
    {
        return miscellaneous;
    }

    void setMiscellaneous(double miscellaneous)
    {
        this.miscellaneous = miscellaneous;
    }

    public double getTotalSize()
    {
        return jitCodeCache + jitDataCache + classStorage + miscellaneous;
    }
}
