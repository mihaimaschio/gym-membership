package com.gym_membership.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public String getChatResponse(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        
        part.put("text", "You are a helpful gym assistant specialized in fitness and nutrition. If the user asks about anything unrelated to these topics, politely refuse to answer. Keep your response concise: " + userMessage);
        content.put("parts", Collections.singletonList(part));
        requestBody.put("contents", Collections.singletonList(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = apiUrl + "?key=" + apiKey;

        int maxRetries = 7;
        int retryCount = 0;
        long waitTime = 2000; // Start with 2 seconds

        while (retryCount < maxRetries) {
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
                Map<String, Object> responseBody = response.getBody();
                
                if (responseBody != null && responseBody.containsKey("candidates")) {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map<String, Object> candidate = candidates.get(0);
                        Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                        if (!parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
                return "Sorry, I couldn't understand that.";
            } catch (HttpClientErrorException.TooManyRequests e) {
                retryCount++;
                System.out.println("Quota exceeded. Response: " + e.getResponseBodyAsString());
                System.out.println("Retrying in " + waitTime + "ms... (Attempt " + retryCount + "/" + maxRetries + ")");
                if (retryCount >= maxRetries) {
                     return "The AI service is currently experiencing high traffic. Please try again in 2-3 minutes.";
                }
                try {
                    Thread.sleep(waitTime);
                    waitTime *= 2; // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "Request interrupted.";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Error communicating with AI: " + e.getMessage();
            }
        }
        return "The AI service is unavailable.";
    }
}
