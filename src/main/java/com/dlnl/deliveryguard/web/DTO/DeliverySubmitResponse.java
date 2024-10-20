package com.dlnl.deliveryguard.web.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliverySubmitResponse {

    @JsonProperty("result")
    private String result;  // 응답 결과 (예: SUCCESS)

    @JsonProperty("error_type")
    private String errorType;  // 오류 유형 (없을 경우 빈 문자열)

    @JsonProperty("error_code")
    private String errorCode;  // 오류 코드 (없을 경우 빈 문자열)

    @JsonProperty("error_message")
    private String errorMessage;  // 오류 메시지 (없을 경우 빈 문자열)

    @JsonProperty("request_id")
    private String requestId;  // 요청 번호

    @JsonProperty("delivery_id")
    private String deliveryId;  // 배달 ID

    @JsonProperty("base_fee")
    private Integer baseFee;  // 기본 요금

    @JsonProperty("extra_fee")
    private Integer extraFee;  // 추가 요금

    @JsonProperty("sum_total")
    private Integer sumTotal;  // 총 요금

    @JsonProperty("extra_fee_details")
    private List<ExtraFeeDetail> extraFeeDetails;  // 추가 요금 세부 정보

    @JsonProperty("distance")
    private Double distance;  // 배달 거리

    @JsonProperty("balance")
    private Integer balance;  // 잔액

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExtraFeeDetail {

        @JsonProperty("type")
        private String type;  // 추가 요금 유형

        @JsonProperty("amount")
        private Integer amount;  // 추가 요금 금액

        @JsonProperty("title")
        private String title;  // 추가 요금 제목 (예: 지역 할증 등)
    }
}
