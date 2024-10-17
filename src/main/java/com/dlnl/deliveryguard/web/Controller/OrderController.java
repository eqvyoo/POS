package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.service.OrderService;
import com.dlnl.deliveryguard.service.UserService;
import com.dlnl.deliveryguard.web.DTO.OrderCreateRequest;
import com.dlnl.deliveryguard.web.DTO.OrderDetailResponse;
import com.dlnl.deliveryguard.web.DTO.OrderListResponse;
import com.dlnl.deliveryguard.web.DTO.OrderSearchCriteria;
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
}