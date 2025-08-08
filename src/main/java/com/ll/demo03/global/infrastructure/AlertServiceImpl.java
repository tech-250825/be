package com.ll.demo03.global.infrastructure;

import com.ll.demo03.global.port.AlertService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class AlertServiceImpl implements AlertService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${slack.url}")
    private String webhookUrl;

    @Override
    public void sendAlert(String message) {
        Map<String, String> payload = new HashMap<>();
        payload.put("text", message);
        restTemplate.postForEntity(webhookUrl, payload, String.class);
    }
}
