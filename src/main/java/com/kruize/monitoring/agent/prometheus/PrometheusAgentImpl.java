package com.kruize.monitoring.agent.prometheus;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.kruize.collection.CollectMetrics;
import com.kruize.main.Kruize;
import com.kruize.metrics.CurrentMetrics;
import com.kruize.monitoring.agent.MonitoringAgentImpl;
import com.kruize.util.HttpUtil;
import io.prometheus.client.Gauge;

import java.net.URL;

public class PrometheusAgentImpl extends MonitoringAgentImpl
{
    final Gauge cpuRequests = Gauge.build()
            .name("kruize_exp_cpu_requests")
            .help("CPU Requests obtained by Kruize")
            .labelNames("namespace", "application_name")
            .register();

    final Gauge cpuLimits = Gauge.build()
            .name("kruize_exp_cpu_limits")
            .help("CPU Limits obtained by Kruize")
            .labelNames("namespace", "application_name")
            .register();

    final Gauge memoryRequests = Gauge.build()
            .name("kruize_exp_memory_requests")
            .help("Memory Requests obtained by Kruize")
            .labelNames("namespace", "application_name")
            .register();

    final Gauge memoryLimits = Gauge.build()
            .name("kruize_exp_memory_limits")
            .help("Memory Limits obtained by Kruize")
            .labelNames("namespace", "application_name")
            .register();

    final Gauge originalMemoryLimits = Gauge.build()
            .name("kruize_exp_original_memory_limits")
            .help("Original Memory Limits")
            .labelNames("namespace", "application_name")
            .register();

    final Gauge originalMemoryRequests = Gauge.build()
            .name("kruize_exp_original_memory_requests")
            .help("Original Memory Requests")
            .labelNames("namespace", "application_name")
            .register();

    final Gauge originalCpuRequests = Gauge.build()
            .name("kruize_exp_original_cpu_requests")
            .help("Original CPU Requests")
            .labelNames("namespace", "application_name")
            .register();

    final Gauge originalCpuLimits = Gauge.build()
            .name("kruize_exp_original_cpu_limits")
            .help("Original CPU Limits")
            .labelNames("namespace", "application_name")
            .register();

    final Gauge applicationCpuUsed = Gauge.build()
            .name("kruize_exp_application_cpu_current")
            .help("Current CPU used by application")
            .labelNames("namespace", "application_name")
            .register();

    final Gauge applicationMemUsed = Gauge.build()
            .name("kruize_exp_application_rss_current")
            .help("Current RSS of application")
            .labelNames("namespace", "application_name")
            .register();

    @Override
    public void setRecommendations(String application, CurrentMetrics currentMetrics)
    {
        Kruize.applicationCpuUsed.labels(namespace, application).set(currentMetrics.getCpu());
        Kruize.applicationMemUsed.labels(namespace, application).set(currentMetrics.getRss());

        if (cpuRequests > 0) {
            Kruize.cpuRequests.labels(namespace, application).set(Math.max(cpuRequests, MIN_CPU_REQUEST));
        } else {
            Kruize.cpuRequests.labels(namespace, application).set(-1);
        }

        if (cpuLimit > 0) {
            Kruize.cpuLimits.labels(namespace, application).set(Math.max(cpuLimit, MIN_CPU_LIMIT));
        } else {
            Kruize.cpuLimits.labels(namespace, application).set(-1);
        }

        Kruize.memoryLimits.labels(namespace, application).set(metrics.getRssLimits());
        Kruize.memoryRequests.labels(namespace, application).set(metrics.getRssRequests());

        Kruize.originalCpuLimits.labels(namespace, application).set(metrics.getOriginalCpuLimit());
        Kruize.originalCpuRequests.labels(namespace, application).set(metrics.getOriginalCpuRequests());

        Kruize.originalMemoryRequests.labels(namespace, application).set(metrics.getOriginalMemoryRequests());
        Kruize.originalMemoryLimits.labels(namespace, application).set(metrics.getOriginalMemoryLimit());

    }

    @Override
    public void getQuery()
    {
        super.getQuery();
    }

    @Override
    public double getValueForQuery(URL url)
    {
        return getAsJsonArray(url, "value")
                .get(1)
                .getAsDouble();
    }

    private JsonArray getAsJsonArray(URL url, String values) throws IndexOutOfBoundsException
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
                .get(values)
                .getAsJsonArray();
    }


}
