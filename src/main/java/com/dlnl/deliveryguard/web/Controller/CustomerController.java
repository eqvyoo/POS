package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.domain.Customer;
import com.dlnl.deliveryguard.service.CustomerService;
import com.dlnl.deliveryguard.web.DTO.CustomerDetailResponse;
import com.dlnl.deliveryguard.web.DTO.CustomerListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/list")
    public ResponseEntity<CustomerListResponse> getCustomers(
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        CustomerListResponse customerResponse = customerService.getCustomers(nickname, phoneNumber, address, pageable);
        return ResponseEntity.ok(customerResponse);
    }

    @GetMapping("/detail/{customerID}")
    public ResponseEntity<CustomerDetailResponse> getCustomerDetail(@PathVariable String customerID) {
        CustomerDetailResponse customerDetail = customerService.getCustomerDetail(customerID);
        return ResponseEntity.ok(customerDetail);
    }

    @PutMapping("/update-phone")
    public ResponseEntity<String> updateCustomerPhoneNumber(
            @RequestParam String orderNumber,
            @RequestParam String orderPlatform,
            @RequestParam String newPhoneNumber) {
        try {
            customerService.updateCustomerPhoneNumber(orderNumber, orderPlatform, newPhoneNumber);
            return ResponseEntity.ok("고객의 전화번호가 성공적으로 변경되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}