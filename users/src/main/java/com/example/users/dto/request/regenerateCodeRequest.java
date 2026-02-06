package com.example.users.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class regenerateCodeRequest {
    private String email;
}
