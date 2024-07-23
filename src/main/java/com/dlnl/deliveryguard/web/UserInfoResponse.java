package com.dlnl.deliveryguard.web;

import lombok.*;

import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {
    private Long id;
    private String username;
    private String isvalid;
    private Date subExpiredAt;
}
