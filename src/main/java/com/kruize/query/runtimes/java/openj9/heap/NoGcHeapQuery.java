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

package com.kruize.query.runtimes.java.openj9.heap;

import com.kruize.exceptions.InvalidValueException;
import com.kruize.query.runtimes.java.HeapQuery;

public abstract class NoGcHeapQuery implements HeapQuery
{
    private final String[] partsOfHeap = {"tenured"};

    @Override
    public String[] getPartsOfHeap()
    {
        return partsOfHeap;
    }

    @Override
    public String getHeapQuery(String application, String dataSource, String partOfHeap, String area) throws InvalidValueException
    {
        if ("tenured".equals(partOfHeap)) {
            return getTenured(area, dataSource, application);
        }
        throw new InvalidValueException("No " + partOfHeap + " present in heap");
    }

    public abstract String getTenured(String area, String dataSource, String name);

}
