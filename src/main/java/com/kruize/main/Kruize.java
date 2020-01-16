/*******************************************************************************
 * Copyright (c) 2019, 2019 IBM Corporation and others.
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

package com.kruize.main;

import com.kruize.collection.CollectMetrics;
import com.kruize.initialize.Initialize;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.MetricsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kruize
{
    // port to listen connection
    private static final int PORT = 31313;

    /*
     * Gauges in Prometheus are values that can arbitrarily change to some other value.
     */
    public static final Gauge cpuRequests = Gauge.build()
            .name("kruize_exp_cpu_requests")
            .help("CPU Requests obtained by Kruize")
            .labelNames("namespace", "application_name")
            .register();

    public static final Gauge cpuLimits = Gauge.build()
            .name("kruize_exp_cpu_limits")
            .help("CPU Limits obtained by Kruize")
            .labelNames("namespace", "application_name")
            .register();

    public static final Gauge memoryRequests = Gauge.build()
            .name("kruize_exp_memory_requests")
            .help("Memory Requests obtained by Kruize")
            .labelNames("namespace", "application_name")
            .register();

    public static final Gauge memoryLimits = Gauge.build()
            .name("kruize_exp_memory_limits")
            .help("Memory Limits obtained by Kruize")
            .labelNames("namespace", "application_name")
            .register();

    public static final Gauge originalMemoryLimits = Gauge.build()
            .name("kruize_exp_original_memory_limits")
            .help("Original Memory Limits")
            .labelNames("namespace", "application_name")
            .register();

    public static final Gauge originalMemoryRequests = Gauge.build()
            .name("kruize_exp_original_memory_requests")
            .help("Original Memory Requests")
            .labelNames("namespace", "application_name")
            .register();

    public static final Gauge originalCpuRequests = Gauge.build()
            .name("kruize_exp_original_cpu_requests")
            .help("Original CPU Requests")
            .labelNames("namespace", "application_name")
            .register();

    public static final Gauge originalCpuLimits = Gauge.build()
            .name("kruize_exp_original_cpu_limits")
            .help("Original CPU Limits")
            .labelNames("namespace", "application_name")
            .register();

    public static final Gauge applicationCpuUsed = Gauge.build()
            .name("kruize_exp_application_cpu_current")
            .help("Current CPU used by application")
            .labelNames("namespace", "application_name")
            .register();

    public static final Gauge applicationMemUsed = Gauge.build()
            .name("kruize_exp_application_rss_current")
            .help("Current RSS of application")
            .labelNames("namespace", "application_name")
            .register();


    private static final Logger LOGGER = LoggerFactory.getLogger(Kruize.class);

    public static void main(String[] args) throws Exception
    {
        Initialize.setup_deployment_info();
        LOGGER.info("End of initialization phase");

        CollectMetrics collectMetrics = new CollectMetrics();
        Thread metricThread = new Thread(collectMetrics);
        metricThread.setDaemon(true);
        metricThread.start();

        startServer();
    }

    private static void startServer() throws Exception
    {
        disableServerLogging();

        Server server = new Server(PORT);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
        context.addServlet(new ServletHolder(new HealthEndpoint()), "/health");

        server.start();
    }

    private static void disableServerLogging()
    {
        /* The jetty server creates a lot of server log messages that are unnecessary.
         * This disables jetty logging. */
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
    }
}
