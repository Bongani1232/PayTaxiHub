package com.paytaxihub.repository;
import com.paytaxihub.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WalletRepository extends JpaRepository<Wallet,Integer> {
 Optional<Wallet> findByUser_UserId(Integer userId);
}
