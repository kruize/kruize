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

public class OpenJ9MetronomeHeap implements JavaHeap
{
    private double JavaHeap;

    public void setHeap(double value, String partOfHeap) throws InvalidValueException
    {
        if ("JavaHeap".equals(partOfHeap)) {
            setJavaHeap(value);
        } else {
            throw new InvalidValueException("No " + partOfHeap + " in heap");
        }
    }

    public double getHeap(String partOfHeap) throws InvalidValueException
    {
        if ("JavaHeap".equals(partOfHeap)) {
            return getJavaHeap();
        }
        throw new InvalidValueException("No " + partOfHeap + " in heap");
    }

    public double getJavaHeap()
    {
        return JavaHeap;
    }

    public void setJavaHeap(double javaHeap)
    {
        this.JavaHeap = javaHeap;
    }

    public double getTotalSize()
    {
        return JavaHeap;
    }
}
