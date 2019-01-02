package com.lark.cloud.utils.base.entity;

import lombok.Data;

/**
 * 带TOKEN验证的接口标准入参定义
 * @date 2018-12
 * @author xc.li
 */
@Data
public class BaseParamEntity<E> {
    private String token;
    private E param;
}
