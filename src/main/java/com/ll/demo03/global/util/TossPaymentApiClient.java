package com.ll.demo03.global.util;

import com.ll.demo03.global.exception.CustomException;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ll.demo03.global.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TossPaymentApiClient {

    private static final String TOSS_PAYMENT_API_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public JSONObject confirmPayment(JSONObject requestBody, String secretKey) throws CustomException {
        try {

            String authHeader = generateAuthorizationHeader(secretKey);

            URL url = new URL(TOSS_PAYMENT_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", authHeader);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }

            int responseCode = connection.getResponseCode();
            InputStream responseStream = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            JSONParser parser = new JSONParser();
            try (Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
                return (JSONObject) parser.parse(reader);
            }
        } catch (Exception e) {
            logger.error("Failed to confirm payment: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
        }
    }

    private String generateAuthorizationHeader(String secretKey) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedBytes);
    }
}
