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

package com.kruize.service;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kruize.environment.EnvTypeImpl;
import com.kruize.exceptions.NoSuchApplicationException;
import com.kruize.recommendations.application.ApplicationRecommendationsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RecommendationsService extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationsService.class);

    /**
     *
     * Returns a JSON of recommendations for applications monitored by Kruize.
     *
     * API:
     * /recommendations : gives recommendations for all applications monitored by Kruize
     * /recommendations?application_name=<APPLICATION_NAME> : gives recommendation for specific application monitored by Kruize
     * <pre>
     * Example JSON:
     * [
     *   {
     *     "application_name": "kruize",
     *     "resources": {
     *       "requests": {
     *         "memory": "51.4MB",
     *         "cpu": 0.6
     *       },
     *       "limits": {
     *         "memory": "83.5MB",
     *         "cpu": 0.9
     *       }
     *     }
     *   },
     *   {
     *     "application_name": "cadvisor",
     *     "resources": {
     *       "requests": {
     *         "memory": "49.4MB",
     *         "cpu": 0.3
     *       },
     *       "limits": {
     *         "memory": "99.4MB",
     *         "cpu": 0.2
     *       }
     *     }
     *   }
     * ]
     * </pre>
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String application_name = req.getParameter("application_name");

        JsonArray jsonArray = new JsonArray();

        ApplicationRecommendationsImpl applicationRecommendations = EnvTypeImpl.getInstance().applicationRecommendations;

        /* No parameter application_name in HTTP request. Give recommendations for all applications monitored */
        if (application_name == null)
        {
            for (String application : applicationRecommendations.applicationMap.keySet())
            {
                try {
                    JsonObject applicationRecommendationJson = getApplicationJson(applicationRecommendations,
                            application);
                    jsonArray.add(applicationRecommendationJson);
                } catch (NoSuchApplicationException e) {
                    System.out.println(application + " not found");

                }
            }
        }
        else
        {
            try {
                JsonObject applicationRecommendationJson = getApplicationJson(applicationRecommendations,
                        application_name);
                if (applicationRecommendationJson != null)
                    jsonArray.add(applicationRecommendationJson);
            } catch (NoSuchApplicationException e) {
                resp.getWriter().println("Error: No such application found");
                return;
            }
        }

        resp.setContentType("application/json");

        /* Pretty print the recommendations JSON */
        resp.getWriter().println(new GsonBuilder().setPrettyPrinting().create().toJson(jsonArray));
    }

    private JsonObject getApplicationJson(ApplicationRecommendationsImpl applicationRecommendations, String application) throws NoSuchApplicationException
    {
        JsonObject applicationRecommendationJson = new JsonObject();
        applicationRecommendationJson.addProperty("application_name", application);

        JsonObject resourcesJson = getResourceJson(applicationRecommendations, application);

        if (resourcesJson != null)
        {
            applicationRecommendationJson.add("resources", resourcesJson);
            return applicationRecommendationJson;
        }
        else
        {
            return null;
        }
    }

    private JsonObject getResourceJson(ApplicationRecommendationsImpl applicationRecommendations, String application) throws NoSuchApplicationException
    {
        String applicationStatus = applicationRecommendations.getStatus(application);

        /* If application is still running or idle, or if application is removed,
            but Kruize has earlier generated recommendations.
         */
        if (applicationStatus.equals("running") || applicationStatus.equals("idle")
                || (applicationRecommendations.getRssRequests(application) != 0))
        {
            JsonObject resourcesJson = new JsonObject();
            JsonObject resourceRequestsJson = new JsonObject();
            resourceRequestsJson.addProperty("memory", applicationRecommendations.getRssRequests(application) + "M");
            resourceRequestsJson.addProperty("cpu", applicationRecommendations.getCpuRequests(application));

            JsonObject resourceLimitsJson = new JsonObject();
            resourceLimitsJson.addProperty("memory", applicationRecommendations.getRssLimits(application) + "M");
            resourceLimitsJson.addProperty("cpu", applicationRecommendations.getCpuLimit(application));

            resourcesJson.add("requests", resourceRequestsJson);
            resourcesJson.add("limits", resourceLimitsJson);
            return resourcesJson;
        }

        LOGGER.info("Application {} is no longer running and has no recommendations generated earlier", application);
        LOGGER.info("Not returning any recommendations");
        return null;
    }
}
