package com.ccjiuhong.util;

import java.lang.annotation.*;

/**
 * 标注在某个方法上，表明该方法经过了测试
 *
 * <p>这是在单元测试不充分情况下的权宜之计，治本的办法是补全单元测试</p>
 *
 * @author G.Seinfeld
 * @date 2019/07/03
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Tested {
    /**
     * 测试是否通过
     *
     * @return 通过为true，否则为false
     */
    boolean passed() default false;
}
