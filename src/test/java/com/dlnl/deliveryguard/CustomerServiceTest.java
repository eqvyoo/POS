package com.dlnl.deliveryguard;

import com.dlnl.deliveryguard.domain.*;
import com.dlnl.deliveryguard.repository.CustomerRepository;
import com.dlnl.deliveryguard.repository.OrderRepository;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.service.CustomerService;
import com.dlnl.deliveryguard.web.DTO.CustomerDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("CustomerService - getCustomerDetail 테스트")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mockito 초기화
    }

    @Nested
    @DisplayName("고객 상세 조회 성공 케이스")
    class GetCustomerDetailSuccessTests {

        @Test
        @DisplayName("고객 상세 정보를 정상적으로 조회")
        void getCustomerDetail_Success() {
            // Given
            String customerID = "customer123";
            Customer customer = Customer.builder()
                    .id(1L)
                    .nickname("John Doe")
                    .phoneNumber("010-1234-5678")
                    .addresses(Arrays.asList(
                            Address.builder().id(1L).destAddress("Seoul").build(),
                            Address.builder().id(2L).destAddress("Busan").build()
                    ))
                    .build();

            // Mocking
            when(customerRepository.findByCustomerID(customerID)).thenReturn(Optional.of(customer));

            // When
            CustomerDetailResponse response = customerService.getCustomerDetail(customerID);

            // Then
            assertNotNull(response);
            assertEquals("John Doe", response.getNickname());
            assertEquals("010-1234-5678", response.getPhoneNumber());
            assertEquals(2, response.getAddresses().size());
            assertTrue(response.getAddresses().contains("Seoul"));
            assertTrue(response.getAddresses().contains("Busan"));

            verify(customerRepository, times(1)).findByCustomerID(anyString());
        }
    }

    @Nested
    @DisplayName("고객 상세 조회 실패 케이스")
    class GetCustomerDetailFailureTests {

        @Test
        @DisplayName("존재하지 않는 고객을 조회할 때 RuntimeException 발생")
        void getCustomerDetail_CustomerNotFound() {
            // Given
            String customerID = "nonExistentCustomer";

            // Mocking
            when(customerRepository.findByCustomerID(customerID)).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                customerService.getCustomerDetail(customerID);
            });

            assertEquals("해당 고객을 찾을 수 없습니다.", exception.getMessage());
            verify(customerRepository, times(1)).findByCustomerID(anyString());
        }
    }

    @Test
    @DisplayName("정상적으로 고객의 전화번호가 업데이트됨")
    void updateCustomerPhoneNumber_Success() {
        // Given
        String orderNumber = "12345";
        String orderPlatform = "배달의민족";
        String newPhoneNumber = "01098765432";

        Customer customer = Customer.builder()
                .id(1L)
                .nickname("John Doe")
                .phoneNumber("01012345678")
                .build();

        Order order = Order.builder()
                .id(1L)
                .orderNumber(orderNumber)
                .orderPlatform(orderPlatform)
                .customer(customer)
                .contactless(false).build();

        // Mocking
        when(orderRepository.findByOrderNumberAndOrderPlatform(orderNumber, orderPlatform))
                .thenReturn(Optional.of(order));

        // When
        customerService.updateCustomerPhoneNumber(orderNumber, orderPlatform, newPhoneNumber);

        // Then
        assertEquals(newPhoneNumber, customer.getPhoneNumber());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("잘못된 전화번호 형식일 때 예외 발생")
    void updateCustomerPhoneNumber_InvalidPhoneNumber() {
        // Given
        String orderNumber = "12345";
        String orderPlatform = "배달의민족";
        String invalidPhoneNumber = "0123456789"; // 잘못된 번호

        Customer customer = Customer.builder()
                .id(1L)
                .nickname("John Doe")
                .phoneNumber("01012345678")
                .build();

        Order order = Order.builder()
                .id(1L)
                .orderNumber(orderNumber)
                .orderPlatform(orderPlatform)
                .customer(customer)
                .contactless(false)
                .build();

        // Mocking
        when(orderRepository.findByOrderNumberAndOrderPlatform(orderNumber, orderPlatform))
                .thenReturn(Optional.of(order));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.updateCustomerPhoneNumber(orderNumber, orderPlatform, invalidPhoneNumber);
        });

        assertEquals("잘못된 전화번호 형식입니다. 010으로 시작하는 번호여야 합니다.", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class)); // 저장 시도 없음
    }

}