/*
 *
 */

package com.kruize.recommendations.runtimes.java;

public class OpenJ9JavaRecommendations implements JavaRecommendations
{
    /* TODO Also add nursery recommendations and GC */
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
}
