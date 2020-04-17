/*******************************************************************************
<<<<<<< HEAD
 * Copyright (c) 2019, 2020 IBM Corporation and others.
=======
 * Copyright (c) 2019, 2019 IBM Corporation and others.
>>>>>>> Made query changes
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

import com.kruize.exceptions.InvalidValueException;

public interface HeapQuery
{
    /**
     * @return String array containing heap parts of memory
     */
    String[] getPartsOfHeap();

    /**
     * Get non-heap query for the specified application and part of heap
     * @param application
     * @param partOfHeap
     * @param area
     * @return query string
     * @throws InvalidValueException
     */
    String getHeapQuery(String application, String partOfHeap, String area) throws InvalidValueException;
}
