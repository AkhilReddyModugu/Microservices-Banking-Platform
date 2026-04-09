package com.example.accountservices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankDto {
    private String responseCode;
    private String  responseMessage;
    private AccountInfo accountInfo;
}
