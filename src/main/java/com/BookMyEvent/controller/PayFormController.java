package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.MerchantAccount;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.rmi.server.UID;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Hidden
@Slf4j
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayFormController {

  private final String merchantSecretKey = "07e12edf1d5f39eaf8b1b7fd029cd10f2b557c3e";
  private final String merchantLogin = "evently_book_vercel_app";

  private final String baseUrl = "https://secure.wayforpay.com/pay";
  String merchantAccount = merchantLogin;
  String merchantDomainName
      = "https://evently-book.vercel.app/";

  String orderReference = "DH"+ random();
  String orderDate = "1415379863";
  String amount = "1";
  String currency = "UAH";
  String productName = "Квиток на Подію Stand-up";
  String productPrice = "1";
  String productCount = "1";
  String serviceUrl = "http://localhost:8080/api/v1/pay/status";

  @PostMapping("/status")
  public ResponseEntity<AppResponse> payStatus(@RequestParam Map<String, String> payload) {
    AppResponse response = new AppResponse(
        HttpStatus.OK.value(), payload.toString());

    log.info("pay/status  {}", payload.toString());
    return ResponseEntity.ok(response);
  }

//  @PostMapping("/status")
//  public ResponseEntity<AppResponse> payStatus(@RequestParam(name="merchantAccount", required = false) String merchantAccount,
//                                               @RequestParam(name="orderReference",required = false) String orderReference,
//                                               @RequestParam(name="merchantSignature",required = false) String merchantSignature,
//                                               @RequestParam(name="amount",required = false) BigDecimal amount,
//                                               @RequestParam(required = false) String currency,
//                                               @RequestParam(required = false) String authCode,
//                                               @RequestParam(required = false) String email,
//                                               @RequestParam(required = false) String phone,
//                                               @RequestParam(required = false) Long createdDate,
//                                               @RequestParam(required = false) Long processingDate,
//                                               @RequestParam(required = false) String cardType,
//                                               @RequestParam(required = false) String issuerBankCountry,
//                                               @RequestParam(required = false) String issuerBankName,
//                                               @RequestParam(required = false) String recToken,
//                                               @RequestParam(required = false) String reason,
//                                               @RequestParam(required = false) String reasonCode,
//                                               @RequestParam(required = false) String fee,
//                                               @RequestParam(required = false) String paymentSystem
//  ) {
//    AppResponse response = new AppResponse(
//        HttpStatus.OK.value(), merchantAccount);
//
//    log.info("pay/status  {}",merchantAccount+" "+orderReference
//        +" "+merchantSignature+" "+amount+" "+currency+" "+authCode+" "+email+" "+
//        phone+" "+createdDate+" "+processingDate+" "+cardType+" "+issuerBankCountry+" "+issuerBankName+" "
//        +recToken+" "+reason+" "+reasonCode+" "+fee+" "+paymentSystem
//    );
//    return ResponseEntity.ok(response);
//  }

  @GetMapping("/form")
  public ModelAndView payForm(ModelMap model) {
    String url = createUrl(baseUrl, getParameterPay());
    String dataToSign = String.join(";",
        merchantAccount,
        merchantDomainName,
        orderReference,
        orderDate,
        amount,
        currency,
        productName,
        productCount,
        productPrice

    );

    String ms2 = generateSignature2(merchantSecretKey, dataToSign);
    model.addAttribute("url", baseUrl);
    model.addAttribute("orderDate", orderDate);
    model.addAttribute("merchantAccount", merchantLogin);
    model.addAttribute("merchantAuthType", "SimpleSignature");
    model.addAttribute("merchantDomainName", merchantDomainName);
    model.addAttribute("orderReference", orderReference);
    model.addAttribute("orderDate", orderDate);
    model.addAttribute("amount", amount);
    model.addAttribute("currency", currency);
    model.addAttribute("orderTimeout", "600");
    model.addAttribute("productName", productName);
    model.addAttribute("productPrice", productPrice);
    model.addAttribute("productCount", productCount);
    model.addAttribute("clientFirstName", "Василь");
    model.addAttribute("clientLastName", "Пібаренко");
    model.addAttribute("clientAddress", "пр. Науки, 12");
    model.addAttribute("clientCity", "Дніпро");
    model.addAttribute("clientEmail", "jj3564527@gmail.com");
    model.addAttribute("defaultPaymentSystem", "card");
    model.addAttribute("serviceUrl", serviceUrl);
    model.addAttribute("merchantSignature", ms2); // 004eb652bb4b2524282666ba2667d7d4
    System.out.println(dataToSign);
    System.out.println(url);
    System.out.println(ms2);
    return new ModelAndView("tour-form", model);
  }

  public Map<String, Object> getParameterPay() {
    List<String> listProductName = List.of("Квиток на Подію Stand-up");
    Map<String, Object> parameter = new LinkedHashMap<>();
    parameter.put("merchantAccount", "test_merch_n1");
    parameter.put("merchantDomainName", "https://evently-book.vercel.app/");
    parameter.put("merchantTransactionSecureType", "AUTO");
    parameter.put("merchantSignature", "");
    parameter.put("language", "UA");
    parameter.put("returnUrl", "https://evently-book.vercel.app/");
    parameter.put("serviceUrl", "localhost:8080/api/v1/pay/status");
    parameter.put("orderReference", "DH783023");
    parameter.put("orderDate", String.valueOf(LocalDateTime.now().getSecond()));
    parameter.put("amount", "1");
    parameter.put("currency", "UAH");
    parameter.put("orderLifetime", "600");
    parameter.put("productName[]", listProductName);
    parameter.put("productPrice[]", List.of("1"));
    parameter.put("productCount[]", List.of("1"));
    parameter.put("clientEmail", "jj3564527@gmail.com");
    return parameter;
  }

  public static String createUrl(String baseUrl, Map<String, Object> parameters) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);

    // Iterate through the map and add parameters
    parameters.forEach((key, value) -> {
      if (value instanceof List) {
        // If the value is a list, add each element separately
        for (Object item : (List<?>) value) {
          builder.queryParam(key, item);
        }
      } else {
        builder.queryParam(key, value);
      }
    });

    return builder.toUriString();
  }


  public static String generateSignature2(String secretKey, String params) {
    try {
      // 1. Формуємо рядок для підпису
      String stringToSign = params;

      // 2. Створюємо HMAC_MD5 генератор
      Mac mac = Mac.getInstance("HmacMD5");
      SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacMD5");
      mac.init(secretKeySpec);

      // 3. Генеруємо підпис
      byte[] digest = mac.doFinal(stringToSign.getBytes());

      // 4. Перетворюємо підпис на 16-річне шістнадцяткове число
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }

      return sb.toString();
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      e.printStackTrace();
      return null;
    }
  }

  public long random(){
    Random random = new Random();
    long randomNumber = 1_000_000_000_000L + (long) (random.nextDouble() * 9_000_000_000_000L);
    return randomNumber;
  }

}
