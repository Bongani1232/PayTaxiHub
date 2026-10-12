package com.paytaxihub.repository;
import com.paytaxihub.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TransactionRepository extends JpaRepository<Transaction,Integer> {
 List<Transaction> findByWallet_WalletIdOrderByCreatedAtDesc(Integer walletId);
 List<Transaction> findByTrip_TripIdOrderByCreatedAtDesc(Integer tripId);
 List<Transaction> findByFromWallet_WalletIdOrToWallet_WalletIdOrderByCreatedAtDesc(Integer fromWalletId, Integer toWalletId);
}
