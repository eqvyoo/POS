package com.dlnl.deliveryguard.web.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class RiderDeliveryRequest {

    public static class ItemDetail {

        public static class OptionDetail {

            @JsonProperty("name")
            private String name;

            @JsonProperty("quantity")
            private Integer quantity;

            @JsonProperty("unit_price")
            private Integer unitPrice;

        }

        @JsonProperty("type")
        private String type;

        @JsonProperty("name")
        private String name;

        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("unit_price")
        private Integer unitPrice;

        @JsonProperty("stock_code")
        private String stockCode;

        @JsonProperty("option_detail")
        private List<OptionDetail> optionDetails;
    }

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("branch_code")
    private String branchCode;

    @JsonProperty("sender_phone")
    private String senderPhone;

    @JsonProperty("dest_address")
    private String destAddress;

    @JsonProperty("dest_address_detail")
    private String destAddressDetail;

    @JsonProperty("dest_lat")
    private String destLat;

    @JsonProperty("dest_lng")
    private String destLng;

    @JsonProperty("recipient_phone")
    private String recipientPhone;

    @JsonProperty("client_delivery_no")
    private String clientDeliveryNo;

    @JsonProperty("client_order_no")
    private String clientOrderNo;

    @JsonProperty("contactless")
    private boolean contactless;

    @JsonProperty("delivery_value")
    private int deliveryValue;

    @JsonProperty("item_detail")
    private List<ItemDetail> itemDetail;
}
