package com.dlnl.deliveryguard.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "store")
@Entity
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "branch_code", nullable = false)
    private String branchCode;  // 지점 코드

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;  // 상점 전화번호

    @Column(name = "address", nullable = false)
    private String address;  // 상점 주소
    @OneToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore  // owner 필드를 직렬화에서 제외
    private User owner;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"store"})
    @Builder.Default
    private List<Customer> customers = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"store"})
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}