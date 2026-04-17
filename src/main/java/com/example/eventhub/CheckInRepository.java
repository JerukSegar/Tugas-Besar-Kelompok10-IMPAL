package com.example.eventhub;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    List<CheckIn> findByTiketId(Long tiketId);
    boolean existsByTiketId(Long tiketId);
}