package com.ccjiuhong.web;

import lombok.Data;

/**
 * ajax返回结果
 *
 * @author G. Seinfeld
 * @since 2020/02/04
 */
@Data
public class AjaxResult {
    private String code;
    private String message;
    private Object data;

    public static final String SUCCESS = "200";
    public static final String FAIL = "500";

    public static AjaxResult success(Object data) {
        AjaxResult result = new AjaxResult();
        result.setCode(SUCCESS);
        result.setMessage("请求成功");
        result.setData(data);
        return result;
    }

    public static AjaxResult success() {
        return success(null);
    }

    public static AjaxResult error(String message, Object data) {
        AjaxResult result = new AjaxResult();
        result.setCode(FAIL);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static AjaxResult error(String message) {
        return error(message, null);
    }

}
