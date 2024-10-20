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

import java.time.LocalDateTime;

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

    // 주문 요청 수락 처리 API
    @PutMapping("/{orderId}/accept")
    public ResponseEntity<String> acceptOrder(
            @PathVariable Long orderId,
            @RequestBody AcceptOrderRequest acceptOrderRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 현재 사용자의 정보 가져오기
            Long userId = userService.getUserIdFromUserDetails(userDetails);
            User user = userService.findUserById(userId);

            // 주문 요청 수락 처리
            orderService.acceptOrder(orderId, acceptOrderRequest.getEstimatedCookingTime(), user);

            return ResponseEntity.ok("주문이 성공적으로 수락되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 수락 중 오류가 발생했습니다.");
        }
    }

    // 주문요청 거절 - 가게에서 주문 요청을 거절하는 API
    @PutMapping("/{orderId}/reject")
    public ResponseEntity<String> rejectOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long userId = userService.getUserIdFromUserDetails(userDetails);
            User user = userService.findUserById(userId);

            orderService.rejectOrder(orderId, user);

            return ResponseEntity.ok("주문이 성공적으로 거절되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 거절 중 오류가 발생했습니다.");
        }
    }



    @PostMapping("/rider-request")
    public ResponseEntity<?> createRiderDeliveryRequest(
            @RequestBody RiderCallRequest riderCallRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long userId = userService.getUserIdFromUserDetails(userDetails);
            User user = userService.findUserById(userId);

            DeliverySubmitResponse deliverySubmitResponse = orderService.handleRiderDeliveryRequest(user, riderCallRequest);

            return ResponseEntity.ok(deliverySubmitResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("배달 요청에 실패했습니다: " + e.getMessage());
        }
    }

}

