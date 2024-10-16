package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.config.SecurityUtil;
import com.dlnl.deliveryguard.domain.Store;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.repository.OrderRepository;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.web.DTO.OrderListResponse;
import com.dlnl.deliveryguard.web.DTO.OrderSearchCriteria;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PagedResourcesAssembler<OrderListResponse> pagedResourcesAssembler;

    @Transactional(readOnly = true)
    public Page<OrderListResponse> searchOrders(OrderSearchCriteria criteria, Pageable pageable, User user) {
        Long storeId = user.getStore().getId();
        return orderRepository.searchOrders(criteria, pageable, storeId);
    }
}