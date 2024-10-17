package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.*;
import com.dlnl.deliveryguard.web.DTO.OrderListResponse;
import com.dlnl.deliveryguard.web.DTO.OrderSearchCriteria;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
public class CustomOrderRepositoryImpl implements CustomOrderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<OrderListResponse> searchOrders(OrderSearchCriteria criteria, Pageable pageable, Long storeId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QOrder order = QOrder.order;
        QMenu menu = QMenu.menu;
        QCustomer customer = QCustomer.customer;
        QAddress address = QAddress.address1;
        QOrderMenu orderMenu = QOrderMenu.orderMenu;

        // fetch join을 사용하여 관련 데이터를 모두 가져옵니다.
        List<OrderListResponse> results = queryFactory
                .select(order)
                .from(order)
                .leftJoin(order.orderMenus, orderMenu).fetchJoin() // fetch join
                .leftJoin(orderMenu.menu, menu).fetchJoin() // fetch join
                .leftJoin(order.customer, customer).fetchJoin() // fetch join
                .leftJoin(order.address, address).fetchJoin() // fetch join
                .where(
                        order.store.id.eq(storeId),
                        orderDateTimeEq(order, criteria.getOrderDateTime()),
                        menuNameContains(order, criteria.getMenuName()),
                        customerPhoneNumberContains(order, criteria.getCustomerPhoneNumber()),
                        orderNumberContains(order, criteria.getOrderNumber()),
                        orderPlatformEq(order, criteria.getOrderPlatform()),
                        paymentMethodEq(order, criteria.getPaymentMethod()),
                        orderTypeEq(order, criteria.getOrderType()),
                        statusEq(order, criteria.getStatus()),
                        paymentAmountEq(order, criteria.getPaymentAmount())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(o -> OrderListResponse.builder()
                        .orderDateTime(o.getOrderDateTime())
                        .menus(o.getOrderMenus().stream()
                                .map(om -> OrderListResponse.MenuWithQuantity.builder()
                                        .menuName(om.getMenu().getName())
                                        .quantity(om.getQuantity())
                                        .build())
                                .collect(Collectors.toList()))
                        .customerPhoneNumber(o.getCustomerPhoneNumber())
                        .customerNickname(o.getCustomer().getNickname())
                        .customerID(o.getCustomer().getCustomerID())
                        .orderNumber(o.getOrderNumber())
                        .orderPlatform(o.getOrderPlatform())
                        .paymentMethod(o.getPaymentMethod())
                        .orderType(o.getOrderType())
                        .status(o.getStatus())
                        .paymentAmount(o.getPaymentAmount())
                        .address(o.getAddress().getAddress())  // 주문에 연결된 주소 출력
                        .estimatedCookingTime(o.getEstimatedCookingTime())
                        .deliveryAgency(o.getDeliveryAgency())
                        .riderRequestTime(o.getRiderRequestTime())
                        .build())
                .collect(Collectors.toList());

        long total = queryFactory
                .selectFrom(order)
                .where(
                        order.store.id.eq(storeId),
                        orderDateTimeEq(order, criteria.getOrderDateTime()),
                        menuNameContains(order, criteria.getMenuName()),
                        customerPhoneNumberContains(order, criteria.getCustomerPhoneNumber()),
                        orderNumberContains(order, criteria.getOrderNumber()),
                        orderPlatformEq(order, criteria.getOrderPlatform()),
                        paymentMethodEq(order, criteria.getPaymentMethod()),
                        orderTypeEq(order, criteria.getOrderType()),
                        statusEq(order, criteria.getStatus()),
                        paymentAmountEq(order, criteria.getPaymentAmount())
                )
                .fetchCount();

        return new PageImpl<>(results, pageable, total);
    }
    private BooleanExpression orderDateTimeEq(QOrder order, LocalDateTime orderDateTime) {
        return orderDateTime != null ? order.orderDateTime.eq(orderDateTime) : null;
    }

    private BooleanExpression menuNameContains(QOrder order, String menuName) {
        if (menuName == null) {
            return null;
        }
        QOrderMenu orderMenu = QOrderMenu.orderMenu;
        QMenu menu = QMenu.menu;
        return JPAExpressions.selectOne()
                .from(orderMenu)
                .join(orderMenu.menu, menu)
                .where(menu.name.containsIgnoreCase(menuName)
                        .and(orderMenu.order.id.eq(order.id)))  // 주문과 메뉴의 관계를 확인
                .exists();
    }

    private BooleanExpression customerPhoneNumberContains(QOrder order, String phoneNumber) {
        return phoneNumber != null ? order.CustomerPhoneNumber.contains(phoneNumber) : null;
    }

    private BooleanExpression orderNumberContains(QOrder order, String orderNumber) {
        return orderNumber != null ? order.orderNumber.contains(orderNumber) : null;
    }

    private BooleanExpression orderPlatformEq(QOrder order, String orderPlatform) {
        return orderPlatform != null ? order.orderPlatform.eq(orderPlatform) : null;
    }

    private BooleanExpression paymentMethodEq(QOrder order, String paymentMethod) {
        return paymentMethod != null ? order.paymentMethod.eq(paymentMethod) : null;
    }

    private BooleanExpression orderTypeEq(QOrder order, OrderType orderType) {
        return orderType != null ? order.orderType.eq(orderType) : null;
    }

    private BooleanExpression statusEq(QOrder order, Status status) {
        return status != null ? order.status.eq(status) : null;
    }

    private BooleanExpression paymentAmountEq(QOrder order, String paymentAmount) {
        return paymentAmount != null ? order.paymentAmount.eq(paymentAmount) : null;
    }
}
