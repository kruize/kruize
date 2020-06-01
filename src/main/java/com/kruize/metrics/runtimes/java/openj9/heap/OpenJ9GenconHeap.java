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

package com.kruize.metrics.runtimes.java.openj9.heap;

import com.kruize.exceptions.InvalidValueException;
import com.kruize.metrics.runtimes.java.JavaHeap;

public class OpenJ9GenconHeap implements JavaHeap
{
    private double tenuredLOA;
    private double nurserySurviror;
    private double nurseryAllocate;
    private double tenuredSOA;

    public void setHeap(double value, String partOfHeap) throws InvalidValueException
    {
        switch (partOfHeap) {
            case "tenured-LOA":
                setTenuredLOA(value);
                break;
            case "tenured-SOA":
                setTenuredSOA(value);
                break;
            case "nursery-survivor":
                setNurserySurviror(value);
                break;
            case "nursery-allocate":
                setNurseryAllocate(value);
                break;
            default:
                throw new InvalidValueException("No " + partOfHeap + " in heap");
        }
    }

    public double getHeap(String partOfHeap) throws InvalidValueException
    {
        switch (partOfHeap) {
            case "tenured-LOA":
                return getTenuredLOA();
            case "tenured-SOA":
                return getTenuredSOA();
            case "nursery-survivor":
                return getNurserySurviror();
            case "nursery-allocate":
                return getNurseryAllocate();
            default:
                throw new InvalidValueException("No " + partOfHeap + " in heap");
        }
    }

    double getTenuredLOA()
    {
        return tenuredLOA;
    }

    void setTenuredLOA(double tenuredLOA)
    {
        this.tenuredLOA = tenuredLOA;
    }

    double getNurserySurviror()
    {
        return nurserySurviror;
    }

    void setNurserySurviror(double nurserySurviror)
    {
        this.nurserySurviror = nurserySurviror;
    }

    double getNurseryAllocate()
    {
        return nurseryAllocate;
    }

    void setNurseryAllocate(double nurseryAllocate)
    {
        this.nurseryAllocate = nurseryAllocate;
    }

    double getTenuredSOA()
    {
        return tenuredSOA;
    }

    void setTenuredSOA(double tenuredSOA)
    {
        this.tenuredSOA = tenuredSOA;
    }

    public double getTotalSize()
    {
        return nurseryAllocate + nurserySurviror + tenuredLOA + tenuredSOA;
    }
}
