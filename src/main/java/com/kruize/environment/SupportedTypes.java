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

import java.util.*;

public class SupportedTypes
{
    public static final Set<String> CLUSTER_TYPES_SUPPORTED =
            new HashSet<>(Arrays.asList("KUBERNETES", "DOCKER"));

    public static final Set<String> K8S_TYPES_SUPPORTED =
            new HashSet<>(Arrays.asList("MINIKUBE", "OPENSHIFT", "ICP", null));

    public static final Set<String> AUTH_TYPES_SUPPORTED =
            new HashSet<>(Arrays.asList("SAML", "OIDC", "", null));

    public static final Set<String> MONITORING_AGENTS_SUPPORTED =
            new HashSet<>(Collections.singletonList("PROMETHEUS"));

    public static final Set<String> RUNTIMES_SUPPORTED =
            new HashSet<>(Collections.singletonList("JAVA"));

    public static final ArrayList<String> POLICIES_SUPPORTED =
            new ArrayList<>(Arrays.asList("THROUGHPUT", "SECURITY","STARTUP"));


}
