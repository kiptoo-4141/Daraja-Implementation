package dev.kiptoo.DarajaAPI.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String merchantRequestId;

    private String checkoutRequestId;

    private String transactionId;

    private String phoneNumber;

    private String amount;

    private String accountReference;

    private String transactionDesc;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String resultCode;

    private String resultDesc;

    private String mpesaReceiptNumber;

    private LocalDateTime transactionDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
}