package com.lark.cloud.utils.base.entity;

import lombok.Data;

@Data
public class BaseParamEntity<E> {
    private String token;
    private E param;
}
