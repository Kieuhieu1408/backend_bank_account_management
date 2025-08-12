package com.klb.transactionService.shared.utils;

import java.util.Random;

public class CardNumberGenerator {

    // Ví dụ sinh số 16 số bắt đầu bằng 4 (giống Visa)
    public String generate() {
        StringBuilder sb = new StringBuilder("4");
        Random rand = new Random();
        for (int i = 0; i < 15; i++) {
            sb.append(rand.nextInt(10));
        }
        // Có thể bổ sung thuật toán Luhn để hợp lệ hóa số thẻ nếu muốn chuẩn hơn
        return sb.toString();
    }
}

