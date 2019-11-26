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

package com.kruize.environment;

import com.kruize.analysis.Analysis;
import com.kruize.environment.docker.DockerEnvImpl;
import com.kruize.environment.kubernetes.KubernetesEnvImpl;
import com.kruize.metrics.AbstractMetrics;
import com.kruize.query.Query;
import com.kruize.recommendations.application.AbstractApplicationRecommendations;

public abstract class EnvTypeImpl implements EnvType
{
    public AbstractMetrics metrics;
    public Analysis analysis;
    public Query query;
    public AbstractApplicationRecommendations applicationRecommendations;

    private static EnvTypeImpl envType = null;

    static {
        getInstance();
    }

    public static EnvTypeImpl getInstance()
    {
        if (envType == null) {
            if (DeploymentInfo.getClusterType().toUpperCase().equals("DOCKER")) {
                envType = new DockerEnvImpl();
            } else {
                envType = new KubernetesEnvImpl();
            }
            envType.setupMonitoringAgent();
            envType.setupApplicationRecommendations();
            envType.setupAnalysis();
            envType.setupQuery();
            envType.getAllApps();
        }
        return envType;
    }

}
