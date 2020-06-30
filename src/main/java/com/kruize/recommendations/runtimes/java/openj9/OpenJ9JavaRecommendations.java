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

package com.kruize.recommendations.runtimes.java.openj9;

import com.kruize.recommendations.runtimes.java.JavaRecommendations;

public class OpenJ9JavaRecommendations implements JavaRecommendations
{
    /* TODO Also add nursery recommendations and GC */
    private String gcPolicy = "gencon";

    private double rssMax;
    private double heapRecommendation;
    private double nonHeapRecommendation;

    @Override
    public double getHeapRecommendation()
    {
        return heapRecommendation;
    }

    @Override
    public void setHeapRecommendation(double heapRecommendation)
    {
        this.heapRecommendation = heapRecommendation;
    }

    @Override
    public double getNonHeapRecommendation()
    {
        return nonHeapRecommendation;
    }

    @Override
    public void setNonHeapRecommendation(double nonHeapRecommendation)
    {
        this.nonHeapRecommendation = nonHeapRecommendation;
    }

    @Override
    public String getGcPolicy()
    {
        return gcPolicy;
    }

    @Override
    public void setGcPolicy(String gcPolicy)
    {
        this.gcPolicy = gcPolicy;
    }

    @Override
    public double getRssMax()
    {
        return rssMax;
    }

    @Override
    public void setRssMax(double rss)
    {
        this.rssMax = rss;
    }
}
