package com.ll.demo03.domain.payment.dto;

import lombok.*;
import net.minidev.json.JSONObject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmResponse {
    private JSONObject responseData;
    private int statusCode;

    public static PaymentConfirmResponse fromJsonObject(JSONObject jsonObject, int statusCode) {
        return new PaymentConfirmResponse(jsonObject, statusCode);
    }
}
