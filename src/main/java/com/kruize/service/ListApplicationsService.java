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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ListApplicationsService extends HttpServlet
{
    /**
     * Returns a JSON of the list of applications being monitored by Kruize, along with their status and
     * if recommendations are generated.
     *
     * <pre>
     * Example JSON:
     * [
     *   {
     *     "application_name": "kruize",
     *     "recommendations_generated": "no",
     *     "status": "idle"
     *   },
     *   {
     *     "application_name": "grafana",
     *     "recommendations_generated": "no",
     *     "status": "idle"
     *   },
     *   {
     *     "application_name": "cadvisor",
     *     "recommendations_generated": "yes",
     *     "status": "running"
     *   },
     *   {
     *     "application_name": "prometheus",
     *     "recommendations_generated": "no",
     *     "status": "idle"
     *   }
     * ]
     * </pre>
     *
     *
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        JsonArray jsonArray = new JsonArray();
        resp.setContentType("application/json");

        ApplicationRecommendationsImpl applicationRecommendations = EnvTypeImpl.getInstance().applicationRecommendations;

        for (String application : applicationRecommendations.applicationMap.keySet())
        {
            String recommendationsGenerated = "no";

            try {
                if (applicationRecommendations.getRssLimits(application) != 0)
                    recommendationsGenerated = "yes";
            } catch (NoSuchApplicationException ignored) { }

            JsonObject applicationJson = new JsonObject();
            applicationJson.addProperty("application_name", application);
            applicationJson.addProperty("recommendations_generated", recommendationsGenerated);
            applicationJson.addProperty("status", applicationRecommendations.getStatus(application));
            jsonArray.add(applicationJson);
        }

        resp.getWriter().println(new GsonBuilder().setPrettyPrinting().create().toJson(jsonArray));
    }
}
