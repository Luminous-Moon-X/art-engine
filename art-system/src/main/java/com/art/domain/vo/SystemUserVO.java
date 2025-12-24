package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 用户基础信息VO
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Data
public class SystemUserVO {
    /**
     * 用户名
     */
    @JsonProperty("USER_NAME")
    private String userName;
    /**
     * 用户名称
     */
    @JsonProperty("USER_ALL_NAME")
    private String userAllName;
    /**
     * 邮箱
     */
    @JsonProperty("EMAIL")
    private String email;
    /**
     * 电话
     */
    @JsonProperty("PHONE")
    private String phone;
    /**
     * 年龄
     */
    @JsonProperty("AGE")
    private String age;
    /**
     * 时区
     */
    @JsonProperty("TIMEZONE")
    private String timezone;
    /**
     * 语言
     */
    @JsonProperty("LANGUAGE")
    private String language;
    /**
     * 国家
     */
    @JsonProperty("COUNTRY")
    private String country;
    /**
     * 头像文件ID
     */
    @JsonProperty("HEADER_FILE_ID")
    private String headerFileId;
}
