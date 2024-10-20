package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.Order;
import com.dlnl.deliveryguard.web.DTO.CancelDeliveryRequest;
import com.dlnl.deliveryguard.web.DTO.CancelDeliveryResponse;
import com.dlnl.deliveryguard.web.DTO.DeliverySubmitResponse;
import com.dlnl.deliveryguard.web.DTO.RiderDeliveryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeliveryPlatformService {

    private final RestTemplate restTemplate;

    // 배달 취소 API 요청 메서드
    public CancelDeliveryResponse cancelDelivery(String deliveryId) {
        String url = "http://localhost:8080/api/delivery/cancel";   // todo : 실제 api로 변경 필요

        // 요청 데이터 구성
        CancelDeliveryRequest cancelDeliveryRequest = CancelDeliveryRequest.builder()
                .deliveryId(deliveryId)
                .build();

        // 헤더 설정 (API 키 인증 추가)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "ApiKey your-api-key");  // todo : 실제 API 키로 변경
        headers.set("ApiSecret", "your-api-secret");          // todo : 실제 API 시크릿으로 변경

        // HttpEntity를 이용해 요청 본문과 헤더를 모두 포함
        HttpEntity<CancelDeliveryRequest> requestEntity = new HttpEntity<>(cancelDeliveryRequest, headers);

        // API 호출
        ResponseEntity<CancelDeliveryResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, CancelDeliveryResponse.class);

        return responseEntity.getBody();
    }



    // 배달 제출 API 요청 메서드
    public DeliverySubmitResponse submitDelivery(RiderDeliveryRequest deliverySubmitRequest) {
        String url = "http://localhost:8000/api/delivery/submit";  // todo :  실제 API 엔드포인트로 수정

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", "ApiKey");  // todo : 실제 API 키로 수정
        headers.set("secret", "your-api-secret");  // todo : 실제 시크릿 키로 수정
        headers.set("Accept-Encoding", "gzip");

        // HttpEntity를 이용해 요청 본문과 헤더를 모두 포함
        HttpEntity<RiderDeliveryRequest> requestEntity = new HttpEntity<>(deliverySubmitRequest, headers);

        // API 호출
        ResponseEntity<DeliverySubmitResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, DeliverySubmitResponse.class);

        return responseEntity.getBody();
    }
}
