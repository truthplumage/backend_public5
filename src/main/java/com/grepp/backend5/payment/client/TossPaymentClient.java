package com.grepp.backend5.payment.client;

import com.grepp.backend5.payment.application.dto.PaymentCommand;
import com.grepp.backend5.payment.client.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Component
public class TossPaymentClient {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final RestClient restClient;
    @Value("payment.toss.secret-key")
    private String secretKey;
    public TossPaymentResponse confirm(PaymentCommand command) throws HttpStatusCodeException{
        if(secretKey == null){
            throw new IllegalStateException("Toss secret key is not configured");
        }
        //토스에 요청할 header
        HttpHeaders headers = createHeaders();
        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", command.paymentKey());
        body.put("orderId", command.orderId());
        body.put("amount", command.amount());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restClient.post().uri(URI.create(CONFIRM_URL)).body(body).headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve().body(TossPaymentResponse.class);
    }

    /**
     * 토스에 전달하는 헤더값<br>
     * 헤더 생성 함수
     * @return
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();//headers 생성
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = secretKey + ":";
        //secretkey base64 Encode
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        //Authorization header에 인코딩 값 입력.
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        return headers;
    }
}
