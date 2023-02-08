package com.example.wpct.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysLogAnnotation {

    String opModel() default ""; // 操作模块

    String opType() default "";  // 操作类型

    String opDesc() default "";  // 操作说明

}
