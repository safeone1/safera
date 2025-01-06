package com.ids.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class ApiService {
    private final HttpClient httpClient;

    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<String> sendGetRequest(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .exceptionally(e -> "Error: " + e.getMessage());
        } catch (Exception e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.complete("Error creating request: " + e.getMessage());
            return future;
        }
    }

    public CompletableFuture<String> sendPostRequest(String url, String requestBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .exceptionally(e -> "Error: " + e.getMessage());
        } catch (Exception e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.complete("Error creating request: " + e.getMessage());
            return future;
        }
    }
}