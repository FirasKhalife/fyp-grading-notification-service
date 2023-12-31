package com.fypgrading.notificationservice.service.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum RoleEnum {

    @JsonProperty("ADMIN")
    ADMIN,

    @JsonProperty("JURY_MEMBER")
    JURY_MEMBER,

    @JsonProperty("ADVISOR")
    ADVISOR

}
