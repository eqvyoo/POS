package com.dlnl.deliveryguard.domain;

public enum Status {

    WAITING,            // 대기 (포장, 배달)
    PROCESSING,         // 처리중 (포장, 배달)
    REQUEST_DELIVERY,   // 배차 요청(배달)
    DELIVERING,         // 배달 중 (배달)
    CUSTOMER_CALL,      // 고객 호출 (포장)
    COMPLETED ,          // 완료 (포장, 배달)

    CANCELED            // 취소


}
