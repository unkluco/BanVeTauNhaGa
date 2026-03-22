package com;

import com.connectDB.ConnectDB;
import com.dao.*;
import com.entity.*;
import com.enums.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Class Main dung de test tat ca cac entity va DAO
 * - Test tao doi tuong entity
 * - Test cac phuong thuc DAO (getAll, findById, insert, update, ...)
 */
public class Main {

    public static void main(String[] args) {
        try {
            // ==================== KET NOI DATABASE ====================
            System.out.println("========================================");
            System.out.println("  BAN VE TAU NHA GA - TEST SYSTEM");
            System.out.println("========================================\n");

            ConnectDB.getInstance().connect();
            System.out.println();

            // ==================== TEST ENTITY (tao doi tuong khong can DB) ====================
            testEntityCreation();

            // ==================== TEST DAO (can ket noi DB) ====================
            testDAO_NhanVien();
            testDAO_Ga();
            testDAO_DauMay();
            testDAO_ToaTau();
            testDAO_Gia();
            testDAO_KhuyenMai();
            testDAO_Tuyen();
            testDAO_DoanTau();
            testDAO_ChiTietDoanTau();
            testDAO_Ghe();
            testDAO_Lich();
            testDAO_ChiTietGia();
            testDAO_HoaDon();
            testDAO_Ve();
            testDAO_ChiTietVe();
            testDAO_ApDungKM();
            testDAO_GiuCho();

            System.out.println("\n========================================");
            System.out.println("  TAT CA TESTS HOAN THANH!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("LOI: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectDB.getInstance().disconnect();
        }
    }

    // ==================== TEST TAO DOI TUONG ENTITY ====================
    private static void testEntityCreation() {
        System.out.println("--- TEST TAO DOI TUONG ENTITY ---");

        // NhanVien
        NhanVien nv = new NhanVien("NV-TEST", "Nguyen Van Test", "123456", VaiTro.BAN_VE, "0999999999");
        System.out.println("NhanVien: " + nv);

        // Ga
        Ga ga = new Ga("GA-TEST", "Ga Test", "123 Duong Test");
        System.out.println("Ga: " + ga);

        // DauMay
        DauMay dm = new DauMay("DM-TEST", "Dau may test");
        System.out.println("DauMay: " + dm);

        // ToaTau
        ToaTau tt = new ToaTau("TOA-TEST", LoaiGhe.GHE_MEM);
        System.out.println("ToaTau: " + tt);

        // Tuyen
        Ga gaDi = new Ga("GA-001", "Ga Ha Noi", "Ha Noi");
        Ga gaDen = new Ga("GA-005", "Ga Sai Gon", "TP.HCM");
        Tuyen tuyen = new Tuyen("TUY-TEST", gaDi, gaDen);
        System.out.println("Tuyen: " + tuyen);

        // DoanTau
        DoanTau doanTau = new DoanTau("DT-TEST", "SE-Test", dm);
        System.out.println("DoanTau: " + doanTau);

        // ChiTietDoanTau
        ChiTietDoanTau ctdt = new ChiTietDoanTau("CTDT-TEST", doanTau, tt, 1);
        System.out.println("ChiTietDoanTau: " + ctdt);

        // Ghe
        Ghe ghe = new Ghe("G-TEST-01", tt, 1);
        System.out.println("Ghe: " + ghe);

        // Lich
        Lich lich = new Lich("LCH-TEST", tuyen, doanTau, LocalDateTime.of(2026, 4, 10, 6, 0), "5 gio 30 phut");
        System.out.println("Lich: " + lich);

        // Gia
        Gia gia = new Gia("GIA-TEST",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59, 59),
                "Bang gia test", false);
        System.out.println("Gia: " + gia);

        // ChiTietGia
        ChiTietGia ctg = new ChiTietGia("CTG-TEST", gia, tuyen, LoaiGhe.GHE_MEM, 250000);
        System.out.println("ChiTietGia: " + ctg);

        // KhuyenMai
        KhuyenMai km = new KhuyenMai("KM-TEST", "Giam gia test", 0.10, "Dieu kien test",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59, 59));
        System.out.println("KhuyenMai: " + km);

        // HoaDon
        HoaDon hd = new HoaDon("HD-TEST", nv, LocalDateTime.now());
        System.out.println("HoaDon: " + hd);

        // Ve
        Ve ve = new Ve("VE-TEST", hd, "Nguyen Van A", "012345678901", 250000,
                TrangThaiVe.DA_BAN, null, null);
        System.out.println("Ve: " + ve);

        // ChiTietVe
        ChiTietVe ctv = new ChiTietVe("CTV-TEST", ve, lich, ghe, TrangThaiVe.DA_BAN);
        System.out.println("ChiTietVe: " + ctv);

        // ApDungKM
        ApDungKM adkm = new ApDungKM("ADKM-TEST", ve, km);
        System.out.println("ApDungKM: " + adkm);

        // GiuCho
        GiuCho gc = new GiuCho("GC-TEST", nv, lich, ghe, LocalDateTime.now().plusMinutes(5));
        System.out.println("GiuCho: " + gc);
        System.out.println("  -> con hieu luc: " + gc.conHieuLuc());

        System.out.println("=> Tao doi tuong entity: THANH CONG\n");
    }

    // ==================== TEST DAO NHAN VIEN ====================
    private static void testDAO_NhanVien() {
        System.out.println("--- TEST DAO_NhanVien ---");
        DAO_NhanVien dao = new DAO_NhanVien();

        // getAll
        List<NhanVien> dsNV = dao.getAll();
        System.out.println("  getAll: " + dsNV.size() + " nhan vien");
        for (NhanVien nv : dsNV) {
            System.out.println("    " + nv);
        }

        // findById
        NhanVien nv = dao.findById("NV-0001");
        System.out.println("  findById(NV-0001): " + nv);

        // checkLogin
        NhanVien login = dao.checkLogin("NV-0001", "Pass@123");
        System.out.println("  checkLogin(NV-0001, Pass@123): " + (login != null ? "THANH CONG" : "THAT BAI"));

        // search
        List<NhanVien> timKiem = dao.search("Nguyen");
        System.out.println("  search('Nguyen'): " + timKiem.size() + " ket qua");

        System.out.println();
    }

    // ==================== TEST DAO GA ====================
    private static void testDAO_Ga() {
        System.out.println("--- TEST DAO_Ga ---");
        DAO_Ga dao = new DAO_Ga();

        List<Ga> dsGa = dao.getAll();
        System.out.println("  getAll: " + dsGa.size() + " ga");
        for (Ga ga : dsGa) {
            System.out.println("    " + ga);
        }

        Ga ga = dao.findById("GA-001");
        System.out.println("  findById(GA-001): " + ga);

        System.out.println();
    }

    // ==================== TEST DAO DAU MAY ====================
    private static void testDAO_DauMay() {
        System.out.println("--- TEST DAO_DauMay ---");
        DAO_DauMay dao = new DAO_DauMay();

        List<DauMay> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " dau may");
        for (DauMay dm : ds) {
            System.out.println("    " + dm);
        }

        DauMay dm = dao.findById("DM-001");
        System.out.println("  findById(DM-001): " + dm);

        System.out.println();
    }

    // ==================== TEST DAO TOA TAU ====================
    private static void testDAO_ToaTau() {
        System.out.println("--- TEST DAO_ToaTau ---");
        DAO_ToaTau dao = new DAO_ToaTau();

        List<ToaTau> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " toa tau");
        for (ToaTau tt : ds) {
            System.out.println("    " + tt);
        }

        ToaTau tt = dao.findById("TOA-001");
        System.out.println("  findById(TOA-001): " + tt);

        System.out.println();
    }

    // ==================== TEST DAO GIA ====================
    private static void testDAO_Gia() {
        System.out.println("--- TEST DAO_Gia ---");
        DAO_Gia dao = new DAO_Gia();

        List<Gia> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " ky gia");
        for (Gia g : ds) {
            System.out.println("    " + g);
        }

        Gia g = dao.findById("GIA-001");
        System.out.println("  findById(GIA-001): " + g);

        Gia hienHanh = dao.getGiaHienHanh();
        System.out.println("  getGiaHienHanh: " + hienHanh);

        System.out.println();
    }

    // ==================== TEST DAO KHUYEN MAI ====================
    private static void testDAO_KhuyenMai() {
        System.out.println("--- TEST DAO_KhuyenMai ---");
        DAO_KhuyenMai dao = new DAO_KhuyenMai();

        List<KhuyenMai> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " khuyen mai");
        for (KhuyenMai km : ds) {
            System.out.println("    " + km);
        }

        KhuyenMai km = dao.findById("KM-001");
        System.out.println("  findById(KM-001): " + km);

        List<KhuyenMai> hienHanh = dao.getKhuyenMaiHienHanh();
        System.out.println("  getKhuyenMaiHienHanh: " + hienHanh.size() + " khuyen mai");

        System.out.println();
    }

    // ==================== TEST DAO TUYEN ====================
    private static void testDAO_Tuyen() {
        System.out.println("--- TEST DAO_Tuyen ---");
        DAO_Tuyen dao = new DAO_Tuyen();

        List<Tuyen> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " tuyen");
        for (Tuyen t : ds) {
            System.out.println("    " + t);
        }

        Tuyen t = dao.findById("TUY-001");
        System.out.println("  findById(TUY-001): " + t);

        List<Tuyen> byGa = dao.findByGaDiGaDen("GA-001", "GA-002");
        System.out.println("  findByGaDiGaDen(GA-001, GA-002): " + byGa.size() + " tuyen");

        System.out.println();
    }

    // ==================== TEST DAO DOAN TAU ====================
    private static void testDAO_DoanTau() {
        System.out.println("--- TEST DAO_DoanTau ---");
        DAO_DoanTau dao = new DAO_DoanTau();

        List<DoanTau> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " doan tau");
        for (DoanTau dt : ds) {
            System.out.println("    " + dt);
        }

        DoanTau dt = dao.findById("DT-001");
        System.out.println("  findById(DT-001): " + dt);

        System.out.println();
    }

    // ==================== TEST DAO CHI TIET DOAN TAU ====================
    private static void testDAO_ChiTietDoanTau() {
        System.out.println("--- TEST DAO_ChiTietDoanTau ---");
        DAO_ChiTietDoanTau dao = new DAO_ChiTietDoanTau();

        List<ChiTietDoanTau> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " chi tiet");

        List<ChiTietDoanTau> byDT = dao.findByDoanTau("DT-001");
        System.out.println("  findByDoanTau(DT-001): " + byDT.size() + " toa");
        for (ChiTietDoanTau ct : byDT) {
            System.out.println("    Toa " + ct.getSoThuTu() + ": " +
                    ct.getToaTau().getMaToaTau() + " (" + ct.getToaTau().getLoaiGhe() + ")");
        }

        System.out.println();
    }

    // ==================== TEST DAO GHE ====================
    private static void testDAO_Ghe() {
        System.out.println("--- TEST DAO_Ghe ---");
        DAO_Ghe dao = new DAO_Ghe();

        List<Ghe> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " ghe");

        Ghe ghe = dao.findById("G-001-01");
        System.out.println("  findById(G-001-01): " + ghe);

        List<Ghe> byToa = dao.findByToaTau("TOA-001");
        System.out.println("  findByToaTau(TOA-001): " + byToa.size() + " ghe");

        // Test kiem tra ghe trong
        boolean isTrong = dao.isGheTrong("G-001-01", "LCH-001");
        System.out.println("  isGheTrong(G-001-01, LCH-001): " + isTrong);

        System.out.println();
    }

    // ==================== TEST DAO LICH ====================
    private static void testDAO_Lich() {
        System.out.println("--- TEST DAO_Lich ---");
        DAO_Lich dao = new DAO_Lich();

        List<Lich> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " lich");
        for (Lich l : ds) {
            System.out.println("    " + l.getMaLich() + " | Tuyen: " +
                    l.getTuyen().getMaTuyen() + " | " + l.getThoiGianBatDau() + " | " + l.getThoiGianChay());
        }

        Lich l = dao.findById("LCH-001");
        System.out.println("  findById(LCH-001): " + l);

        System.out.println();
    }

    // ==================== TEST DAO CHI TIET GIA ====================
    private static void testDAO_ChiTietGia() {
        System.out.println("--- TEST DAO_ChiTietGia ---");
        DAO_ChiTietGia dao = new DAO_ChiTietGia();

        List<ChiTietGia> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " chi tiet gia");

        ChiTietGia ctg = dao.findByGiaTuyenLoaiGhe("GIA-001", "TUY-001", "GHE_MEM");
        System.out.println("  findByGiaTuyenLoaiGhe(GIA-001, TUY-001, GHE_MEM): " +
                (ctg != null ? ctg.getGiaNiemYet() + " VND" : "null"));

        List<ChiTietGia> byGia = dao.findByGia("GIA-001");
        System.out.println("  findByGia(GIA-001): " + byGia.size() + " chi tiet");

        System.out.println();
    }

    // ==================== TEST DAO HOA DON ====================
    private static void testDAO_HoaDon() {
        System.out.println("--- TEST DAO_HoaDon ---");
        DAO_HoaDon dao = new DAO_HoaDon();

        List<HoaDon> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " hoa don");
        for (HoaDon hd : ds) {
            System.out.println("    " + hd);
        }

        HoaDon hd = dao.findById("HD-10042026-001");
        System.out.println("  findById(HD-10042026-001): " + hd);

        String maHDMoi = dao.phatSinhMaHoaDon();
        System.out.println("  phatSinhMaHoaDon: " + maHDMoi);

        System.out.println();
    }

    // ==================== TEST DAO VE ====================
    private static void testDAO_Ve() {
        System.out.println("--- TEST DAO_Ve ---");
        DAO_Ve dao = new DAO_Ve();

        List<Ve> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " ve");
        for (Ve ve : ds) {
            System.out.println("    " + ve);
        }

        Ve ve = dao.findById("VE-001");
        System.out.println("  findById(VE-001): " + ve);

        List<Ve> byHD = dao.findByHoaDon("HD-10042026-001");
        System.out.println("  findByHoaDon(HD-10042026-001): " + byHD.size() + " ve");

        List<Ve> byCCCD = dao.findByCCCD("012345678901");
        System.out.println("  findByCCCD(012345678901): " + byCCCD.size() + " ve");

        System.out.println();
    }

    // ==================== TEST DAO CHI TIET VE ====================
    private static void testDAO_ChiTietVe() {
        System.out.println("--- TEST DAO_ChiTietVe ---");
        DAO_ChiTietVe dao = new DAO_ChiTietVe();

        List<ChiTietVe> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " chi tiet ve");

        List<ChiTietVe> byVe = dao.findByVe("VE-001");
        System.out.println("  findByVe(VE-001): " + byVe.size() + " chang");

        List<ChiTietVe> byLich = dao.findByLich("LCH-001");
        System.out.println("  findByLich(LCH-001): " + byLich.size() + " ghe da ban");

        System.out.println();
    }

    // ==================== TEST DAO AP DUNG KM ====================
    private static void testDAO_ApDungKM() {
        System.out.println("--- TEST DAO_ApDungKM ---");
        DAO_ApDungKM dao = new DAO_ApDungKM();

        List<ApDungKM> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " ap dung KM");

        List<ApDungKM> byVe = dao.findByVe("VE-004");
        System.out.println("  findByVe(VE-004): " + byVe.size() + " khuyen mai");
        for (ApDungKM adkm : byVe) {
            System.out.println("    " + adkm);
        }

        System.out.println();
    }

    // ==================== TEST DAO GIU CHO ====================
    private static void testDAO_GiuCho() {
        System.out.println("--- TEST DAO_GiuCho ---");
        DAO_GiuCho dao = new DAO_GiuCho();

        List<GiuCho> ds = dao.getAll();
        System.out.println("  getAll: " + ds.size() + " giu cho");
        for (GiuCho gc : ds) {
            System.out.println("    " + gc + " | con hieu luc: " + gc.conHieuLuc());
        }

        List<GiuCho> conHieuLuc = dao.findConHieuLucByLich("LCH-008");
        System.out.println("  findConHieuLucByLich(LCH-008): " + conHieuLuc.size() + " giu cho");

        // Xoa giu cho het han
        int deleted = dao.deleteExpired();
        System.out.println("  deleteExpired: " + deleted + " ban ghi da xoa");

        System.out.println();
    }
}
