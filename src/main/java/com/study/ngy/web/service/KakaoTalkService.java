package com.study.ngy.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoTalkService {

    private static final String KAKAO_PUSH_URL = "https://kapi.kakao.com/v2/push/send";
    private static final String ADMIN_KEY = "KAKAO_ADMIN_KEY";  // Replace with your actual Kakao Admin Key
    private static final String DEVICE_ID = "DEVICE_ID";        // Replace with user's registered device ID
    private static final String UUID = "USER_UUID";             // Replace with actual user's Kakao UUID

    public String sendKakaoMessage(String phoneNumber, String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "KakaoAK " + ADMIN_KEY);

            // Build request body
            Map<String, Object> notificationMessage = new HashMap<>();
            notificationMessage.put("for_fcm", Map.of("notification", Map.of("title", "New Alert", "body", message)));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("uuids", new String[]{UUID});
            requestBody.put("push_message", notificationMessage);

            // Convert request body to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);
            ResponseEntity<String> response = restTemplate.exchange(KAKAO_PUSH_URL, HttpMethod.POST, entity, String.class);

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending push notification: " + e.getMessage();
        }
    }
}
