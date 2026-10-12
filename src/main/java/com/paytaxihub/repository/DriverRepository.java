package com.paytaxihub.repository;
import com.paytaxihub.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DriverRepository extends JpaRepository<Driver,Integer> {
 Optional<Driver> findByUser_UserId(Integer userId);
 Optional<Driver> findBySaIdNumber(String saIdNumber);
 Optional<Driver> findByPrdpNumber(String prdpNumber);
}
