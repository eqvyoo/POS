package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.config.SecurityUtil;
import com.dlnl.deliveryguard.domain.Customer;
import com.dlnl.deliveryguard.domain.Store;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.repository.CustomerRepository;

import com.dlnl.deliveryguard.repository.StoreRepository;
import com.dlnl.deliveryguard.repository.UserRepository;
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

        // StoreDTO 생성 (한번만 생성)
        CustomerListResponse.StoreDTO storeDTO = CustomerListResponse.StoreDTO.builder()
                .id(store.getId())
                .storeName(store.getStoreName())
                .storeAddress(store.getStoreAddress())
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
}
