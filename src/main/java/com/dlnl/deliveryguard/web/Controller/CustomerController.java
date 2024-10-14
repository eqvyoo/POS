package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.domain.Customer;
import com.dlnl.deliveryguard.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/list")
    public ResponseEntity<Page<Customer>> getCustomers(
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "storeId") Long storeId,
            Pageable pageable) {

        Page<Customer> customers = customerService.getCustomers(nickname, phoneNumber, address, storeId, pageable);
        return ResponseEntity.ok(customers);
    }
}