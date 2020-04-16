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

package com.kruize.query.runtimes.java.openj9;

public class OpenJ9JavaQuery
{
    public final static String getAppsQuery = "jvm_memory_used_bytes{area=\"heap\",id=\"tenured-SOA\"}";

    /* TODO Add checks for area */
    public static HeapQuery heapQuery = new HeapQuery();
    public static NonHeapQuery nonHeapQuery = new NonHeapQuery();
}

