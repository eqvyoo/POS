package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.Order;
import com.dlnl.deliveryguard.web.DTO.CancelDeliveryRequest;
import com.dlnl.deliveryguard.web.DTO.CancelDeliveryResponse;
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
        String url = "http://localhost:8080/api/delivery/cancel";

        // 요청 데이터 구성
        CancelDeliveryRequest cancelDeliveryRequest = CancelDeliveryRequest.builder()
                .deliveryId(deliveryId)
                .build();

        // 헤더 설정 (API 키 인증 추가)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "ApiKey your-api-key");  // 실제 API 키로 변경
        headers.set("ApiSecret", "your-api-secret");          // 실제 API 시크릿으로 변경

        // HttpEntity를 이용해 요청 본문과 헤더를 모두 포함
        HttpEntity<CancelDeliveryRequest> requestEntity = new HttpEntity<>(cancelDeliveryRequest, headers);

        // API 호출
        ResponseEntity<CancelDeliveryResponse> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, CancelDeliveryResponse.class);

        return responseEntity.getBody();
    }
//
//    public void callRider(Order order, LocalDateTime riderRequestTime) {
//        String url = "http://localhost:8080/api/delivery/submit";
//
//        // RiderDeliveryRequest DTO로 요청 데이터 구성
//        RiderDeliveryRequest riderDeliveryRequest = RiderDeliveryRequest.builder()
//                .requestId(order.getOrderNumber())
//                .branchCode(order.getStore().getBranchCode())  // 적절한 branchCode 필드를 설정해야 함
//                .senderPhone(order.getStore().getPhoneNumber())
//                .destAddress(order.getAddress().getAddress())
//                .destAddressDetail(order.getAddress().getDetail())
//                .destLat(order.getAddress().getLatitude())
//                .destLng(order.getAddress().getLongitude())
//                .recipientPhone(order.getCustomer().getPhoneNumber())
//                .clientDeliveryNo(order.getOrderNumber())
//                .clientOrderNo(order.getOrderNumber())
//                .deliveryValue(Integer.parseInt(order.getPaymentAmount()))
//                .build();
//
//        // 헤더 설정
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        // 요청 본문과 헤더를 HttpEntity로 구성
//        HttpEntity<RiderDeliveryRequest> requestEntity = new HttpEntity<>(riderDeliveryRequest, headers);
//
//        // API 호출
//        restTemplate.postForEntity(url, requestEntity, String.class);
//    }
}