package com.catcher.core.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAdditionalInfoRequest {
    @NotNull(message = "introduceContent should be not null")
    private String introduceContent;

    @NotNull
    private List<String> tags;
}
