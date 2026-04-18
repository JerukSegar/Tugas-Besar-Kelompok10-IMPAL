package com.example.eventhub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/notifikasi")
@CrossOrigin(origins = "*")
public class NotifikasiController {

    @Autowired private NotifikasiRepository notifikasiRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private PendaftaranRepository pendaftaranRepository;
    @Autowired private NotifikasiReadRepository notifikasiReadRepository;

    // ===== BROADCAST ke semua peserta event =====
    @PostMapping("/broadcast")
    public Map<String, Object> broadcast(@RequestBody Map<String, Object> body) {
        Long eventId         = Long.parseLong(body.get("eventId").toString());
        Long penyelenggaraId = Long.parseLong(body.get("penyelenggaraId").toString());
        String judul         = body.get("judul").toString();
        String pesan         = body.get("pesan").toString();

        if (judul == null || judul.isBlank())
            return Map.of("success", false, "message", "Judul tidak boleh kosong!");
        if (pesan == null || pesan.isBlank())
            return Map.of("success", false, "message", "Pesan tidak boleh kosong!");

        Optional<Event> evOpt = eventRepository.findById(eventId);
        if (evOpt.isEmpty())
            return Map.of("success", false, "message", "Event tidak ditemukan!");

        Event ev = evOpt.get();
        if (!ev.getCreatedBy().equals(penyelenggaraId))
            return Map.of("success", false, "message", "Kamu tidak berhak broadcast event ini!");

        List<Pendaftaran> pesertaList = pendaftaranRepository.findAll()
            .stream().filter(p -> p.getEventId().equals(eventId)).toList();

        Notifikasi notif = new Notifikasi();
        notif.setEventId(eventId);
        notif.setPenyelenggaraId(penyelenggaraId);
        notif.setJudul(judul);
        notif.setPesan(pesan);
        notifikasiRepository.save(notif);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Broadcast berhasil dikirim ke " + pesertaList.size() + " peserta!");
        result.put("totalPeserta", pesertaList.size());
        result.put("notifikasiId", notif.getId());
        return result;
    }

    // ===== GET notifikasi by event =====
    @GetMapping("/event/{eventId}")
    public List<Notifikasi> getByEvent(@PathVariable Long eventId) {
        return notifikasiRepository.findByEventId(eventId);
    }

    // ===== GET notifikasi untuk peserta (berdasarkan event yang diikuti) =====
    @GetMapping("/peserta/{userId}")
    public List<Map<String, Object>> getForPeserta(@PathVariable Long userId) {
        List<Pendaftaran> pendaftaranList = pendaftaranRepository.findByUserId(userId);
        List<Map<String, Object>> hasil = new ArrayList<>();

        // Ambil semua ID notifikasi yang sudah dihapus user ini
        Set<Long> deletedIds = new HashSet<>(notifikasiReadRepository.findDeletedIdsByUserId(userId));
        // Ambil semua ID notifikasi yang sudah dibaca user ini
        Set<Long> readIds = new HashSet<>(notifikasiReadRepository.findReadIdsByUserId(userId));

        for (Pendaftaran p : pendaftaranList) {
            List<Notifikasi> notifs = notifikasiRepository.findByEventId(p.getEventId());
            Optional<Event> evOpt = eventRepository.findById(p.getEventId());
            String namaEvent = evOpt.isPresent() ? evOpt.get().getNama() : "-";

            for (Notifikasi n : notifs) {
                // Skip notifikasi yang sudah dihapus user ini
                if (deletedIds.contains(n.getId())) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("id",          n.getId());
                item.put("judul",       n.getJudul());
                item.put("pesan",       n.getPesan());
                item.put("namaEvent",   namaEvent);
                item.put("eventId",     n.getEventId());
                item.put("dibuatAt",    n.getDibuatAt());
                item.put("sudahDibaca", readIds.contains(n.getId()));
                hasil.add(item);
            }
        }

        hasil.sort((a, b) -> b.get("id").toString().compareTo(a.get("id").toString()));
        return hasil;
    }

    // ===== GET semua notifikasi oleh penyelenggara =====
    @GetMapping("/penyelenggara/{userId}")
    public List<Notifikasi> getByPenyelenggara(@PathVariable Long userId) {
        return notifikasiRepository.findByPenyelenggaraId(userId);
    }

    // ===== TANDAI SATU NOTIFIKASI DIBACA =====
    @PutMapping("/{notifId}/baca")
    public Map<String, Object> tandaiBaca(
            @PathVariable Long notifId,
            @RequestBody(required = false) Map<String, Object> body) {
        Long userId = null;
        if (body != null && body.get("userId") != null) {
            userId = Long.parseLong(body.get("userId").toString());
        }
        if (userId == null) return Map.of("success", true); // frontend pakai localStorage, ok

        notifikasiReadRepository.markAsRead(userId, notifId);
        return Map.of("success", true);
    }

    // ===== TANDAI SEMUA DIBACA =====
    @PutMapping("/baca-semua/{userId}")
    public Map<String, Object> tandaiSemuaBaca(@PathVariable Long userId) {
        List<Pendaftaran> pendaftaranList = pendaftaranRepository.findByUserId(userId);
        for (Pendaftaran p : pendaftaranList) {
            List<Notifikasi> notifs = notifikasiRepository.findByEventId(p.getEventId());
            for (Notifikasi n : notifs) {
                notifikasiReadRepository.markAsRead(userId, n.getId());
            }
        }
        return Map.of("success", true);
    }

    // ===== HAPUS SATU NOTIFIKASI =====
    // Jika ada userId = soft delete untuk peserta
    // Jika tidak ada userId = hard delete oleh penyelenggara
    @DeleteMapping("/{notifId}")
    public Map<String, Object> hapusNotif(
            @PathVariable Long notifId,
            @RequestParam(required = false) Long userId) {
        if (userId != null) {
            // Peserta: soft delete (hanya tersembunyi untuk user ini)
            notifikasiReadRepository.markAsDeleted(userId, notifId);
        } else {
            // Penyelenggara: hard delete dari database
            notifikasiRepository.deleteById(notifId);
        }
        return Map.of("success", true, "message", "Notifikasi dihapus");
    }

    // ===== HAPUS SEMUA NOTIFIKASI (untuk user ini saja) =====
    @DeleteMapping("/hapus-semua/{userId}")
    public Map<String, Object> hapusSemua(@PathVariable Long userId) {
        List<Pendaftaran> pendaftaranList = pendaftaranRepository.findByUserId(userId);
        for (Pendaftaran p : pendaftaranList) {
            List<Notifikasi> notifs = notifikasiRepository.findByEventId(p.getEventId());
            for (Notifikasi n : notifs) {
                notifikasiReadRepository.markAsDeleted(userId, n.getId());
            }
        }
        return Map.of("success", true, "message", "Semua notifikasi dihapus");
    }
}