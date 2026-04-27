package com.example.eventhub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface NotifikasiReadRepository extends JpaRepository<NotifikasiRead, Long> {

    Optional<NotifikasiRead> findByUserIdAndNotifikasiId(Long userId, Long notifikasiId);

    // Ambil ID notifikasi yang sudah dibaca user
    @Query("SELECT nr.notifikasiId FROM NotifikasiRead nr WHERE nr.userId = :userId AND nr.sudahDibaca = true AND nr.dihapus = false")
    List<Long> findReadIdsByUserId(@Param("userId") Long userId);

    // Ambil ID notifikasi yang sudah dihapus user
    @Query("SELECT nr.notifikasiId FROM NotifikasiRead nr WHERE nr.userId = :userId AND nr.dihapus = true")
    List<Long> findDeletedIdsByUserId(@Param("userId") Long userId);

    // Tandai satu notifikasi sebagai dibaca (INSERT or UPDATE)
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO notifikasi_read (user_id, notifikasi_id, sudah_dibaca, dihapus)
        VALUES (:userId, :notifId, true, false)
        ON DUPLICATE KEY UPDATE sudah_dibaca = true
        """, nativeQuery = true)
    void markAsRead(@Param("userId") Long userId, @Param("notifId") Long notifId);

    // Tandai satu notifikasi sebagai dihapus untuk user ini
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO notifikasi_read (user_id, notifikasi_id, sudah_dibaca, dihapus)
        VALUES (:userId, :notifId, false, true)
        ON DUPLICATE KEY UPDATE dihapus = true
        """, nativeQuery = true)
    void markAsDeleted(@Param("userId") Long userId, @Param("notifId") Long notifId);
}