-- ============================================================
--  EventHub - Full SQL Dump (Semua Tabel + Dummy Data)
--  Database : eventhub
--  Tabel    : users, events, pendaftaran, tiket, check_in, laporan
--  Dibuat   : 2026-04-07
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- DROP semua tabel (urutan aman: child dulu, parent belakangan)
-- ============================================================
DROP TABLE IF EXISTS `check_in`;
DROP TABLE IF EXISTS `tiket`;
DROP TABLE IF EXISTS `laporan`;
DROP TABLE IF EXISTS `pendaftaran`;
DROP TABLE IF EXISTS `events`;
DROP TABLE IF EXISTS `users`;

-- ============================================================
-- 1. CREATE TABLE: users
-- ============================================================
CREATE TABLE `users` (
  `id`         INT          NOT NULL AUTO_INCREMENT,
  `nama`       VARCHAR(150) NOT NULL,
  `email`      VARCHAR(150) NOT NULL UNIQUE,
  `password`   VARCHAR(255) NOT NULL,
  `role`       ENUM('peserta','penyelenggara','admin') NOT NULL DEFAULT 'peserta',
  `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 2. CREATE TABLE: events
-- ============================================================
CREATE TABLE `events` (
  `id`               INT          NOT NULL AUTO_INCREMENT,
  `penyelenggara_id` INT          NOT NULL,
  `judul`            VARCHAR(255) NOT NULL,
  `deskripsi`        TEXT,
  `lokasi`           VARCHAR(255),
  `tanggal_mulai`    DATETIME     NOT NULL,
  `tanggal_selesai`  DATETIME     NOT NULL,
  `kuota`            INT          NOT NULL DEFAULT 100,
  `status`           ENUM('draft','aktif','selesai','dibatalkan') NOT NULL DEFAULT 'aktif',
  `created_at`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_event_penyelenggara`
    FOREIGN KEY (`penyelenggara_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. CREATE TABLE: pendaftaran
-- ============================================================
CREATE TABLE `pendaftaran` (
  `id`                 INT       NOT NULL AUTO_INCREMENT,
  `peserta_id`         INT       NOT NULL,
  `event_id`           INT       NOT NULL,
  `status_pendaftaran` ENUM('menunggu','dikonfirmasi','ditolak','dibatalkan') NOT NULL DEFAULT 'menunggu',
  `terdaftar_at`       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_pendaftaran_peserta`
    FOREIGN KEY (`peserta_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_pendaftaran_event`
    FOREIGN KEY (`event_id`) REFERENCES `events`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. CREATE TABLE: tiket
-- ============================================================
CREATE TABLE `tiket` (
  `id`             INT          NOT NULL AUTO_INCREMENT,
  `pendaftaran_id` INT          NOT NULL,
  `kode_qr`        VARCHAR(255) NOT NULL UNIQUE,
  `status_tiket`   ENUM('aktif','digunakan','dibatalkan') NOT NULL DEFAULT 'aktif',
  `diterbitkan_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tiket_pendaftaran`
    FOREIGN KEY (`pendaftaran_id`) REFERENCES `pendaftaran`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 5. CREATE TABLE: check_in
-- ============================================================
CREATE TABLE `check_in` (
  `id`               INT       NOT NULL AUTO_INCREMENT,
  `tiket_id`         INT       NOT NULL,
  `penyelenggara_id` INT       NOT NULL,
  `waktu_checkin`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_checkin_tiket`
    FOREIGN KEY (`tiket_id`) REFERENCES `tiket`(`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_checkin_penyelenggara`
    FOREIGN KEY (`penyelenggara_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 6. CREATE TABLE: laporan
-- ============================================================
CREATE TABLE `laporan` (
  `id`              INT       NOT NULL AUTO_INCREMENT,
  `event_id`        INT       NOT NULL UNIQUE,
  `total_pendaftar` INT       NOT NULL DEFAULT 0,
  `total_hadir`     INT       NOT NULL DEFAULT 0,
  `dibuat_at`       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_laporan_event`
    FOREIGN KEY (`event_id`) REFERENCES `events`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================
-- DUMMY DATA
-- ============================================================

-- ------------------------------------------------------------
-- INSERT: users
-- 3 penyelenggara (ID 1-3), 10 peserta (ID 4-13), 1 admin (ID 14)
-- Password semua: password123 (di-hash bcrypt)
-- ------------------------------------------------------------
INSERT INTO `users` (`id`, `nama`, `email`, `password`, `role`, `created_at`) VALUES
-- Penyelenggara
(1,  'TechCom Indonesia',    'techcom@eventhub.id',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'penyelenggara', '2025-10-01 08:00:00'),
(2,  'DesignLab Studio',     'designlab@eventhub.id',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'penyelenggara', '2025-10-05 09:00:00'),
(3,  'DevCommunity ID',      'devcommunity@eventhub.id','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'penyelenggara', '2025-10-10 10:00:00'),
-- Peserta
(4,  'Ahmad Bejo',           'ahmad@gmail.com',        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'peserta', '2025-11-01 08:00:00'),
(5,  'Siti Rahayu',          'siti@gmail.com',         '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'peserta', '2025-11-02 09:30:00'),
(6,  'Budi Santoso',         'budi@gmail.com',         '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'peserta', '2025-11-03 10:15:00'),
(7,  'Dewi Lestari',         'dewi@gmail.com',         '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'peserta', '2025-11-04 11:00:00'),
(8,  'Rizky Firmansyah',     'rizky@gmail.com',        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'peserta', '2025-11-05 08:45:00'),
(9,  'Nurul Hidayah',        'nurul@gmail.com',        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'peserta', '2025-11-06 09:00:00'),
(10, 'Fajar Ramadhan',       'fajar@gmail.com',        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'peserta', '2025-11-07 10:00:00'),
(11, 'Indah Permatasari',    'indah@gmail.com',        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'peserta', '2025-11-08 13:00:00'),
(12, 'Galih Prasetyo',       'galih@gmail.com',        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'peserta', '2025-11-09 14:00:00'),
(13, 'Rina Kusumawati',      'rina@gmail.com',         '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'peserta', '2025-11-10 15:00:00'),
-- Admin
(14, 'Admin EventHub',       'admin@eventhub.id',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin',   '2025-10-01 07:00:00');

-- ------------------------------------------------------------
-- INSERT: events (10 event)
-- Penyelenggara ID 1, 2, 3
-- ------------------------------------------------------------
INSERT INTO `events` (`id`, `penyelenggara_id`, `judul`, `deskripsi`, `lokasi`, `tanggal_mulai`, `tanggal_selesai`, `kuota`, `status`, `created_at`) VALUES
(1,  1, 'Tech Summit 2026',
     'Konferensi teknologi tahunan menghadirkan pembicara dari perusahaan tech terkemuka di Indonesia dan mancanegara. Membahas tren AI, Cloud, dan Web3.',
     'Jakarta Convention Center, Jakarta',
     '2026-03-25 08:00:00', '2026-03-25 17:00:00', 500, 'selesai', '2026-01-10 08:00:00'),

(2,  2, 'UI/UX Bootcamp Intensif',
     'Bootcamp intensif 1 hari mendesain produk digital dari user research hingga prototyping menggunakan Figma. Cocok untuk pemula maupun yang ingin upgrade skill.',
     'Online (Zoom)',
     '2026-04-01 09:00:00', '2026-04-01 16:00:00', 100, 'aktif', '2026-02-01 10:00:00'),

(3,  3, 'Annual Developer Conference',
     'Konferensi developer tahunan membahas tren software engineering, cloud computing, dan kecerdasan buatan. Hadiri sesi panel dan workshop eksklusif.',
     'Trans Luxury Hotel, Bandung',
     '2026-04-12 08:00:00', '2026-04-12 18:00:00', 200, 'aktif', '2026-02-10 09:00:00'),

(4,  1, 'Data Science for Beginners',
     'Webinar pengenalan data science dan machine learning untuk pemula. Pelajari Python, Pandas, dan dasar-dasar visualisasi data.',
     'Online (Google Meet)',
     '2026-04-18 13:00:00', '2026-04-18 15:00:00', 300, 'aktif', '2026-02-15 11:00:00'),

(5,  2, 'Cloud Computing Workshop',
     'Workshop hands-on mengelola infrastruktur cloud menggunakan AWS dan Google Cloud Platform. Peserta akan praktek langsung membuat EC2, S3, dan Cloud Functions.',
     'Gedung Cyber 2, Jakarta Selatan',
     '2026-04-22 09:00:00', '2026-04-22 17:00:00', 50, 'aktif', '2026-02-20 08:00:00'),

(6,  3, 'Startup Pitching Summit',
     'Event pitching startup terbesar di Indonesia Timur. Dihadiri lebih dari 30 investor dan mentor berpengalaman. Daftarkan startup kamu dan dapatkan pendanaan.',
     'Surabaya Convention Hall, Surabaya',
     '2026-04-28 10:00:00', '2026-04-28 17:00:00', 150, 'aktif', '2026-02-25 09:00:00'),

(7,  1, 'Mobile Dev Bootcamp Flutter',
     'Bootcamp intensif membangun aplikasi mobile Android dan iOS dengan Flutter dari nol hingga deploy ke Play Store dan App Store.',
     'Coworking Space YK, Yogyakarta',
     '2026-05-05 08:00:00', '2026-05-05 17:00:00', 40, 'aktif', '2026-03-01 08:00:00'),

(8,  2, 'Cybersecurity Awareness Seminar',
     'Seminar keamanan siber membahas ancaman terkini seperti phishing, ransomware, dan social engineering. Pelajari cara melindungi data pribadi dan perusahaan.',
     'Online (Zoom Webinar)',
     '2026-05-10 09:00:00', '2026-05-10 12:00:00', 200, 'aktif', '2026-03-05 10:00:00'),

(9,  3, 'AI for Business Implementation',
     'Seminar implementasi kecerdasan buatan untuk meningkatkan efisiensi bisnis di berbagai industri: retail, manufaktur, dan keuangan.',
     'Hotel Mulia, Jakarta Pusat',
     '2026-05-15 13:00:00', '2026-05-15 16:00:00', 250, 'aktif', '2026-03-10 11:00:00'),

(10, 1, 'Full Stack Web Development Bootcamp',
     'Bootcamp intensif 2 minggu belajar full stack web development: HTML, CSS, JavaScript, React, Node.js, Express, hingga deployment di cloud.',
     'Dicoding Space, Jakarta',
     '2026-06-01 08:00:00', '2026-06-14 17:00:00', 30, 'draft', '2026-03-15 09:00:00');

-- ------------------------------------------------------------
-- INSERT: pendaftaran (15 data)
-- Peserta ID 4-13 mendaftar ke berbagai event
-- ------------------------------------------------------------
INSERT INTO `pendaftaran` (`id`, `peserta_id`, `event_id`, `status_pendaftaran`, `terdaftar_at`) VALUES
(1,  4,  1, 'dikonfirmasi', '2026-01-15 08:30:00'),
(2,  5,  1, 'dikonfirmasi', '2026-01-16 09:00:00'),
(3,  6,  1, 'dikonfirmasi', '2026-01-17 10:00:00'),
(4,  7,  2, 'dikonfirmasi', '2026-02-05 11:00:00'),
(5,  8,  2, 'menunggu',     '2026-02-06 09:30:00'),
(6,  4,  3, 'dikonfirmasi', '2026-02-12 08:00:00'),
(7,  9,  3, 'dikonfirmasi', '2026-02-13 10:15:00'),
(8,  10, 4, 'dikonfirmasi', '2026-02-20 14:00:00'),
(9,  11, 4, 'dikonfirmasi', '2026-02-21 08:45:00'),
(10, 5,  5, 'dikonfirmasi', '2026-02-25 09:00:00'),
(11, 12, 5, 'ditolak',      '2026-02-26 11:00:00'),
(12, 6,  6, 'dikonfirmasi', '2026-03-01 13:00:00'),
(13, 13, 7, 'dikonfirmasi', '2026-03-05 08:00:00'),
(14, 7,  8, 'menunggu',     '2026-03-10 09:30:00'),
(15, 8,  9, 'dibatalkan',   '2026-03-12 10:00:00');

-- ------------------------------------------------------------
-- INSERT: tiket (10 data)
-- Hanya pendaftaran berstatus 'dikonfirmasi' yang dapat tiket
-- ------------------------------------------------------------
INSERT INTO `tiket` (`id`, `pendaftaran_id`, `kode_qr`, `status_tiket`, `diterbitkan_at`) VALUES
(1,  1,  'EVH-QR-A1B2C3D4E5F6', 'digunakan',  '2026-01-15 08:35:00'),
(2,  2,  'EVH-QR-B2C3D4E5F6G7', 'digunakan',  '2026-01-16 09:05:00'),
(3,  3,  'EVH-QR-C3D4E5F6G7H8', 'digunakan',  '2026-01-17 10:05:00'),
(4,  4,  'EVH-QR-D4E5F6G7H8I9', 'aktif',      '2026-02-05 11:05:00'),
(5,  6,  'EVH-QR-E5F6G7H8I9J0', 'aktif',      '2026-02-12 08:05:00'),
(6,  7,  'EVH-QR-F6G7H8I9J0K1', 'aktif',      '2026-02-13 10:20:00'),
(7,  8,  'EVH-QR-G7H8I9J0K1L2', 'aktif',      '2026-02-20 14:05:00'),
(8,  9,  'EVH-QR-H8I9J0K1L2M3', 'aktif',      '2026-02-21 08:50:00'),
(9,  10, 'EVH-QR-I9J0K1L2M3N4', 'aktif',      '2026-02-25 09:05:00'),
(10, 12, 'EVH-QR-J0K1L2M3N4O5', 'aktif',      '2026-03-01 13:05:00');

-- ------------------------------------------------------------
-- INSERT: check_in (10 data)
-- Event 1 (Tech Summit) sudah selesai, pesertanya sudah check-in
-- ------------------------------------------------------------
INSERT INTO `check_in` (`id`, `tiket_id`, `penyelenggara_id`, `waktu_checkin`) VALUES
(1,  1,  1, '2026-03-25 07:55:00'),
(2,  2,  1, '2026-03-25 08:03:00'),
(3,  3,  1, '2026-03-25 08:10:00'),
(4,  4,  2, '2026-04-01 08:58:00'),
(5,  5,  3, '2026-04-12 07:50:00'),
(6,  6,  3, '2026-04-12 07:58:00'),
(7,  7,  1, '2026-04-18 12:55:00'),
(8,  8,  1, '2026-04-18 13:02:00'),
(9,  9,  2, '2026-04-22 08:57:00'),
(10, 10, 3, '2026-04-28 09:52:00');

-- ------------------------------------------------------------
-- INSERT: laporan (10 data — 1 per event)
-- ------------------------------------------------------------
INSERT INTO `laporan` (`id`, `event_id`, `total_pendaftar`, `total_hadir`, `dibuat_at`) VALUES
(1,  1,  450, 388, '2026-03-25 20:00:00'),
(2,  2,  87,  74,  '2026-04-01 18:00:00'),
(3,  3,  185, 162, '2026-04-12 20:00:00'),
(4,  4,  270, 231, '2026-04-18 16:00:00'),
(5,  5,  48,  43,  '2026-04-22 18:00:00'),
(6,  6,  140, 118, '2026-04-28 19:00:00'),
(7,  7,  38,  35,  '2026-05-05 18:00:00'),
(8,  8,  195, 167, '2026-05-10 13:00:00'),
(9,  9,  220, 195, '2026-05-15 17:00:00'),
(10, 10, 15,  0,   '2026-06-14 18:00:00');


-- ============================================================
-- QUERY VERIFIKASI (uncomment untuk cek)
-- ============================================================

-- Cek semua user:
-- SELECT id, nama, email, role FROM users;

-- Cek semua event beserta penyelenggara:
-- SELECT e.id, e.judul, u.nama AS penyelenggara, e.status, e.kuota
-- FROM events e JOIN users u ON e.penyelenggara_id = u.id;

-- Cek pendaftaran + nama peserta + nama event:
-- SELECT p.id, u.nama AS peserta, e.judul AS event, p.status_pendaftaran, p.terdaftar_at
-- FROM pendaftaran p
-- JOIN users u ON p.peserta_id = u.id
-- JOIN events e ON p.event_id = e.id;

-- Cek tiket + status + nama event:
-- SELECT t.id, t.kode_qr, t.status_tiket, e.judul, u.nama AS peserta
-- FROM tiket t
-- JOIN pendaftaran p ON t.pendaftaran_id = p.id
-- JOIN events e ON p.event_id = e.id
-- JOIN users u ON p.peserta_id = u.id;

-- Cek laporan per event:
-- SELECT l.id, e.judul, l.total_pendaftar, l.total_hadir,
--        ROUND(l.total_hadir / l.total_pendaftar * 100, 1) AS persen_hadir
-- FROM laporan l JOIN events e ON l.event_id = e.id;