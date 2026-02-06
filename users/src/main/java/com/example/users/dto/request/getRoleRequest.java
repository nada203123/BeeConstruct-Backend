package com.example.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class getRoleRequest {
    @NotBlank(message = "Email is required")
    private String email;
}
