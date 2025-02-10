package com.BookMyEvent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MerchantAccount {
  private String merchantAccount;
  private String orderReference;
  private String merchantSignature;
  private BigDecimal amount;
  private String currency;
  private String authCode;
  private String email;
  private String phone;
  private long createdDate;
  private long processingDate;
  private String cardPan;
  private String cardType;
  private String issuerBankCountry;
  private String issuerBankName;
  private String recToken;
  private String transactionStatus;
  private String reason;
  private int reasonCode;
  private BigDecimal fee;
  private String paymentSystem;
}
