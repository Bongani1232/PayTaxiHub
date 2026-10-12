package com.paytaxihub.repository;
import com.paytaxihub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface UserRepository extends JpaRepository<User,Integer> {
 Optional<User> findByEmail(String email);
 Optional<User> findByPhoneNumber(String phoneNumber);
}
