package com.art.sensitive;

import com.art.annotation.Sensitive;
import com.art.enums.SensitiveStrategy;

import java.time.LocalDateTime;

public class UserTest {
    @Sensitive(strategy = SensitiveStrategy.MASK_ALL)
    private String password;
    @Sensitive(strategy = SensitiveStrategy.MASK_PHONE)
    private String phone;
    @Sensitive(strategy = SensitiveStrategy.MASK_EMAIL)
    private String email;
    private LocalDateTime createTime;
    @Sensitive(strategy = SensitiveStrategy.MASK_ID_CARD)
    private String idCard;
    @Sensitive(strategy = SensitiveStrategy.MASK_BANK_CARD)
    private String bankCardId;
    @Sensitive(strategy = SensitiveStrategy.CUSTOM, prefixLen = 3, suffixLen = 1)
    private String customSensitive;

    @Override
    public String toString() {
        return "UserTest{" +
                "password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", createTime=" + createTime +
                ", idCard='" + idCard + '\'' +
                ", bankCardId='" + bankCardId + '\'' +
                ", customSensitive='" + customSensitive + '\'' +
                '}';
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getBankCardId() {
        return bankCardId;
    }

    public void setBankCardId(String bankCardId) {
        this.bankCardId = bankCardId;
    }

    public String getCustomSensitive() {
        return customSensitive;
    }

    public void setCustomSensitive(String customSensitive) {
        this.customSensitive = customSensitive;
    }
}
