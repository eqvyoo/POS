package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.config.SecurityUtil;
import com.dlnl.deliveryguard.domain.*;
import com.dlnl.deliveryguard.repository.*;
import com.dlnl.deliveryguard.web.DTO.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final OrderMenuRepository orderMenuRepository;

    private final DeliveryPlatformService deliveryPlatformService;

    private final PagedResourcesAssembler<OrderListResponse> pagedResourcesAssembler;


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
            throw e;  // 예외는 컨트롤러에서 처리
        } catch (Exception e) {
            // 기타 예외 처리
            throw new RuntimeException("주문 상세 조회 중 오류가 발생했습니다.");
        }
    }
    @Transactional(readOnly = true)
    public Page<OrderListResponse> searchOrders(OrderSearchCriteria criteria, Pageable pageable, User user) {
        Long storeId = user.getStore().getId();
        return orderRepository.searchOrders(criteria, pageable, storeId);
    }
    @Transactional
    public void createOrder(OrderCreateRequest orderCreateRequest, User user) {
        // 현재 사용자 가게 확인
        Store store = user.getStore();
        if (store == null) {
            throw new IllegalStateException("사용자는 가게를 소유하지 않습니다.");
        }

        // 고객 정보 확인 또는 기본 정보로 고객 생성
        Customer customer = customerRepository.findByPhoneNumber(orderCreateRequest.getCustomerPhoneNumber())
                .orElseGet(() -> {
                    // 필요한 정보만으로 고객을 생성
                    Customer newCustomer = Customer.builder()
                            .phoneNumber(orderCreateRequest.getCustomerPhoneNumber())
                            .store(store)  // 가게와 연결
                            .nickname(orderCreateRequest.getCustomerPhoneNumber())  // 고객이름이 없을 경우 전화번호로 대체
                            .customerID("CUST_" + System.currentTimeMillis())  // 기본 ID 생성
                            .build();
                    return customerRepository.save(newCustomer);
                });

        // 고객의 주소 처리 (고객의 기존 주소가 없다면 새로 추가)
        Address address = customer.getAddresses().stream()
                .filter(a -> a.getAddress().equals(orderCreateRequest.getAddress()))
                .findFirst()
                .orElseGet(() -> {
                    // 새로운 주소 생성 및 저장
                    Address newAddress = Address.builder()
                            .address(orderCreateRequest.getAddress())
                            .customer(customer)
                            .build();
                    return addressRepository.save(newAddress);
                });

        // 새로운 주문 생성
        Order newOrder = Order.builder()
                .orderDateTime(orderCreateRequest.getOrderDateTime())
                .orderNumber(orderCreateRequest.getOrderNumber())
                .orderPlatform(orderCreateRequest.getOrderPlatform())
                .paymentMethod(orderCreateRequest.getPaymentMethod())
                .orderType(orderCreateRequest.getOrderType())
                .status(orderCreateRequest.getStatus())
                .paymentAmount(orderCreateRequest.getPaymentAmount())
                .store(store)
                .address(address)  // 주소 추가
                .receiptData(orderCreateRequest.getReceiptData())
                .deliveryId(orderCreateRequest.getDeliveryId())
                .customer(customer)  // 고객 연결
                .build();
        orderRepository.save(newOrder);
        // 메뉴 항목 추가
        List<OrderMenu> orderMenus = orderCreateRequest.getMenus().stream()
                .map(menuItem -> {
                    Menu menu = menuRepository.findByName(menuItem.getMenuName())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다: " + menuItem.getMenuName()));
                    // OrderMenu 생성 및 저장
                    OrderMenu orderMenu = OrderMenu.builder()
                            .order(newOrder)
                            .menu(menu)
                            .quantity(menuItem.getQuantity())
                            .build();
                    return orderMenuRepository.save(orderMenu);  // OrderMenu 저장
                }).collect(Collectors.toList());

    }



}