package com.example.eventhub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired private EventRepository eventRepository;
    @Autowired private PendaftaranRepository pendaftaranRepository;
    @Autowired private TiketRepository tiketRepository;

    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/{id}")
    public Map<String, Object> getEventById(@PathVariable Long id) {
        Optional<Event> ev = eventRepository.findById(id);
        if (ev.isEmpty()) return Map.of("success", false, "message", "Event tidak ditemukan!");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("event", ev.get());
        return result;
    }

    @PostMapping
    public Map<String, Object> createEvent(@RequestBody Map<String, Object> body) {
        String nama          = (String) body.get("nama");
        String kategori      = (String) body.get("kategori");
        String tanggal       = (String) body.get("tanggal");
        String waktu         = (String) body.get("waktu");
        String lokasi        = (String) body.get("lokasi");
        String venue         = (String) body.get("venue");
        String deskripsi     = (String) body.get("deskripsi");
        String penyelenggara = (String) body.get("penyelenggara");
        String harga         = (String) body.get("harga");
        String tipeHarga     = (String) body.get("tipeHarga");
        int kapasitas        = Integer.parseInt(body.get("kapasitas").toString());
        Long createdBy       = Long.parseLong(body.get("createdBy").toString());

        if (nama == null || nama.isBlank() || kategori == null || tanggal == null) {
            return Map.of("success", false, "message", "Field wajib tidak boleh kosong!");
        }

        Event ev = new Event();
        ev.setNama(nama); ev.setKategori(kategori); ev.setTanggal(tanggal);
        ev.setWaktu(waktu); ev.setLokasi(lokasi); ev.setVenue(venue);
        ev.setDeskripsi(deskripsi); ev.setPenyelenggara(penyelenggara);
        ev.setHarga(harga); ev.setTipeHarga(tipeHarga);
        ev.setKapasitas(kapasitas); ev.setTerisi(0); ev.setCreatedBy(createdBy);
        eventRepository.save(ev);

        return Map.of("success", true, "message", "Event berhasil dibuat!", "id", ev.getId());
    }

@Transactional
    @PutMapping("/{id}")
    public Map<String, Object> updateEvent(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<Event> evOpt = eventRepository.findById(id);
        if (evOpt.isEmpty()) {
            return Map.of("success", false, "message", "Event tidak ditemukan!");
        }

        Event ev = evOpt.get();
        
        try {
            // Update field dasar jika ada dalam request body
            if (body.containsKey("nama"))          ev.setNama(body.get("nama").toString());
            if (body.containsKey("kategori"))      ev.setKategori(body.get("kategori").toString());
            if (body.containsKey("tanggal"))       ev.setTanggal(body.get("tanggal").toString());
            if (body.containsKey("waktu"))         ev.setWaktu(body.get("waktu").toString());
            if (body.containsKey("lokasi"))        ev.setLokasi(body.get("lokasi").toString());
            if (body.containsKey("venue"))         ev.setVenue(body.get("venue").toString());
            if (body.containsKey("deskripsi"))     ev.setDeskripsi(body.get("deskripsi").toString());
            if (body.containsKey("penyelenggara")) ev.setPenyelenggara(body.get("penyelenggara").toString());
            if (body.containsKey("harga"))         ev.setHarga(body.get("harga").toString());
            if (body.containsKey("tipeHarga"))     ev.setTipeHarga(body.get("tipeHarga").toString());
            
            // Validasi khusus untuk kapasitas
            if (body.containsKey("kapasitas")) {
                int newKapasitas = Integer.parseInt(body.get("kapasitas").toString());
                // Proteksi: Kapasitas baru tidak boleh lebih kecil dari jumlah peserta yang sudah mendaftar
                if (newKapasitas < ev.getTerisi()) {
                    return Map.of(
                        "success", false, 
                        "message", "Gagal update! Kapasitas baru (" + newKapasitas + 
                        ") lebih kecil dari jumlah peserta yang sudah mendaftar (" + ev.getTerisi() + ")."
                    );
                }
                ev.setKapasitas(newKapasitas);
            }

            eventRepository.save(ev);
            return Map.of("success", true, "message", "Data event berhasil diperbarui!");

        } catch (Exception e) {
            return Map.of("success", false, "message", "Error saat update: " + e.getMessage());
        }
    }

@Transactional
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteEvent(@PathVariable Long id) {
        Optional<Event> evOpt = eventRepository.findById(id);
        if (evOpt.isEmpty()) {
            return Map.of("success", false, "message", "Event tidak ditemukan!");
        }

        // LOGIKA PROTEKSI TAMBAHAN:
        // Cek apakah ada data pendaftaran yang merujuk ke event ini
        boolean hasParticipants = pendaftaranRepository.findAll()
                .stream()
                .anyMatch(p -> p.getEventId().equals(id));

        if (hasParticipants) {
            return Map.of(
                "success", false, 
                "message", "Event tidak bisa dihapus karena sudah ada peserta yang terdaftar! " +
                "Hapus semua data pendaftaran terkait terlebih dahulu untuk menjaga integritas data."
            );
        }

        try {
            eventRepository.deleteById(id);
            return Map.of("success", true, "message", "Event berhasil dihapus secara permanen.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Gagal menghapus event: " + e.getMessage());
        }
    }

    @GetMapping("/kelola/{userId}")
    public List<Event> getEventByPenyelenggara(@PathVariable Long userId) {
        return eventRepository.findByCreatedBy(userId);
    }

    @Transactional
    @PostMapping("/daftar")
    public Map<String, Object> daftarEvent(@RequestBody Map<String, Object> body) {
        Long userId  = Long.parseLong(body.get("userId").toString());
        Long eventId = Long.parseLong(body.get("eventId").toString());

        Optional<Event> evOpt = eventRepository.findById(eventId);
        if (evOpt.isEmpty()) return Map.of("success", false, "message", "Event tidak ditemukan!");

        Event ev = evOpt.get();
        if (ev.getTerisi() >= ev.getKapasitas())
            return Map.of("success", false, "message", "Event sudah penuh!");
        if (pendaftaranRepository.existsByUserIdAndEventId(userId, eventId))
            return Map.of("success", false, "message", "Kamu sudah terdaftar di event ini!");

        String kode = "EVH-" + java.time.Year.now().getValue() + "-"
                + String.format("%03d", userId) + String.format("%03d", eventId);

        Pendaftaran p = new Pendaftaran();
        p.setUserId(userId); p.setEventId(eventId);
        p.setKodeTiket(kode); p.setStatus("upcoming");
        pendaftaranRepository.save(p);

        Tiket tiket = new Tiket();
        tiket.setPendaftaranId(p.getId());
        tiket.setKodeQr("QR-" + kode);
        tiket.setStatusTiket("aktif");
        tiketRepository.save(tiket);

        ev.setTerisi(ev.getTerisi() + 1);
        eventRepository.save(ev);

        return Map.of("success", true, "message", "Berhasil mendaftar event!", "kodeTiket", kode, "kodeQr", "QR-" + kode);
    }

    @GetMapping("/saya/{userId}")
    public List<Map<String, Object>> getEventSaya(@PathVariable Long userId) {
        List<Pendaftaran> list = pendaftaranRepository.findByUserId(userId);
        return list.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            Optional<Event> evOpt = eventRepository.findById(p.getEventId());
            if (evOpt.isPresent()) {
                Event ev = evOpt.get();
                m.put("eventId", ev.getId());         m.put("nama", ev.getNama());
                m.put("kategori", ev.getKategori());  m.put("tanggal", ev.getTanggal());
                m.put("waktu", ev.getWaktu());        m.put("lokasi", ev.getLokasi());
                m.put("venue", ev.getVenue());        m.put("penyelenggara", ev.getPenyelenggara());
            }
            m.put("pendaftaranId", p.getId());
            m.put("kodeTiket", p.getKodeTiket());
            m.put("status", p.getStatus());
            return m;
        }).collect(Collectors.toList());
    }


    
    @Transactional
    @PostMapping("/batal")
    public Map<String, Object> batalDaftar(@RequestBody Map<String, Object> body) {
        Long userId  = Long.parseLong(body.get("userId").toString());
        Long eventId = Long.parseLong(body.get("eventId").toString());

        // Cari data pendaftaran
        Pendaftaran pendaftaran = pendaftaranRepository.findByUserIdAndEventId(userId, eventId);
        if (pendaftaran == null)
            return Map.of("success", false, "message", "Data pendaftaran tidak ditemukan!");

        // Blokir jika tiket sudah digunakan (sudah check-in)
        List<Tiket> tikets = tiketRepository.findByPendaftaranId(pendaftaran.getId());
        boolean sudahCheckIn = tikets.stream()
            .anyMatch(t -> "digunakan".equals(t.getStatusTiket()));
        if (sudahCheckIn) {
            return Map.of("success", false, "message", "Pendaftaran tidak dapat dibatalkan karena kamu sudah melakukan check-in!");
        }

        // Hapus tiket yang terkait
        if (!tikets.isEmpty()) {
            tiketRepository.deleteAll(tikets);
        }

        // Hapus pendaftaran dari database
        pendaftaranRepository.deleteByUserIdAndEventId(userId, eventId);

        // Kurangi jumlah terisi di event
        Optional<Event> evOpt = eventRepository.findById(eventId);
        evOpt.ifPresent(ev -> {
            ev.setTerisi(Math.max(0, ev.getTerisi() - 1));
            eventRepository.save(ev);
        });

        return Map.of("success", true, "message", "Pendaftaran berhasil dibatalkan!");
    }

    @GetMapping("/peserta/{eventId}")
    public Map<String, Object> getPesertaEvent(@PathVariable Long eventId) {
        Optional<Event> evOpt = eventRepository.findById(eventId);
        if (evOpt.isEmpty()) return Map.of("success", false, "message", "Event tidak ditemukan!");

        List<Pendaftaran> list = pendaftaranRepository.findAll()
            .stream().filter(p -> p.getEventId().equals(eventId)).collect(Collectors.toList());

        return Map.of("success", true, "total", list.size(), "peserta", list);
    }

    @Transactional
    @PostMapping("/sync-terisi")
    public Map<String, Object> syncTerisi() {
        List<Event> events = eventRepository.findAll();
        for (Event ev : events) {
            long jumlahPeserta = pendaftaranRepository.findAll()
                .stream().filter(p -> p.getEventId().equals(ev.getId())).count();
            ev.setTerisi((int) jumlahPeserta);
            eventRepository.save(ev);
        }
        return Map.of("success", true, "message", "Sinkronisasi selesai!", "totalEvent", events.size());
    }
}