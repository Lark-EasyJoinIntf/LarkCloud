package com.lark.cloud.utils.base.entity;

import lombok.Data;

@Data
public class BaseEntity<E> {
    private String token;
    private E data;
}
