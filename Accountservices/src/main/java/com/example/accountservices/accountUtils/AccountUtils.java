package com.example.accountservices.accountUtils;

import java.time.Year;

public class AccountUtils {

    public static final String ACCOUNT_NOT_EXISTS_CODE = "010";
    public static final String ACCOUNT_NOT_EXISTS_MESSAGE = "THE ACCOUNT NOT EXISTS";

    public static final String ACCOUNT_EXISTS_CODE = "011";
    public static final String ACCOUNT_EXISTS_MESSAGE = "THE ACCOUNT IS ALREADY EXISTS";

    public static final String ACCOUNT_CREATION_CODE = "012";
    public static final String ACCOUNT_CREATION_MESSAGE = "ACCOUNT IS SUCCESSFULLY CREATED";

    public static final String ACCOUNT_DELETION_CODE = "019";
    public static final String ACCOUNT_DELETION_MESSAGE = "ACCOUNT DELETED SUCCESSFULLY";

    public static String generateAccountNumber() {
        Year currentYear = Year.now();
        int min=10000000;
        int max=99999999;
        int randomNum= (int) Math.floor(Math.random() *(max-min+1)+min);

        String year=String.valueOf(currentYear);
        String randomNumber=String.valueOf(randomNum);

        return year + randomNumber;
    }
}
