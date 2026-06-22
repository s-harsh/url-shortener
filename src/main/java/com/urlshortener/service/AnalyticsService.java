package com.urlshortener.service;

public interface AnalyticsService {

    void recordClick(String shortCode, String ipAddress, String userAgent, String referer);
}
