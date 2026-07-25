package com.queueless.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckInRequest {
    @NotBlank(message = "QR Signature is required")
    private String qrSignature;
}
