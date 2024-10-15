package com.dlnl.deliveryguard.web.DTO;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
@Builder
@Getter
public class CustomerListResponse {

    private StoreDTO store;  // Store 정보는 한번만 출력
    private List<CustomerDTO> customers;  // 고객 목록

    @Builder
    @Getter
    public static class StoreDTO {
        private Long id;
        private String storeName;
        private String storeAddress;
    }

    @Builder
    @Getter
    public static class CustomerDTO {
        private String nickname;
        private String phoneNumber;
        private List<AddressDTO> addresses;

        @Builder
        @Getter
        public static class AddressDTO {
            private Long id;
            private String address;
        }
    }
}