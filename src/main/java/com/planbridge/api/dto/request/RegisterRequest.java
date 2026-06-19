package com.planbridge.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "username은 필수입니다.")
    private String username;

    @NotBlank(message = "password는 필수입니다.")
    private String password;

    @Pattern(regexp = "ADMIN|PLANNER|DEVELOPER", message = "role은 ADMIN, PLANNER, DEVELOPER 중 하나여야 합니다.")
    private String role = "DEVELOPER";
}
