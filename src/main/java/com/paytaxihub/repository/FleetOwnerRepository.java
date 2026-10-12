package com.paytaxihub.repository;
import com.paytaxihub.entity.FleetOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface FleetOwnerRepository extends JpaRepository<FleetOwner,Integer> {
 Optional<FleetOwner> findByUser_UserId(Integer userId);
}
