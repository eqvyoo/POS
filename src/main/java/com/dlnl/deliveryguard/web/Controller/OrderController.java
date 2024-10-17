package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.service.OrderService;
import com.dlnl.deliveryguard.service.UserService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}