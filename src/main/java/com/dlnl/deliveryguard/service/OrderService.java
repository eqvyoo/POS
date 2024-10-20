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

import java.sql.Time;
import java.time.LocalDateTime;
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
                    .address(order.getAddress().getDestAddress())   // todo : dest_address_detail, dest_address_road, dest_address_detail_road 등을 추가하는 로직 추가해야함
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
                .filter(a -> a.getDestAddress().equals(orderCreateRequest.getAddress()))
                .findFirst()
                .orElseGet(() -> {
                    // 새로운 주소 생성 및 저장
                    Address newAddress = Address.builder()
                            .destAddress(orderCreateRequest.getAddress())
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
                .contactless(false).build();    // todo : 컨택리스 여부도 입력으로 받아야하도록 수정.
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

    @Transactional
    public void cancelOrder(Long orderId, String cancelReason, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. ID: " + orderId));

        if (!order.getStore().getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 주문에 대한 접근 권한이 없습니다.");
        }

        // 배달 플랫폼이 'vroong'인 경우만 배달 취소 API 호출
        if ("vroong".equalsIgnoreCase(order.getDeliveryAgency())) {
            String deliveryId = getDeliveryIdByOrderId(orderId);
            deliveryPlatformService.cancelDelivery(deliveryId);
        }

        order.updateStatus(Status.CANCELED);
        order.updateCancelReason(cancelReason);
        orderRepository.save(order);
    }

    public String getDeliveryIdByOrderId(Long orderId) {
        return orderRepository.findById(orderId)
                .map(Order::getDeliveryId)
                .orElseThrow(() -> new IllegalArgumentException("배달 ID를 찾을 수 없습니다. 주문 ID: " + orderId));
    }

    @Transactional
    public void callCustomer(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. ID: " + orderId));


        if (!order.getStore().getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 주문에 접근할 권한이 없습니다.");
        }

        // 주문 상태를 'CUSTOMER_CALL'로 변경
        order.updateStatus(Status.CUSTOMER_CALL);
        orderRepository.save(order);

        // todo : 연동된 배달 대행사 API로 고객 호출 요청을 보내는 로직 추가 필요
    }

    @Transactional
    public void acceptOrder(Long orderId, Time estimatedCookingTime, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. ID: " + orderId));

        if (!order.getStore().getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 주문에 대한 접근 권한이 없습니다.");
        }

        // 주문 상태를 'PROCESSING'으로 변경 및 조리 예상 시간 설정
        order = Order.builder()
                .id(order.getId())
                .orderDateTime(order.getOrderDateTime())
                .orderNumber(order.getOrderNumber())
                .orderPlatform(order.getOrderPlatform())
                .paymentMethod(order.getPaymentMethod())
                .orderType(order.getOrderType())
                .status(Status.PROCESSING)
                .paymentAmount(order.getPaymentAmount())
                .customer(order.getCustomer())
                .store(order.getStore())
                .address(order.getAddress())
                .estimatedCookingTime(estimatedCookingTime)  // 조리 예상 시간 설정
                .contactless(false).build();    // todo : contactless false로 기본으로 들어가도록 설정해두었으나, 입력으로 받아오게 추가해야함.

        orderRepository.save(order);
    }

    @Transactional
    public void rejectOrder(Long orderId, User user) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. 주문 ID: " + orderId));

        if (!order.getStore().getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 주문에 대한 접근 권한이 없습니다.");
        }

        order.updateStatus(Status.CANCELED);
        order.updateCancelReason("가게에서 주문 요청을 거절했습니다.");  // 거절 사유 고정

        orderRepository.save(order);
    }

    @Transactional
    public DeliverySubmitResponse handleRiderDeliveryRequest(User user, RiderCallRequest riderCallRequest) {
        // 주문 조회
        Order order = orderRepository.findById(riderCallRequest.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. 주문 ID: " + riderCallRequest.getOrderId()));

        // 가게 정보 가져오기
        Store store = order.getStore();
        Address address = order.getAddress();
        Customer customer = order.getCustomer();

        // 주문에 배달 대행사와 픽업 요청 시간을 설정
        order.updateDeliveryAgency(riderCallRequest.getDeliveryAgency());
        order.updatePickupIn(riderCallRequest.getPickupIn());

        // RiderDeliveryRequest 구성
        RiderDeliveryRequest riderDeliveryRequest = RiderDeliveryRequest.builder()
                .requestId(order.getOrderNumber())  // 주문 번호를 요청 ID로 사용
                .branchCode(store.getBranchCode())  // 가게의 지점 코드
                .senderPhone(store.getPhoneNumber())  // 상점 전화번호
                .destAddress(address.getDestAddress())  // 목적지 주소
                .destAddressDetail(address.getDestAddressDetail())  // 목적지 상세 주소
                .destAddressRoad(address.getDestAddressRoad())  // 도로명 주소
                .destAddressDetailRoad(address.getDestAddressDetailRoad())  // 도로명 상세 주소
                .destLat(address.getLatitude())  // 위도
                .destLng(address.getLongitude())  // 경도
                .paymentMethod("PREPAID")  // 결제 수단 (예시로 PREPAID)
                .deliveryValue(Integer.parseInt(order.getPaymentAmount()))  // 결제 금액
                .pickupIn(riderCallRequest.getPickupIn())  // 픽업 요청 시간
                .recipientPhone(customer.getPhoneNumber())  // 고객 전화번호
                .contactless(order.isContactless())  // 비대면 여부
                .clientOrderNo(order.getOrderNumber())  // 신청자 주문 번호
                .itemDetail(createItemDetails(order))  // 주문 메뉴 상세 정보
                .build();

        // 라이더 호출 시간 업데이트
        order.updateRiderRequestTime(LocalDateTime.now());
        orderRepository.save(order);

        // 배달 플랫폼에 요청 전송 (vroong 등 대행사별 처리)
        if ("VROONG".equalsIgnoreCase(order.getDeliveryAgency())) {
            try {
                return deliveryPlatformService.submitDelivery(riderDeliveryRequest);
            } catch (Exception e) {
                throw new IllegalArgumentException("배달 대행사 VROONG로 요청 실패: " + e.getMessage());
            }
        }

        throw new IllegalArgumentException("지원하지 않는 배달 대행사입니다: " + riderCallRequest.getDeliveryAgency());
    }

    private List<RiderDeliveryRequest.ItemDetail> createItemDetails(Order order) {
        return order.getOrderMenus().stream()
                .map(orderMenu -> RiderDeliveryRequest.ItemDetail.builder()
                        .type("ITEM")  // 기본적으로 상품 유형으로 설정
                        .name(orderMenu.getMenu().getName())  // 메뉴 이름
                        .quantity(orderMenu.getQuantity())  // 수량
                        .unitPrice(Integer.parseInt(orderMenu.getMenu().getPrice()))  // 단가
                        .stockCode(orderMenu.getMenu().getId().toString())  // 메뉴 ID를 재고 코드로 사용
                        .build())
                .collect(Collectors.toList());
    }
}
