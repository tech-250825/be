package com.ll.demo03.payment.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minidev.json.JSONObject;

@Getter
@NoArgsConstructor
@ToString
public class OxaPayInvoiceResponse {
    private boolean success;
    private String payLink;
    private String trackId;
    private String orderId;
    private String message;
    private String errorCode;
    private int statusCode;

    private OxaPayInvoiceResponse(boolean success, String payLink, String trackId, String orderId, 
                                String message, String errorCode, int statusCode) {
        this.success = success;
        this.payLink = payLink;
        this.trackId = trackId;
        this.orderId = orderId;
        this.message = message;
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public static OxaPayInvoiceResponse success(String payLink, String trackId, String orderId) {
        return new OxaPayInvoiceResponse(true, payLink, trackId, orderId, null, null, 200);
    }

    public static OxaPayInvoiceResponse fromJsonObject(JSONObject jsonObject, int statusCode) {
        if (statusCode == 200) {
            return new OxaPayInvoiceResponse(
                true,
                (String) jsonObject.get("payLink"),
                (String) jsonObject.get("trackId"),
                (String) jsonObject.get("orderId"),
                null,
                null,
                statusCode
            );
        } else {
            return new OxaPayInvoiceResponse(
                false,
                null,
                null,
                null,
                (String) jsonObject.get("message"),
                (String) jsonObject.get("code"),
                statusCode
            );
        }
    }

    public static OxaPayInvoiceResponse error(String message, String errorCode, int statusCode) {
        return new OxaPayInvoiceResponse(false, null, null, null, message, errorCode, statusCode);
    }
}