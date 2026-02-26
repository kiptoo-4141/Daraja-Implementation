package dev.kiptoo.DarajaAPI.repository;

import dev.kiptoo.DarajaAPI.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByMerchantRequestId(String merchantRequestId);

    Optional<Transaction> findByCheckoutRequestId(String checkoutRequestId);

    Optional<Transaction> findByMpesaReceiptNumber(String mpesaReceiptNumber);
}