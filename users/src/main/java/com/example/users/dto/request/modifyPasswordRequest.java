package com.example.users.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class modifyPasswordRequest {

    private String email;
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
