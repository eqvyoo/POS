package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.web.DTO.OrderListResponse;
import com.dlnl.deliveryguard.web.DTO.OrderSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomOrderRepository {
    Page<OrderListResponse> searchOrders(OrderSearchCriteria criteria, Pageable pageable, Long storeId);
}