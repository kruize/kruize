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

package com.kruize.util;

import com.kruize.environment.DeploymentInfo;
import com.kruize.main.HealthEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class HttpUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    public static String getDataFromURL(URL url)
    {
        String result = null;
        try {
            HttpURLConnection connection;

            if (url.toString().contains("https")) {
                connection = (HttpsURLConnection) url.openConnection();
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            //TODO Find another way to authorize
            String bearerToken = DeploymentInfo.getAuthToken();

            connection.setRequestProperty("Authorization", bearerToken);

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                result = getDataFromConnection(connection);
            } else {
                if (connection.getResponseCode() == 403) {
                    LOGGER.error("Please refresh your auth token");
                    HealthEndpoint.CURRENT_STATUS = HealthEndpoint.STATUS_DOWN;
                }
                LOGGER.debug("{} Response Failure for {}", connection.getResponseCode(),
                        url.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static String getDataFromConnection(HttpURLConnection connection) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()
        ));

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = bufferedReader.readLine()) != null) {
            response.append(inputLine);
        }

        bufferedReader.close();
        return response.toString();
    }

    public static void disableSSLVertification()
    {
        TrustManager[] dummyTrustManager = new TrustManager[]{new X509TrustManager()
        {
            public X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType)
            {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType)
            {
            }
        }};

        HostnameVerifier allHostsValid = (hostname, session) -> true;

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, dummyTrustManager, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        assert sslContext != null;
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
