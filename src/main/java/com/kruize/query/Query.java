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

package com.kruize.query;

public interface Query
{
    String getCpuQuery(String podName);
    String getRssQuery(String podName);

    String getNetworkBytesTransmitted(String podName);
    String getNetworkBytesReceived(String podName);

    String getMemoryRequests(String podName);
    String getMemoryLimit(String podName);

    String getAPIEndpoint();

    String getPreviousCpuQuery(String podName);
    String getPreviousRssQuery(String podName);

    String getPreviousCpuReqRec(String applicationName);
    String getPreviousCpuLimRec(String applicationName);
    String getPreviousMemReqRec(String applicationName);
    String getPreviousMemLimRec(String applicationName);
}
