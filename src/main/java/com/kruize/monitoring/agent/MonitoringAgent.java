package com.kruize.monitoring.agent;

import java.net.URL;

public interface MonitoringAgent
{
    void setRecommendations();
    void getQuery();
    double getValueForQuery(URL url);
}
