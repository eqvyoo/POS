package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.Order;
import com.dlnl.deliveryguard.domain.Status;
import com.dlnl.deliveryguard.repository.OrderRepository;
import com.dlnl.deliveryguard.web.DTO.TrackDeliveryResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeliveryStatusScheduler {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public DeliveryStatusScheduler(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    // 1분마다 배달 상태 확인
    @Scheduled(fixedRate = 60000)
    public void checkDeliveryStatus() {
        List<Order> orders = orderRepository.findAllByStatus(Status.REQUEST_DELIVERY);
        orders.addAll(orderRepository.findAllByStatus(Status.DELIVERING));

        for (Order order : orders) {
            orderService.checkAndUpdateDeliveryStatus(order.getId());
        }
    }

}