package dev.kiptoo.DarajaAPI.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionStatusRequest {

    @JsonProperty("Initiator")
    private String initiator;

    @JsonProperty("SecurityCredential")
    private String securityCredential;

    @JsonProperty("CommandID")
    private String commandID;

    @JsonProperty("TransactionID")
    private String transactionID;

    @JsonProperty("PartyA")
    private String partyA;

    @JsonProperty("IdentifierType")
    private String identifierType;

    @JsonProperty("ResultURL")
    private String resultURL;

    @JsonProperty("QueueTimeOutURL")
    private String queueTimeOutURL;

    @JsonProperty("Remarks")
    private String remarks;

    @JsonProperty("Occasion")
    private String occasion;
}