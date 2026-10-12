package com.paytaxihub.repository;
import com.paytaxihub.entity.SOSEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SOSEventRepository extends JpaRepository<SOSEvent,Integer> {
 List<SOSEvent> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
}
