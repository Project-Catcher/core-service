package com.catcher.resource.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {
    // true : on / false : off
    @NotNull
    private Boolean status;

    public enum PromotionType {
        PHONE, EMAIL,
    }
}
