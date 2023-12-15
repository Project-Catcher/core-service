package com.catcher.resource.request;

import com.catcher.core.domain.entity.enums.UserGender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoEditRequest {
    @NotNull
    private String nickname;
    @NotNull
    @JsonFormat(pattern = "yyyyMMdd")
    private Date birth;
    private UserGender gender;
}
