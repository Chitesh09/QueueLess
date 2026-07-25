package com.queueless.queue;

import com.queueless.domain.entity.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class QRService {

    @Value("${queueless.qr.secret}")
    private String qrSecret;

    public String generateQRSignature(Token token) {
        String data = String.format("TOKEN:%d:ORG:%d:TIME:%d",
                token.getId(), token.getOrganizationId(), token.getCreatedAt().toEpochMilli());
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(qrSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hmacData = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating QR signature", e);
        }
    }

    public boolean verifyQRSignature(Token token, String signature) {
        if (signature == null || token == null) return false;
        String expected = generateQRSignature(token);
        return expected.equals(signature);
    }
}
