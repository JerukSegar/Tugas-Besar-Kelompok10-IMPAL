package com.example.eventhub;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface PendaftaranRepository extends JpaRepository<Pendaftaran, Long> {
    List<Pendaftaran> findByUserId(Long userId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    Pendaftaran findByUserIdAndEventId(Long userId, Long eventId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Pendaftaran p WHERE p.userId = :userId AND p.eventId = :eventId")
    void deleteByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);
}