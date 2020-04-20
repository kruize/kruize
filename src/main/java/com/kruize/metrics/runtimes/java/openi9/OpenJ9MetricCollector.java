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

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.kruize.exceptions.InvalidValueException;
import com.kruize.metrics.MetricsImpl;
import com.kruize.metrics.runtimes.java.JavaMetricCollector;
import com.kruize.query.runtimes.java.JavaQuery;
import com.kruize.query.runtimes.java.openj9.OpenJ9JavaQuery;
import com.kruize.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class OpenJ9MetricCollector implements JavaMetricCollector
{
    private Heap heap = new Heap();
    private NonHeap nonHeap = new NonHeap();
    private JavaQuery javaQuery = new OpenJ9JavaQuery();

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenJ9MetricCollector.class);


    @Override
    public double getHeap()
    {
        return heap.getTotalSize();
    }

    @Override
    public double getNonHeap()
    {
        return nonHeap.getTotalSize();
    }

    @Override
    public void setHeap(double heap)
    {

    }

    @Override
    public void setNonHeap(double nonHeap)
    {

    }

    public static class Heap
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

    public static class NonHeap
    {
        private double classStorage;
        private double jitCodeCache;
        private double jitDataCache;
        private double miscellaneous;

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

    public void collectOpenJ9Metrics(MetricsImpl metrics, String monitoringAgentEndPoint, String area)
    {
        String labelName = metrics.getLabelName();

        try {
            for (String partOfHeap: javaQuery.heapQuery.getPartsOfHeap())
            {
                double value = getValueForQuery(new URL(monitoringAgentEndPoint +
                        javaQuery.heapQuery.getHeapQuery(labelName, partOfHeap, area)));
                heap.setHeap(value, partOfHeap);
            }

            for (String partOfNonHeap : javaQuery.nonHeapQuery.getPartsOfNonHeap())
            {
                double value = getValueForQuery(new URL(monitoringAgentEndPoint +
                        javaQuery.nonHeapQuery.getNonHeapQuery(labelName, partOfNonHeap, area)));
                nonHeap.setNonHeap(value, partOfNonHeap);

            }
/*
            heap.setTenuredLOA(getValueForQuery(new URL(monitoringAgentEndPoint +
                    javaQuery.heapQuery.getTenuredLOA(area, labelName))));
            heap.setTenuredSOA(getValueForQuery(new URL(monitoringAgentEndPoint +
                    OpenJ9JavaQuery.heapQuery.getTenuredSOA(area, labelName))));
            heap.setNurseryAllocate(getValueForQuery(new URL(monitoringAgentEndPoint +
                    OpenJ9JavaQuery.heapQuery.getNurseryAllocate(area, labelName))));
            heap.setNurserySurviror(getValueForQuery(new URL(monitoringAgentEndPoint +
                    OpenJ9JavaQuery.heapQuery.getNurserySurvivor(area, labelName))));

            nonHeap.setMiscellaneous(getValueForQuery(new URL(monitoringAgentEndPoint +
                    OpenJ9JavaQuery.nonHeapQuery.getMiscellaneous(area, labelName))));
            nonHeap.setClassStorage(getValueForQuery(new URL(monitoringAgentEndPoint +
                    OpenJ9JavaQuery.nonHeapQuery.getClassStorage(area, labelName))));
            nonHeap.setJitDataCache(getValueForQuery(new URL(monitoringAgentEndPoint +
                    OpenJ9JavaQuery.nonHeapQuery.getJitDataCache(area, labelName))));
            nonHeap.setJitCodeCache(getValueForQuery(new URL(monitoringAgentEndPoint +
                    OpenJ9JavaQuery.nonHeapQuery.getJitCodeCache(area, labelName))));
*/

        } catch (InvalidValueException | IndexOutOfBoundsException | MalformedURLException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private double getValueForQuery(URL url) throws IndexOutOfBoundsException
    {
        try {
            return getAsJsonArray(url)
                    .get(1)
                    .getAsDouble();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private JsonArray getAsJsonArray(URL url) throws IndexOutOfBoundsException
    {
        String response = HttpUtil.getDataFromURL(url);

        return new JsonParser()
                .parse(response)
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject()
                .get("result")
                .getAsJsonArray()
                .get(0)
                .getAsJsonObject()
                .get("value")
                .getAsJsonArray();
    }
}
