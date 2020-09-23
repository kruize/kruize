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

package com.kruize.main;

import com.kruize.collection.CollectMetrics;
import com.kruize.initialize.Initialize;
import com.kruize.service.HealthService;
import com.kruize.service.ListApplicationsService;
import com.kruize.service.RecommendationsService;
import io.prometheus.client.exporter.MetricsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kruize
{
    private static final int PORT = 31313;
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

        addServlets(context);

        server.start();
    }

    private static void addServlets(ServletContextHandler context)
    {
        context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
        context.addServlet(RecommendationsService.class, "/recommendations");
        context.addServlet(ListApplicationsService.class, "/listApplications");
        context.addServlet(HealthService.class, "/health");
    }

    private static void disableServerLogging()
    {
        /* The jetty server creates a lot of server log messages that are unnecessary.
         * This disables jetty logging. */
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
    }
}
