package com.example.users.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class verifyCodeRequest {
    private String email;
    private String otpCode;
}
