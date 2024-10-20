package com.dlnl.deliveryguard.repository;
import com.dlnl.deliveryguard.domain.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;


import java.util.List;
@Repository
public class CustomCustomerRepositoryImpl implements CustomCustomerRepository {

    private final JPAQueryFactory queryFactory;

    public CustomCustomerRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }
    @Override
    public Page<Customer> searchCustomersByStore(Long storeId, String nickname, String phoneNumber, String address, Pageable pageable) {
        QCustomer customer = QCustomer.customer;
        QAddress customerAddress = QAddress.address;

        List<Customer> customers = queryFactory
                .selectFrom(customer)
                .leftJoin(customer.addresses, customerAddress)
                .where(
                        customer.store.id.eq(storeId),  // 사장님의 가게에 속한 고객만 조회
                        nicknameContains(nickname),
                        phoneNumberContains(phoneNumber),
                        addressContains(address)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(customer)
                .leftJoin(customer.addresses, customerAddress)
                .where(
                        customer.store.id.eq(storeId),
                        nicknameContains(nickname),
                        phoneNumberContains(phoneNumber),
                        addressContains(address)
                )
                .fetchCount();

        return new PageImpl<>(customers, pageable, total);
    }

    // 검색 조건 헬퍼 메서드는 그대로 유지
    private BooleanExpression nicknameContains(String nickname) {
        return StringUtils.hasText(nickname) ? QCustomer.customer.nickname.containsIgnoreCase(nickname) : null;
    }

    private BooleanExpression phoneNumberContains(String phoneNumber) {
        return StringUtils.hasText(phoneNumber) ? QCustomer.customer.phoneNumber.containsIgnoreCase(phoneNumber) : null;
    }

    private BooleanExpression addressContains(String address) {
        return StringUtils.hasText(address) ? QAddress.address.destAddress.containsIgnoreCase(address) : null;
    }
}