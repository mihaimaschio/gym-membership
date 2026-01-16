package com.gym_membership.controller;

import com.gym_membership.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiService geminiService;

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        String response = geminiService.getChatResponse(userMessage);
        return ResponseEntity.ok(Map.of("response", response));
    }
}
