package com.urlshortener.service;

import com.urlshortener.dto.request.ShortenUrlRequest;
import com.urlshortener.dto.response.ShortenUrlResponse;
import com.urlshortener.dto.response.UrlInfoResponse;
import com.urlshortener.dto.response.UrlStatsResponse;

public interface UrlService {

    ShortenUrlResponse shortenUrl(ShortenUrlRequest request, String ipAddress);

    String getOriginalUrl(String shortCode);

    UrlInfoResponse getUrlInfo(String shortCode);

    UrlStatsResponse getUrlStats(String shortCode);

    void deleteUrl(String shortCode);
}
