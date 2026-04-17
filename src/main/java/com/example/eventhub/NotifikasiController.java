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
    @Autowired private UserRepository userRepository;

    // ===== BROADCAST ke semua peserta event =====
    @PostMapping("/broadcast")
    public Map<String, Object> broadcast(@RequestBody Map<String, Object> body) {
        Long eventId         = Long.parseLong(body.get("eventId").toString());
        Long penyelenggaraId = Long.parseLong(body.get("penyelenggaraId").toString());
        String judul         = body.get("judul").toString();
        String pesan         = body.get("pesan").toString();

        if (judul == null || judul.isBlank()) {
            return Map.of("success", false, "message", "Judul tidak boleh kosong!");
        }
        if (pesan == null || pesan.isBlank()) {
            return Map.of("success", false, "message", "Pesan tidak boleh kosong!");
        }

        Optional<Event> evOpt = eventRepository.findById(eventId);
        if (evOpt.isEmpty()) {
            return Map.of("success", false, "message", "Event tidak ditemukan!");
        }

        // Cek kepemilikan event
        Event ev = evOpt.get();
        if (!ev.getCreatedBy().equals(penyelenggaraId)) {
            return Map.of("success", false, "message", "Kamu tidak berhak broadcast event ini!");
        }

        // Hitung jumlah peserta yang terdaftar
        List<Pendaftaran> pesertaList = pendaftaranRepository.findAll()
            .stream().filter(p -> p.getEventId().equals(eventId)).toList();

        // Simpan notifikasi ke database
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

        for (Pendaftaran p : pendaftaranList) {
            List<Notifikasi> notifs = notifikasiRepository.findByEventId(p.getEventId());
            Optional<Event> evOpt = eventRepository.findById(p.getEventId());
            String namaEvent = evOpt.isPresent() ? evOpt.get().getNama() : "-";

            for (Notifikasi n : notifs) {
                Map<String, Object> item = new HashMap<>();
                item.put("id",        n.getId());
                item.put("judul",     n.getJudul());
                item.put("pesan",     n.getPesan());
                item.put("namaEvent", namaEvent);
                item.put("eventId",   n.getEventId());
                item.put("dibuatAt",  n.getDibuatAt());
                hasil.add(item);
            }
        }

        // Urutkan terbaru dulu
        hasil.sort((a, b) -> b.get("id").toString().compareTo(a.get("id").toString()));
        return hasil;
    }

    // ===== GET semua notifikasi oleh penyelenggara =====
    @GetMapping("/penyelenggara/{userId}")
    public List<Notifikasi> getByPenyelenggara(@PathVariable Long userId) {
        return notifikasiRepository.findByPenyelenggaraId(userId);
    }
}