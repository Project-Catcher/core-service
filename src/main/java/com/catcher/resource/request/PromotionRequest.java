package com.catcher.resource.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {
    @NotNull
    private Boolean isOn;

    public enum PromotionType {
        PHONE, EMAIL,
    }
}
