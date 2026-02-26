package dev.kiptoo.DarajaAPI.service;

import dev.kiptoo.DarajaAPI.dto.*;

public interface DarajaApiService {

    AccessTokenResponse getAccessToken();

    STKPushResponse initiateSTKPush(STKPushRequest request);

    STKPushResponse performSTKPush(String phoneNumber, String amount, String accountReference, String description);

    String initiateB2C(B2CRequest request);

    String queryTransactionStatus(TransactionStatusRequest request);

    void processCallback(CallbackResponse callback);

    void processResult(String result);
}