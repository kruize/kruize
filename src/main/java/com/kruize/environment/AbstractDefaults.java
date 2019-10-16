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

import com.kruize.environment.docker.DockerDefaults;
import com.kruize.environment.kubernetes.KubernetesDefaults;

public abstract class AbstractDefaults implements Defaults
{
    @Override
    public String getMonitoringAgent()
    {
        return "PROMETHEUS";
    }

    public static AbstractDefaults getInstance(String clusterType)
    {
        if (clusterType.toUpperCase().equals("DOCKER")) {
            return new DockerDefaults();
        } else {
            return new KubernetesDefaults();
        }
    }
}
