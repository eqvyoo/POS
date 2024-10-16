package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.config.SecurityUtil;
import com.dlnl.deliveryguard.domain.Store;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.repository.OrderRepository;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.web.DTO.OrderListResponse;
import com.dlnl.deliveryguard.web.DTO.OrderSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<OrderListResponse> searchOrders(OrderSearchCriteria criteria, Pageable pageable, User user) {
        Long storeId = user.getStore().getId(); // 현재 사용자의 가게 ID를 가져옵니다.
        return orderRepository.searchOrders(criteria, pageable, storeId);
    }
}