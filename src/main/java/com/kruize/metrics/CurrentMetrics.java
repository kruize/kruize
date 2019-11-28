/*
 *
 */

package com.kruize.metrics;

import com.kruize.collection.CollectMetrics;
import com.kruize.exceptions.ApplicationIdleStateException;

import java.net.MalformedURLException;
import java.net.URL;

public class CurrentMetrics
{
    private String monitoringAgentEndPoint;
    private AbstractMetrics metrics;
    private String rssQuery;
    private String cpuQuery;
    private double rss;
    private double cpu;

    public CurrentMetrics(String monitoringAgentEndPoint, AbstractMetrics metrics, String rssQuery, String cpuQuery)
    {
        this.monitoringAgentEndPoint = monitoringAgentEndPoint;
        this.metrics = metrics;
        this.rssQuery = rssQuery;
        this.cpuQuery = cpuQuery;
    }

    public double getRss()
    {
        return rss;
    }

    public double getCpu()
    {
        return cpu;
    }

    public CurrentMetrics invoke() throws MalformedURLException
    {
        double MIN_CPU = 0.02;
        try {
            cpu = getValueForQuery(new URL(monitoringAgentEndPoint + cpuQuery));
            System.out.println("CPU: " + cpu);

            rss = getValueForQuery(new URL(monitoringAgentEndPoint + rssQuery));
            System.out.println("RSS: " + rss);

            //TODO Get network data from monitoring agent
            double network = 0;

            if (cpu < MIN_CPU)
                throw new ApplicationIdleStateException();

            metrics.metricCollector.add(new MetricCollector(rss, cpu, network));
            return this;

        } catch (IndexOutOfBoundsException | ApplicationIdleStateException e) {
            return this;
        }

    }

    private double getValueForQuery(URL url) throws IndexOutOfBoundsException
    {
        return CollectMetrics.getAsJsonArray(url, "value")
                .get(1)
                .getAsDouble();
    }
}
