package dev.kiptoo.DarajaAPI.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "daraja")
public class DarajaConfig {

    private String authUrl;
    private String stkPushUrl;
    private String b2cUrl;
    private String transactionStatusUrl;
    private String accountBalanceUrl;

    private String consumerKey;
    private String consumerSecret;
    private String shortCode;
    private String passkey;
    private String callbackUrl;
    private String resultUrl;

    private String initiatorName;
    private String initiatorPassword;
    private String securityCredential;
}