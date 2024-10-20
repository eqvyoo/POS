package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.web.DTO.CancelDeliveryRequest;
import com.dlnl.deliveryguard.web.DTO.CancelDeliveryResponse;
import com.dlnl.deliveryguard.web.DTO.RiderDeliveryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class TestDeliveryController {

    @PostMapping("/cancel")
    public ResponseEntity<CancelDeliveryResponse> cancelDelivery(@RequestBody CancelDeliveryRequest cancelRequest) {
        try {
            // 배달 취소 로직
            if (cancelRequest.getDeliveryId() == null || cancelRequest.getDeliveryId().isEmpty()) {
                throw new IllegalArgumentException("유효한 배달 ID를 입력하세요.");
            }

            // 예시로, 배달 취소 성공으로 처리
            CancelDeliveryResponse response = CancelDeliveryResponse.builder()
                    .result("SUCCESS")
                    .errorType("")
                    .errorCode("")
                    .errorMessage("")
                    .deliveryId(cancelRequest.getDeliveryId())
                    .status("CANCELLED")
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 실패 처리
            CancelDeliveryResponse errorResponse = CancelDeliveryResponse.builder()
                    .result("FAILED")
                    .errorType("CANCELLATION_ERROR")
                    .errorCode("ERR001")
                    .errorMessage(e.getMessage())
                    .deliveryId(cancelRequest.getDeliveryId())
                    .status("FAILED")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitDelivery(@RequestBody RiderDeliveryRequest riderDeliveryRequest) {
        // 가짜 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("result", "SUCCESS");
        response.put("error_type", "");
        response.put("error_code", "");
        response.put("error_message", "");
        response.put("request_id", riderDeliveryRequest.getRequestId());
        response.put("delivery_id", "18051851296124");
        response.put("base_fee", 3000);
        response.put("extra_fee", 500);
        response.put("sum_total", 3500);
        response.put("extra_fee_details", List.of(Map.of("type", "REGIONS", "amount", 500, "title", "삼성동 할증")));
        response.put("distance", 1632.1);
        response.put("balance", 150000);

        return ResponseEntity.ok(response);
    }
}
