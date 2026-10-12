package com.paytaxihub.repository;
import com.paytaxihub.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TripRepository extends JpaRepository<Trip,Integer> {
 List<Trip> findByCommuter_UserIdOrderByStartTimeDesc(Integer userId);
 List<Trip> findByDriver_DriverIdOrderByStartTimeDesc(Integer driverId);
 List<Trip> findByDriver_DriverIdAndTripStatusOrderByStartTimeAsc(Integer driverId, Trip.TripStatus status);
}
