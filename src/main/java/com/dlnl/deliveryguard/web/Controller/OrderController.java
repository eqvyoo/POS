package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.service.DeliveryPlatformService;
import com.dlnl.deliveryguard.service.OrderService;
import com.dlnl.deliveryguard.service.UserService;
import com.dlnl.deliveryguard.web.DTO.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    private final UserService userService;

    private final DeliveryPlatformService deliveryPlatformService;

    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<OrderListResponse>>> searchOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            OrderSearchCriteria criteria,
            @PageableDefault(size = 10) Pageable pageable,
            PagedResourcesAssembler<OrderListResponse> assembler) {

        try {
            Long userId = userService.getUserIdFromUserDetails(userDetails);
            User user = userService.findUserById(userId);

            Page<OrderListResponse> orders = orderService.searchOrders(criteria, pageable, user);

            PagedModel<EntityModel<OrderListResponse>> pagedModel = assembler.toModel(orders);

            return ResponseEntity.ok(pagedModel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = userService.getUserIdFromUserDetails(userDetails);
            User user = userService.findUserById(userId);

            OrderDetailResponse orderDetail = orderService.getOrderDetail(orderId, user);
            return ResponseEntity.ok(orderDetail);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(
            @RequestBody OrderCreateRequest orderCreateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = userService.getUserIdFromUserDetails(userDetails);
            User user = userService.findUserById(userId);

            orderService.createOrder(orderCreateRequest, user);

            return ResponseEntity.status(HttpStatus.CREATED).body("Order created successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create order: " + e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelOrder(
            @RequestBody CancelOrderRequest cancelOrderRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long userId = userService.getUserIdFromUserDetails(userDetails);
            User user = userService.findUserById(userId);

            orderService.cancelOrder(cancelOrderRequest.getOrderId(), cancelOrderRequest.getCancelReason(), user);

            return ResponseEntity.ok("주문이 성공적으로 취소되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("해당 주문을 취소할 수 없습니다. 사유 : " + e.getMessage());
        }
    }

    // 고객 호출 (포장 주문)
    @PutMapping("/{orderId}/call")
    public ResponseEntity<String> callCustomer(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 현재 사용자 정보 가져오기
            Long userId = userService.getUserIdFromUserDetails(userDetails);
            User user = userService.findUserById(userId);

            // 고객 호출 처리
            orderService.callCustomer(orderId, user);

            return ResponseEntity.ok("고객 호출이 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("고객 호출 중 오류가 발생했습니다.");
        }
    }

//    @PostMapping("/rider-call")
//    public ResponseEntity<String> callRider(
//            @RequestBody RiderCallRequest riderCallRequest,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        try {
//            Long userId = userService.getUserIdFromUserDetails(userDetails);
//            User user = userService.findUserById(userId);
//
//            orderService.callRider(riderCallRequest, user);
//
//            return ResponseEntity.ok("라이더 호출 요청이 성공적으로 처리되었습니다.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("라이더 호출 요청 처리 중 오류 발생: " + e.getMessage());
//        }
//    }

}
