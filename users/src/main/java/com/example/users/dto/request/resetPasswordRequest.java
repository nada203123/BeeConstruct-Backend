package com.example.users.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class resetPasswordRequest {
    private String email;
    private String newPassword;
    private String confirmPassword;
}
