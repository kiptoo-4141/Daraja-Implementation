package dev.kiptoo.DarajaAPI.util;

import org.springframework.stereotype.Component;

@Component
public class DarajaUtils {

    public String sanitizePhoneNumber(String phoneNumber) {
        // Remove any non-digit characters
        phoneNumber = phoneNumber.replaceAll("\\D", "");

        // Ensure it starts with 254
        if (phoneNumber.startsWith("0")) {
            phoneNumber = "254" + phoneNumber.substring(1);
        } else if (phoneNumber.startsWith("+")) {
            phoneNumber = phoneNumber.substring(1);
        } else if (!phoneNumber.startsWith("254")) {
            phoneNumber = "254" + phoneNumber;
        }

        return phoneNumber;
    }

    public String formatAmount(String amount) {
        // Remove any non-numeric characters except decimal point
        return amount.replaceAll("[^0-9.]", "");
    }
}