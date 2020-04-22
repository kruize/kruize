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

public class OpenJ9BalancedHeap implements JavaHeap
{
    private double balancedOld;
    private double balancedEden;
    private double balancedSurvivor;
    private double balancedReserved;

    public void setHeap(double value, String partOfHeap) throws InvalidValueException
    {
        switch (partOfHeap) {
            case "balanced-old":
                setBalancedOld(value);
                break;
            case "balanced-eden":
                setBalancedEden(value);
                break;
            case "balanced-survivor":
                setBalancedSurvivor(value);
                break;
            case "balanced-reserved":
                setBalancedReserved(value);
                break;
            default:
                throw new InvalidValueException("No " + partOfHeap + " in heap");
        }
    }

    public double getHeap(String partOfHeap) throws InvalidValueException
    {
        switch (partOfHeap) {
            case "balanced-old":
                return getBalancedOld();
            case "balanced-eden":
                return getBalancedEden();
            case "balanced-survivor":
                return getBalancedSurvivor();
            case "balanced-reserved":
                return getBalancedReserved();
            default:
                throw new InvalidValueException("No " + partOfHeap + " in heap");
        }
    }

    public double getBalancedOld()
    {
        return balancedOld;
    }

    public void setBalancedOld(double balancedOld)
    {
        this.balancedOld = balancedOld;
    }

    public double getBalancedEden()
    {
        return balancedEden;
    }

    public void setBalancedEden(double balancedEden)
    {
        this.balancedEden = balancedEden;
    }

    public double getBalancedSurvivor()
    {
        return balancedSurvivor;
    }

    public void setBalancedSurvivor(double balancedSurvivor)
    {
        this.balancedSurvivor = balancedSurvivor;
    }

    public double getBalancedReserved()
    {
        return balancedReserved;
    }

    public void setBalancedReserved(double balancedReserved)
    {
        this.balancedReserved = balancedReserved;
    }

    @Override
    public double getTotalSize()
    {
        return balancedEden + balancedOld + balancedReserved + balancedSurvivor;
    }
}
