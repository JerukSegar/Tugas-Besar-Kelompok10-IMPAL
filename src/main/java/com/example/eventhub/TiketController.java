package com.example.eventhub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/tiket")
@CrossOrigin(origins = "*")
public class TiketController {

    @Autowired private TiketRepository tiketRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private PendaftaranRepository pendaftaranRepository;
    @Autowired private SertifikatRepository sertifikatRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/pendaftaran/{pendaftaranId}")
    public Map<String, Object> getTiketByPendaftaran(@PathVariable Long pendaftaranId) {
        List<Tiket> list = tiketRepository.findByPendaftaranId(pendaftaranId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tiket", list);
        return result;
    }

    @GetMapping("/qr/{kodeQr}")
    public Map<String, Object> getTiketByQr(@PathVariable String kodeQr) {
        Optional<Tiket> tiketOpt = tiketRepository.findByKodeQr(kodeQr);
        if (tiketOpt.isEmpty()) {
            return Map.of("success", false, "message", "Tiket tidak ditemukan!");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tiket", tiketOpt.get());
        result.put("sudahCheckIn", checkInRepository.existsByTiketId(tiketOpt.get().getId()));
        return result;
    }

    @PostMapping("/buat")
    public Map<String, Object> buatTiket(@RequestBody Map<String, Object> body) {
        Long pendaftaranId = Long.parseLong(body.get("pendaftaranId").toString());
        String kodeTiket   = body.get("kodeTiket").toString();

        List<Tiket> existing = tiketRepository.findByPendaftaranId(pendaftaranId);
        if (!existing.isEmpty()) {
            return Map.of("success", false, "message", "Tiket sudah pernah dibuat!");
        }

        Tiket tiket = new Tiket();
        tiket.setPendaftaranId(pendaftaranId);
        tiket.setKodeQr("QR-" + kodeTiket);
        tiket.setStatusTiket("aktif");
        tiketRepository.save(tiket);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Tiket berhasil dibuat!");
        result.put("kodeQr",  tiket.getKodeQr());
        return result;
    }

@Transactional
    @PostMapping("/checkin")
    public Map<String, Object> checkIn(@RequestBody Map<String, Object> body) {
        // 1. Validasi input awal untuk mencegah NullPointerException saat memanggil .toString()
        if (body.get("kodeQr") == null || body.get("penyelenggaraId") == null) {
            return Map.of("success", false, "message", "Field kodeQr atau penyelenggaraId tidak ditemukan dalam request!");
        }

        try {
            // 2. Konversi data dari body request
            String kodeQr = body.get("kodeQr").toString();
            Long penyelenggaraId = Long.parseLong(body.get("penyelenggaraId").toString());

            // 3. Cari tiket berdasarkan kode QR yang dikirim
            Optional<Tiket> tiketOpt = tiketRepository.findByKodeQr(kodeQr);
            if (tiketOpt.isEmpty()) {
                return Map.of("success", false, "message", "Tiket tidak ditemukan!");
            }

            Tiket tiket = tiketOpt.get();

            // 4. Validasi status tiket agar tidak terjadi check-in ganda
            if ("digunakan".equals(tiket.getStatusTiket())) {
                return Map.of("success", false, "message", "Tiket sudah digunakan!");
            }
            if ("kadaluarsa".equals(tiket.getStatusTiket())) {
                return Map.of("success", false, "message", "Tiket sudah kadaluarsa!");
            }

            // 5. Cek keberadaan data di tabel check-in untuk keamanan ekstra
            if (checkInRepository.existsByTiketId(tiket.getId())) {
                return Map.of("success", false, "message", "Peserta sudah terdata melakukan check-in!");
            }

            // 6. Simpan data kehadiran ke tabel check_in
            CheckIn checkIn = new CheckIn();
            checkIn.setTiketId(tiket.getId());
            checkIn.setPenyelenggaraId(penyelenggaraId);
            checkInRepository.save(checkIn);

            // 7. Ubah status tiket menjadi digunakan
            tiket.setStatusTiket("digunakan");
            tiketRepository.save(tiket);

            // 8. Logika pembuatan sertifikat otomatis setelah sukses check-in
            String kodeSertifikat = null;
            Optional<Pendaftaran> pendOpt = pendaftaranRepository.findById(tiket.getPendaftaranId());

            if (pendOpt.isPresent()) {
                Pendaftaran pend = pendOpt.get();
                Long userId = pend.getUserId();
                Long eventId = pend.getEventId();

                if (!sertifikatRepository.existsByUserIdAndEventId(userId, eventId)) {
                    Optional<User> userOpt = userRepository.findById(userId);
                    Optional<Event> evOpt = eventRepository.findById(eventId);

                    if (userOpt.isPresent() && evOpt.isPresent()) {
                        User user = userOpt.get();
                        Event ev = evOpt.get();
                        String kode = "CERT-" + eventId + "-" + userId + "-" + java.time.Year.now().getValue();

                        Sertifikat sert = new Sertifikat();
                        sert.setUserId(userId);
                        sert.setEventId(eventId);
                        sert.setKodeSertifikat(kode);
                        sert.setNamaPeserta(user.getNama());
                        sert.setNamaEvent(ev.getNama());
                        sert.setTanggalEvent(ev.getTanggal());
                        sert.setPenyelenggara(ev.getPenyelenggara());
                        sertifikatRepository.save(sert);
                        kodeSertifikat = kode;
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Check-in berhasil! Sertifikat telah diterbitkan.");
            if (kodeSertifikat != null) result.put("kodeSertifikat", kodeSertifikat);
            return result;

        } catch (NumberFormatException e) {
            return Map.of("success", false, "message", "ID penyelenggara harus berupa angka valid!");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Gagal memproses check-in: " + e.getMessage());
        }
    }

    @GetMapping("/semua")
    public List<Tiket> getSemuaTiket() {
        return tiketRepository.findAll();
    }
}