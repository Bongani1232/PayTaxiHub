package com.paytaxihub.repository;
import com.paytaxihub.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ShiftRepository extends JpaRepository<Shift,Integer> {
 List<Shift> findByDriver_DriverIdOrderByStartTimeDesc(Integer driverId);
 List<Shift> findByShiftStatusOrderByStartTimeDesc(Shift.ShiftStatus status);
 Optional<Shift> findFirstByDriver_DriverIdAndShiftStatusOrderByStartTimeDesc(Integer driverId, Shift.ShiftStatus status);
 Optional<Shift> findFirstByQrTokenAndShiftStatus(String qrToken, Shift.ShiftStatus status);
}
