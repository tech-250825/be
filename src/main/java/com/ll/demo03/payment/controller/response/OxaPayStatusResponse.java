package com.ll.demo03.payment.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minidev.json.JSONObject;

@Getter
@NoArgsConstructor
@ToString
public class OxaPayStatusResponse {
    private boolean success;
    private Object paymentInfo;
    private String message;
    private String errorCode;
    private int statusCode;

    private OxaPayStatusResponse(boolean success, Object paymentInfo, String message, String errorCode, int statusCode) {
        this.success = success;
        this.paymentInfo = paymentInfo;
        this.message = message;
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public static OxaPayStatusResponse success(Object paymentInfo) {
        return new OxaPayStatusResponse(true, paymentInfo, null, null, 200);
    }

    public static OxaPayStatusResponse fromJsonObject(JSONObject jsonObject, int statusCode) {
        if (statusCode == 200) {
            return new OxaPayStatusResponse(true, jsonObject, null, null, statusCode);
        } else {
            return new OxaPayStatusResponse(
                false,
                null,
                (String) jsonObject.get("message"),
                (String) jsonObject.get("code"),
                statusCode
            );
        }
    }

    public static OxaPayStatusResponse error(String message, String errorCode, int statusCode) {
        return new OxaPayStatusResponse(false, null, message, errorCode, statusCode);
    }
}