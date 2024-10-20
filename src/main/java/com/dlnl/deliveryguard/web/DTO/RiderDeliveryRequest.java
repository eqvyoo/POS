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
public class RiderDeliveryRequest {
    @Builder
    public static class ItemDetail {

        public static class OptionDetail {

            @JsonProperty("name")
            private String name;  // 옵션 이름

            @JsonProperty("quantity")
            private Integer quantity;  // 옵션 수량

            @JsonProperty("unit_price")
            private Integer unitPrice;  // 옵션 단가
        }

        @JsonProperty("type")
        private String type;  // ITEM, DELIVERY_FEE, DISCOUNT, DISPOSABLE_CUP_DEPOSIT 중 하나

        @JsonProperty("name")
        private String name;  // 상품 이름

        @JsonProperty("quantity")
        private Integer quantity;  // 상품 수량

        @JsonProperty("unit_price")
        private Integer unitPrice;  // 상품 단가

        @JsonProperty("stock_code")
        private String stockCode;  // 상품의 재고 코드

        @JsonProperty("option_detail")
        private List<OptionDetail> optionDetails;  // 옵션 상세 내용 (선택 사항)
    }

    @JsonProperty("request_id")
    private String requestId;  // 요청 번호 (필수)

    @JsonProperty("branch_code")
    private String branchCode;  // 점포 코드 (필수)

    @JsonProperty("sender_phone")
    @JsonInclude(JsonInclude.Include.NON_NULL)  // null이면 JSON에서 제외
    private String senderPhone;  // 상점 전화번호 (선택)

    @JsonProperty("dest_address")
    private String destAddress;  // 지번 기본 주소 (필수)

    @JsonProperty("dest_address_detail")
    private String destAddressDetail;  // 지번 상세 주소 (필수)

    @JsonProperty("dest_address_road")
    private String destAddressRoad;  // 도로명 기본 주소 (필수)

    @JsonProperty("dest_address_detail_road")
    private String destAddressDetailRoad;  // 도로명 상세 주소 (필수)

    @JsonProperty("dest_lat")
    private String destLat;  // 위도 (필수)

    @JsonProperty("dest_lng")
    private String destLng;  // 경도 (필수)

    @JsonProperty("payment_method")
    private String paymentMethod;  // 결제 수단 (필수)

    @JsonProperty("delivery_value")
    private int deliveryValue;  // 결제 필요 금액 (필수)

    @JsonProperty("disposable_cup_deposit_amount")
    @JsonInclude(JsonInclude.Include.NON_NULL)  // null이면 JSON에서 제외
    private Integer disposableCupDepositAmount;  // 일회용컵 보증금 합계 (선택)

    @JsonProperty("pickup_in")
    private int pickupIn;  // 픽업 요청 시간 (필수)

    @JsonProperty("recipient_phone")
    private String recipientPhone;  // 고객 전화번호 (필수)

    @JsonProperty("order_notes")
    @JsonInclude(JsonInclude.Include.NON_NULL)  // null이면 JSON에서 제외
    private String orderNotes;  // 배송 메시지 (선택)

    @JsonProperty("client_delivery_no")
    @JsonInclude(JsonInclude.Include.NON_NULL)  // null이면 JSON에서 제외
    private String clientDeliveryNo;  // 신청자 배송 번호 (선택)

    @JsonProperty("client_order_no")
    private String clientOrderNo;  // 신청자 주문 번호 (필수)

    @JsonProperty("contactless")
    private boolean contactless;  // 비대면 배송 여부 (필수)

    @JsonProperty("item_detail")
    private List<ItemDetail> itemDetail;  // 상품의 상세 내용 (필수)
}