package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.config.SecurityUtil;
import com.dlnl.deliveryguard.domain.*;
import com.dlnl.deliveryguard.repository.CustomerRepository;

import com.dlnl.deliveryguard.repository.OrderRepository;
import com.dlnl.deliveryguard.repository.StoreRepository;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.web.DTO.CustomerDetailResponse;
import com.dlnl.deliveryguard.web.DTO.CustomerListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    public CustomerListResponse getCustomers(String nickname, String phoneNumber, String address, Pageable pageable) {
        // 현재 로그인된 사용자의 loginID 또는 username을 통해 사용자 정보를 가져옴
        String currentLoginId = SecurityUtil.getCurrentUserLogin();
        User user = userRepository.findByLoginID(currentLoginId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        // 사용자의 store 정보 가져오기
        Store store = user.getStore();
        if (store == null) {
            throw new RuntimeException("해당 사용자에게 연결된 가게가 없습니다.");
        }

        CustomerListResponse.StoreDTO storeDTO = CustomerListResponse.StoreDTO.builder()
                .id(store.getId())
                .storeName(store.getName())
                .storeAddress(store.getAddress())
                .build();


        // 고객 목록 조회
        Page<Customer> customers = customerRepository.searchCustomersByStore(store.getId(), nickname, phoneNumber, address, pageable);

        // 고객 목록이 비어있는지 확인
        if (customers.isEmpty()) {
            throw new RuntimeException("해당 가게에 속한 고객이 없습니다.");
        }

        List<CustomerListResponse.CustomerDTO> customerDTOs = customers.map(customer ->
                CustomerListResponse.CustomerDTO.builder()
                        .nickname(customer.getNickname())
                        .phoneNumber(customer.getPhoneNumber())
                        .addresses(customer.getAddresses().stream()
                                .map(addressEntity -> CustomerListResponse.CustomerDTO.AddressDTO.builder()
                                        .id(addressEntity.getId())
                                        .address(addressEntity.getAddress())
                                        .build())
                                .collect(Collectors.toList()))
                        .build()
        ).getContent();

        return CustomerListResponse.builder()
                .store(storeDTO)  // Store 정보는 한번만 포함
                .customers(customerDTOs)  // 고객 목록
                .build();
    }

    public CustomerDetailResponse getCustomerDetail(String customerID) {
        Customer customer = customerRepository.findByCustomerID(customerID)
                .orElseThrow(() -> new RuntimeException("해당 고객을 찾을 수 없습니다."));

        List<String> addressList = customer.getAddresses().stream()
                .map(Address::getAddress)
                .collect(Collectors.toList());


        return CustomerDetailResponse.builder()
                .customerID(customer.getCustomerID())
                .nickname(customer.getNickname())
                .phoneNumber(customer.getPhoneNumber())
                .addresses(addressList)
                .build();
    }
    @Transactional
    public void updateCustomerPhoneNumber(String orderNumber, String orderPlatform, String newPhoneNumber) {
        // 주문번호와 주문 플랫폼을 기준으로 주문 조회
        Order order = orderRepository.findByOrderNumberAndOrderPlatform(orderNumber, orderPlatform)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다."));

        // 주문의 고객 정보 가져오기
        Customer customer = order.getCustomer();

        if (customer == null) {
            throw new RuntimeException("해당 주문에 연결된 고객이 없습니다.");
        }

        // 고객의 연락처를 010으로 변경
        if (newPhoneNumber != null && newPhoneNumber.startsWith("010")) {
            customer.updatePhoneNumber(newPhoneNumber);
            customerRepository.save(customer);
        } else {
            throw new IllegalArgumentException("잘못된 전화번호 형식입니다. 010으로 시작하는 번호여야 합니다.");
        }
    }

}
