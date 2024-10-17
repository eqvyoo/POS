package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.config.SecurityUtil;
import com.dlnl.deliveryguard.domain.Order;
import com.dlnl.deliveryguard.domain.Store;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.repository.OrderRepository;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.web.DTO.OrderDetailResponse;
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

import java.util.stream.Collectors;

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

    public OrderDetailResponse getOrderDetail(Long orderId, User user) {
        try {
            // 주문을 가져옵니다.
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. ID: " + orderId));

            // 현재 사용자가 주문이 속한 가게의 주인인지 확인합니다.
            if (!order.getStore().getOwner().getId().equals(user.getId())) {
                throw new IllegalArgumentException("해당 주문에 접근할 권한이 없습니다.");
            }

            // 주문 상세 정보를 DTO로 변환하여 반환합니다.
            return OrderDetailResponse.builder()
                    .orderDateTime(order.getOrderDateTime())
                    .menus(order.getOrderMenus().stream()
                            .map(om -> OrderDetailResponse.MenuWithQuantity.builder()
                                    .menuName(om.getMenu().getName())
                                    .quantity(om.getQuantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .customerPhoneNumber(order.getCustomerPhoneNumber())
                    .orderNumber(order.getOrderNumber())
                    .orderPlatform(order.getOrderPlatform())
                    .paymentMethod(order.getPaymentMethod())
                    .orderType(order.getOrderType())
                    .status(order.getStatus())
                    .paymentAmount(order.getPaymentAmount())
                    .address(order.getAddress().getAddress())
                    .estimatedCookingTime(order.getEstimatedCookingTime())
                    .deliveryAgency(order.getDeliveryAgency())
                    .riderRequestTime(order.getRiderRequestTime())
                    .build();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("주문 상세 조회 중 오류가 발생했습니다.");
        }
    }
}