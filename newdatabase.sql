    CREATE DATABASE IF NOT EXISTS eventhub_baru;
    USE eventhub_baru;

    DROP TABLE IF EXISTS notifikasi_read;
    DROP TABLE IF EXISTS notifikasi;
    DROP TABLE IF EXISTS laporan;
    DROP TABLE IF EXISTS sertifikat;
    DROP TABLE IF EXISTS check_in;
    DROP TABLE IF EXISTS tiket;
    DROP TABLE IF EXISTS pendaftaran;
    DROP TABLE IF EXISTS events;
    DROP TABLE IF EXISTS users;

    -- 1. Tabel Users
    CREATE TABLE `users` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `email` varchar(255) NOT NULL,
    `institusi` varchar(255) DEFAULT NULL,
    `kota` varchar(255) DEFAULT NULL,
    `nama` varchar(255) NOT NULL,
    `password` varchar(255) NOT NULL,
    `role` varchar(255) NOT NULL,
    `telp` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_email` (`email`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

    -- 2. Tabel Events
    CREATE TABLE `events` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `created_by` bigint(20) NOT NULL,
    `deskripsi` varchar(1000) DEFAULT NULL,
    `harga` varchar(255) NOT NULL,
    `tipe_harga` varchar(255) NOT NULL,
    `kapasitas` int(11) NOT NULL,
    `kategori` varchar(255) NOT NULL,
    `lokasi` varchar(255) NOT NULL,
    `venue` varchar(255) NOT NULL,
    `nama` varchar(255) NOT NULL,
    `penyelenggara` varchar(255) NOT NULL,
    `tanggal` varchar(255) NOT NULL,
    `waktu` varchar(255) NOT NULL,
    `terisi` int(11) NOT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

    -- 3. Tabel Pendaftaran
    CREATE TABLE `pendaftaran` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `event_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `kode_tiket` varchar(255) NOT NULL,
    `status` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_kode_tiket` (`kode_tiket`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

    -- 4. Tabel Tiket
    CREATE TABLE `tiket` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `diterbitkan_at` datetime(6) DEFAULT NULL,
    `kode_qr` varchar(255) NOT NULL,
    `pendaftaran_id` bigint(20) NOT NULL,
    `status_tiket` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_kode_qr` (`kode_qr`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

    -- 5. Tabel Check-in
    CREATE TABLE `check_in` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `penyelenggara_id` bigint(20) NOT NULL,
    `tiket_id` bigint(20) NOT NULL,
    `waktu_checkin` datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

    -- 6. Tabel Sertifikat
    CREATE TABLE `sertifikat` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `diterbitkan_at` datetime(6) DEFAULT NULL,
    `event_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `kode_sertifikat` varchar(255) NOT NULL,
    `nama_event` varchar(255) NOT NULL,
    `nama_peserta` varchar(255) NOT NULL,
    `penyelenggara` varchar(255) NOT NULL,
    `tanggal_event` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_kode_sertifikat` (`kode_sertifikat`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

    -- 7. Tabel Laporan
    CREATE TABLE `laporan` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `dibuat_at` datetime(6) DEFAULT NULL,
    `event_id` bigint(20) NOT NULL,
    `total_hadir` int(11) NOT NULL,
    `total_pendaftar` int(11) NOT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

    -- 8. Tabel Notifikasi
    CREATE TABLE `notifikasi` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `dibuat_at` datetime(6) DEFAULT NULL,
    `event_id` bigint(20) NOT NULL,
    `penyelenggara_id` bigint(20) NOT NULL,
    `judul` varchar(255) NOT NULL,
    `pesan` varchar(1000) NOT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

    -- 9. Tabel Notifikasi Read
    CREATE TABLE `notifikasi_read` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `dihapus` bit(1) DEFAULT NULL,
    `sudah_dibaca` bit(1) DEFAULT NULL,
    `notifikasi_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_user_notif` (`user_id`,`notifikasi_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;