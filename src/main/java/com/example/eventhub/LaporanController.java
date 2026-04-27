package com.example.eventhub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/laporan")
@CrossOrigin(origins = "*")
public class LaporanController {

    @Autowired private LaporanRepository laporanRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private PendaftaranRepository pendaftaranRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private TiketRepository tiketRepository;

@GetMapping("/event/{eventId}")
    public Map<String, Object> getLaporanByEvent(@PathVariable Long eventId) {
        Optional<Event> evOpt = eventRepository.findById(eventId);
        if (evOpt.isEmpty()) {
            return Map.of("success", false, "message", "Event tidak ditemukan!");
        }

        // Hitung total pendaftar
        List<Pendaftaran> pendaftaranList = pendaftaranRepository.findAll()
            .stream().filter(p -> p.getEventId().equals(eventId)).toList();
        int totalPendaftar = pendaftaranList.size();

        // Hitung total kehadiran berdasarkan tiket yang sudah digunakan
        int totalHadir = 0;
        for (Pendaftaran p : pendaftaranList) {
            List<Tiket> tikets = tiketRepository.findByPendaftaranId(p.getId());
            totalHadir += tikets.stream().filter(t -> t.getStatusTiket().equals("digunakan")).count();
        }

        // PERBAIKAN: Simpan hasil perhitungan ke database sesuai Sequence Diagram
        Laporan laporan = new Laporan();
        laporan.setEventId(eventId);
        laporan.setTotalPendaftar(totalPendaftar);
        laporan.setTotalHadir(totalHadir);
        laporanRepository.save(laporan);

        return Map.of(
            "success",        true,
            "message",        "Laporan berhasil dihitung dan disimpan!",
            "totalPendaftar", totalPendaftar,
            "totalHadir",     totalHadir,
            "laporanId",      laporan.getId()
        );
    }

    @GetMapping("/semua")
    public List<Laporan> getSemuaLaporan() {
        return laporanRepository.findAll();
    }

    @PostMapping("/generate/{eventId}")
    public Map<String, Object> generateLaporan(@PathVariable Long eventId) {
        Optional<Event> evOpt = eventRepository.findById(eventId);
        if (evOpt.isEmpty()) {
            return Map.of("success", false, "message", "Event tidak ditemukan!");
        }

        List<Pendaftaran> pendaftaranList = pendaftaranRepository.findAll()
            .stream().filter(p -> p.getEventId().equals(eventId)).toList();
        int totalPendaftar = pendaftaranList.size();

        int totalHadir = 0;
        for (Pendaftaran p : pendaftaranList) {
            List<Tiket> tikets = tiketRepository.findByPendaftaranId(p.getId());
            totalHadir += tikets.stream().filter(t -> t.getStatusTiket().equals("digunakan")).count();
        }

        Laporan laporan = new Laporan();
        laporan.setEventId(eventId);
        laporan.setTotalPendaftar(totalPendaftar);
        laporan.setTotalHadir(totalHadir);
        laporanRepository.save(laporan);

        return Map.of(
            "success",        true,
            "message",        "Laporan berhasil digenerate!",
            "totalPendaftar", totalPendaftar,
            "totalHadir",     totalHadir
        );
    }

    @GetMapping("/penyelenggara/{userId}")
    public List<Map<String, Object>> getLaporanPenyelenggara(@PathVariable Long userId) {
        List<Event> events = eventRepository.findByCreatedBy(userId);
        List<Map<String, Object>> hasil = new ArrayList<>();

        for (Event ev : events) {
            List<Pendaftaran> daftarList = pendaftaranRepository.findAll()
                .stream().filter(p -> p.getEventId().equals(ev.getId())).toList();
            int totalPendaftar = daftarList.size();

            int totalHadir = 0;
            for (Pendaftaran p : daftarList) {
                List<Tiket> tikets = tiketRepository.findByPendaftaranId(p.getId());
                totalHadir += tikets.stream().filter(t -> t.getStatusTiket().equals("digunakan")).count();
            }

            Map<String, Object> item = new HashMap<>();
            item.put("eventId",        ev.getId());
            item.put("namaEvent",      ev.getNama());
            item.put("tanggal",        ev.getTanggal());
            item.put("kapasitas",      ev.getKapasitas());
            item.put("totalPendaftar", totalPendaftar);
            item.put("totalHadir",     totalHadir);
            hasil.add(item);
        }
        return hasil;
    }
}