package com.klb.transactionService.shared.utils;


import java.util.Random;

public class CVVGenerator {
    public String generate() {
        Random random = new Random();
        // Sinh ra một chuỗi 3 số, ví dụ 100 - 999
        int cvv = 100 + random.nextInt(900);
        return String.valueOf(cvv);
    }
}
