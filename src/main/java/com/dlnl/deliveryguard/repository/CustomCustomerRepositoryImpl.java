package com.dlnl.deliveryguard.repository;
import com.dlnl.deliveryguard.domain.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


import java.util.List;
@Repository
public class CustomCustomerRepositoryImpl implements CustomCustomerRepository {

    private final JPAQueryFactory queryFactory;

    public CustomCustomerRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Customer> searchCustomers(String nickname, String phoneNumber, String address, Long storeId, Pageable pageable) {
        QCustomer customer = QCustomer.customer;
        QAddress address1 = QAddress.address1;

        List<Customer> customers = queryFactory
                .selectFrom(customer)
                .leftJoin(customer.addresses, address1).fetchJoin()
                .where(
                        nickname != null ? customer.nickname.containsIgnoreCase(nickname) : null,
                        phoneNumber != null ? customer.phoneNumber.contains(phoneNumber) : null,
                        address != null ? address1.address.containsIgnoreCase(address) : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(customer)
                .leftJoin(customer.addresses, address1)
                .where(
                        nickname != null ? customer.nickname.containsIgnoreCase(nickname) : null,
                        phoneNumber != null ? customer.phoneNumber.contains(phoneNumber) : null,
                        address != null ? address1.address.containsIgnoreCase(address) : null
                )
                .fetchCount();

        return new PageImpl<>(customers, pageable, total);
    }
}