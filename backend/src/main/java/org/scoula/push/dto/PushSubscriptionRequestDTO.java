package org.scoula.push.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 브라우저 PushSubscription.toJSON() 그대로 받는 DTO (endpoint + keys.p256dh/auth)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscriptionRequestDTO {
    @NotBlank
    private String endpoint;

    @Valid
    @NotNull
    private Keys keys;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Keys {
        @NotBlank
        private String p256dh;

        @NotBlank
        private String auth;
    }
}
