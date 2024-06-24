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

package com.kruize.metrics.runtimes.java;

import com.kruize.recommendations.runtimes.java.JavaRecommendations;

public class JavaApplicationInfo
{
    private String VM;
    private String gcPolicy;
    private String dataSource;
    private JavaRecommendations javaRecommendations;

    public JavaApplicationInfo(String VM, String gcPolicy, String dataSource, JavaRecommendations javaRecommendations)
    {
        this.VM = VM;
        this.gcPolicy = gcPolicy;
        this.javaRecommendations = javaRecommendations;
        this.dataSource = dataSource;
    }

    public String getVM()
    {
        return VM;
    }

    public void setVM(String VM)
    {
        this.VM = VM;
    }

    public String getGcPolicy()
    {
        return gcPolicy;
    }

    public void setGcPolicy(String gcPolicy)
    {
        this.gcPolicy = gcPolicy;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public JavaRecommendations getJavaRecommendations()
    {
        return javaRecommendations;
    }

    public void setJavaRecommendations(JavaRecommendations javaRecommendations)
    {
        this.javaRecommendations = javaRecommendations;
    }
}
