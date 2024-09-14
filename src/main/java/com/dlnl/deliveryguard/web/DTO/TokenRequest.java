package com.dlnl.deliveryguard.web.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequest {
    private String accessToken;
    private String refreshToken;
}