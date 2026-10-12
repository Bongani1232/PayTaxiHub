package com.paytaxihub.repository;
import com.paytaxihub.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface VehicleRepository extends JpaRepository<Vehicle,Integer> {
 List<Vehicle> findByDriver_DriverId(Integer driverId);
}
