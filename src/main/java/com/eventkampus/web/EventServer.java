package com.eventkampus.web;

import com.eventkampus.Event;
import com.eventkampus.service.EventService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Penghubung halaman admin (HTML) ke EventService.
 * Memakai HttpServer bawaan Java, jadi pom.xml tidak perlu diubah.
 * Kalau kelompok nanti pindah ke Servlet/Spring Boot, cukup ganti file ini;
 * EventService dan halaman HTML tetap sama.
 *
 * Alamat API:
 *   GET  /api/events                      daftar event
 *   POST /api/events                      tambah event
 *   PUT  /api/events/{id}                 ubah event
 *   POST /api/events/{id}/aktifkan        aktifkan event
 *   POST /api/events/{id}/nonaktifkan     nonaktifkan event
 * Halaman admin dibuka di http://localhost:8080/
 */
public class EventServer {

    private static final int PORT = 8080;
    private static final EventService service = new EventService();

    public static void main(String[] args) throws IOException {
           System.out.println("Menyambung ke database...");
   com.eventkampus.util.HibernateUtil.getSessionFactory();
   System.out.println("Database tersambung.");
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/api/events", EventServer::handleApi);
        server.createContext("/", EventServer::handlePage);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Server jalan. Buka http://localhost:" + PORT + " di browser.");
        System.out.println("Tekan Stop (kotak merah) atau Ctrl+C untuk menghentikan.");
    }

    // ---------- halaman ----------
    private static void handlePage(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (!path.equals("/") && !path.equals("/index.html")) {
            kirim(ex, 404, "text/plain; charset=utf-8", "Halaman tidak ditemukan.");
            return;
        }
        try (InputStream in = EventServer.class.getResourceAsStream("/web/kelola-event.html")) {
            if (in == null) {
                kirim(ex, 500, "text/plain; charset=utf-8",
                        "File web/kelola-event.html tidak ada di src/main/resources.");
                return;
            }
            kirim(ex, 200, "text/html; charset=utf-8", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    // ---------- API ----------
    private static void handleApi(HttpExchange ex) throws IOException {
        try {
            String[] p = ex.getRequestURI().getPath().split("/"); // "", "api", "events", id, aksi
            String method = ex.getRequestMethod();

            if (p.length == 3 && method.equals("GET")) {
                kirimJson(ex, 200, daftarJson(service.daftarEvent()));
                return;
            }
            if (p.length == 3 && method.equals("POST")) {
                Event baru = service.tambahEvent(dariJson(bacaBody(ex)));
                kirimJson(ex, 201, toJson(baru));
                return;
            }
            if (p.length == 4 && method.equals("PUT")) {
                Event hasil = service.ubahEvent(Long.valueOf(p[3]), dariJson(bacaBody(ex)));
                kirimJson(ex, 200, toJson(hasil));
                return;
            }
            if (p.length == 5 && method.equals("POST")
                    && (p[4].equals("aktifkan") || p[4].equals("nonaktifkan"))) {
                Long id = Long.valueOf(p[3]);
                if (p[4].equals("aktifkan")) {
                    service.aktifkanEvent(id);
                } else {
                    service.nonaktifkanEvent(id);
                }
                kirimJson(ex, 200, toJson(service.detailEvent(id)));
                return;
            }
            kirimJson(ex, 404, error("Alamat API tidak ditemukan."));
        } catch (IllegalArgumentException e) {
            // pesan validasi dari EventService (bahasa Indonesia)
            kirimJson(ex, 400, error(e.getMessage()));
        } catch (Throwable e) {
            e.printStackTrace();
            kirimJson(ex, 500, error("Terjadi kesalahan di server. Lihat log di terminal."));
        }
    }

    // ---------- JSON -> Event ----------
    private static Event dariJson(String json) {
        Map<String, String> m = parseJson(json);
        Event e = new Event();
        e.setNamaEvent(m.get("nama"));
        e.setKategori(m.get("kategori"));
        e.setDeskripsi(m.get("deskripsi"));
        e.setLokasi(m.get("lokasi"));
        e.setStatus(m.get("status"));
        try {
            e.setTanggalMulai(tanggal(m.get("mulai")));
            e.setTanggalSelesai(tanggal(m.get("selesai")));
            e.setBiaya(kosong(m.get("biaya")) ? null : new BigDecimal(m.get("biaya")));
            e.setKuota(kosong(m.get("kuota")) ? null : Integer.valueOf(m.get("kuota")));
        } catch (DateTimeParseException | NumberFormatException ex) {
            throw new IllegalArgumentException("Format tanggal, biaya, atau kuota tidak valid.");
        }
        return e;
    }

    private static LocalDateTime tanggal(String s) {
        return kosong(s) ? null : LocalDateTime.parse(s);
    }

    private static boolean kosong(String s) {
        return s == null || s.isBlank();
    }

    /** Pembaca JSON sederhana untuk satu objek datar: {"kunci": "teks" atau angka}. */
    private static Map<String, String> parseJson(String s) {
        Map<String, String> m = new HashMap<>();
        int i = s.indexOf('{');
        if (i < 0) {
            throw new IllegalArgumentException("Data yang dikirim tidak valid.");
        }
        i++;
        while (true) {
            i = lewatiSpasi(s, i);
            if (i >= s.length() || s.charAt(i) == '}') {
                break;
            }
            if (s.charAt(i) == ',') {
                i++;
                continue;
            }
            int[] pos = {i};
            String key = bacaString(s, pos);
            i = lewatiSpasi(s, pos[0]);
            if (i >= s.length() || s.charAt(i) != ':') {
                throw new IllegalArgumentException("Data yang dikirim tidak valid.");
            }
            i = lewatiSpasi(s, i + 1);
            if (i >= s.length()) {
                throw new IllegalArgumentException("Data yang dikirim tidak valid.");
            }
            String val;
            if (s.charAt(i) == '"') {
                pos[0] = i;
                val = bacaString(s, pos);
                i = pos[0];
            } else {
                int j = i;
                while (j < s.length() && ",}".indexOf(s.charAt(j)) < 0) {
                    j++;
                }
                val = s.substring(i, j).trim();
                if (val.equals("null")) {
                    val = null;
                }
                i = j;
            }
            m.put(key, val);
        }
        return m;
    }

    private static int lewatiSpasi(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static String bacaString(String s, int[] pos) {
        int i = pos[0];
        if (i >= s.length() || s.charAt(i) != '"') {
            throw new IllegalArgumentException("Data yang dikirim tidak valid.");
        }
        StringBuilder sb = new StringBuilder();
        for (i++; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                pos[0] = i + 1;
                return sb.toString();
            }
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'u' -> {
                        if (i + 4 >= s.length()) {
                            throw new IllegalArgumentException("Data yang dikirim tidak valid.");
                        }
                        sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    default -> sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        throw new IllegalArgumentException("Data yang dikirim tidak valid.");
    }

    // ---------- Event -> JSON ----------
    private static String daftarJson(List<Event> daftar) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < daftar.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(toJson(daftar.get(i)));
        }
        return sb.append(']').toString();
    }

    private static String toJson(Event e) {
        BigDecimal biaya = e.getBiaya() == null ? BigDecimal.ZERO : e.getBiaya();
        return "{\"id\":" + e.getId()
                + ",\"nama\":" + q(e.getNamaEvent())
                + ",\"kategori\":" + q(e.getKategori())
                + ",\"deskripsi\":" + q(e.getDeskripsi())
                + ",\"mulai\":" + q(tgl(e.getTanggalMulai()))
                + ",\"selesai\":" + q(tgl(e.getTanggalSelesai()))
                + ",\"lokasi\":" + q(e.getLokasi())
                + ",\"biaya\":" + biaya.stripTrailingZeros().toPlainString()
                + ",\"kuota\":" + (e.getKuota() == null ? 0 : e.getKuota())
                // halaman HTML memakai "Aktif"/"Nonaktif", apa pun huruf di database
                + ",\"status\":" + q(EventService.isAktif(e) ? "Aktif" : "Nonaktif")
                + "}";
    }

    private static String tgl(LocalDateTime t) {
        return t == null ? "" : t.truncatedTo(ChronoUnit.MINUTES).toString();
    }

    private static String error(String pesan) {
        return "{\"error\":" + q(pesan) + "}";
    }

    private static String q(String s) {
        if (s == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    // ---------- helper HTTP ----------
    private static String bacaBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void kirimJson(HttpExchange ex, int kode, String json) throws IOException {
        kirim(ex, kode, "application/json; charset=utf-8", json);
    }

    private static void kirim(HttpExchange ex, int kode, String tipe, String isi) throws IOException {
        byte[] data = isi.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", tipe);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(kode, data.length);
        ex.getResponseBody().write(data);
        ex.close();
    }
}
