package vn.edu.group12.qldt.nhom;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static vn.edu.group12.qldt.nhom.NhomTestSupport.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.flywaydb.core.Flyway;

import vn.edu.group12.qldt.domain.nhom.*;
import vn.edu.group12.qldt.domain.nhom.NhomBaoCaoModels.*;
import vn.edu.group12.qldt.dto.nhom.TaoNhomForm;
import vn.edu.group12.qldt.repository.nhom.NhomBaoCaoRepository;
import vn.edu.group12.qldt.service.nhom.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@org.junit.jupiter.api.Tag("mysql")
@SpringBootTest(
        properties = {
            "spring.flyway.enabled=true",
            "spring.jpa.hibernate.ddl-auto=none",
            "spring.jpa.show-sql=false"
        })
@AutoConfigureMockMvc
@ActiveProfiles("staging")
@Import(Config.class)
@EnabledIfEnvironmentVariable(
        named = "NHOM_TEST_DB_URL",
        matches = "jdbc:mysql://(127\\.0\\.0\\.1|localhost):[0-9]+/nhom_test_.*")
class NhomMySqlTests extends NhomMySqlTestBase {
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;
    @Autowired NhomService nhom;
    @Autowired QuyenNhomService quyen;
    @Autowired DangKyDeTaiService dangKy;
    @Autowired BaoCaoService baoCao;
    @Autowired MockMvc mvc;
    @Autowired FakeStorage storage;
    @MockitoSpyBean NhomBaoCaoRepository repo;
    Fixture f;

    @BeforeEach
    void setup() {
        assertThat(jdbc.queryForObject("SELECT VERSION()", String.class)).startsWith("8.4.");
        // Schema phải khớp các migration hiện có, kể cả khi phân hệ khác thêm phiên bản mới.
        flyway.validate();
        assertThat(flyway.info().applied()).isNotEmpty();
        assertThat(flyway.info().pending()).isEmpty();
        f = new Fixture(jdbc);
        storage.reset();
    }

    TaoNhomForm form(String name) {
        var form = new TaoNhomForm();
        form.setTenNhom(name);
        form.setMaDot(f.dot);
        return form;
    }

    String tao(int leader) {
        return as(f.sv(leader), "SINH_VIEN", () -> nhom.tao(form("Nhóm " + f.sv(leader))));
    }

    void yeuCau(String ma, int sv) {
        as(f.sv(sv), "SINH_VIEN", () -> nhom.yeuCau(ma));
    }

    void them(String ma, int leader, int sv) {
        yeuCau(ma, sv);
        as(f.sv(leader), "SINH_VIEN", () -> nhom.xuLyYeuCau(ma, f.sv(sv), true));
    }

    String chot() {
        String ma = tao(1);
        them(ma, 1, 2);
        them(ma, 1, 3);
        as(f.sv(1), "SINH_VIEN", () -> nhom.chot(ma));
        return ma;
    }

    String daDuyet() {
        String ma = chot();
        as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(f.topic));
        as(f.dean, "TRUONG_KHOA", () -> dangKy.duyet(ma));
        return ma;
    }

    BaoCao nop(byte[] bytes) {
        return as(f.sv(1), "SINH_VIEN", () -> baoCao.nop(file(bytes)));
    }

    void loi(NghiepVuNhomException.Loai loai, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(
                        NghiepVuNhomException.class, e -> assertThat(e.getLoai()).isEqualTo(loai));
    }

    @Test
    void taiKhoanDangNhapBangEmailNhungNghiepVuDungMaNguoiDung() {
        String ma = f.sv(1);
        assertThat(testDetails(ma, "SINH_VIEN").getUsername())
                .isEqualTo(ma + "@example.invalid");
        assertThat(as(ma, "SINH_VIEN", () -> quyen.hienTai().maNguoiDung()))
                .isEqualTo(ma);
    }

    @Test
    void nhom_01_taoNhomVaTruongCungCommit() {
        String ma = tao(1);
        assertThat(repo.nhom(ma))
                .get()
                .extracting(Nhom::soThanhVien, Nhom::soNhomTruong, Nhom::maNhomTruong)
                .containsExactly(1L, 1L, f.sv(1));
        assertThat(
                        f.count(
                                "SELECT COUNT(*) FROM nhom_sinh_vien WHERE ma_nhom=? AND"
                                        + " ma_nguoi_tao=?",
                                ma,
                                f.sv(1)))
                .isEqualTo(1);
    }

    @Test
    void rollbackNhomKhiThemTruongThatBai() {
        doThrow(
                        new DataIntegrityViolationException(
                                "Lỗi được chủ động đưa vào để kiểm tra rollback"))
                .when(repo)
                .themThanhVien(anyString(), eq(f.sv(1)), eq(true), any(LocalDateTime.class));
        loi(NghiepVuNhomException.Loai.XUNG_DOT, () -> tao(1));
        assertThat(f.count("SELECT COUNT(*) FROM nhom_sinh_vien WHERE ma_nguoi_tao=?", f.sv(1)))
                .isZero();
    }

    @Test
    void nhom_02_khongTaoNhomMoCoiKhiDaCoNhom() {
        String ma = tao(1);
        loi(NghiepVuNhomException.Loai.XUNG_DOT, () -> tao(1));
        assertThat(f.count("SELECT COUNT(*) FROM nhom_sinh_vien WHERE ma_nguoi_tao=?", f.sv(1)))
                .isEqualTo(1);
        assertThat(repo.nhomHienTai(f.sv(1))).contains(ma);
    }

    @Test
    void haiLanTaoDongThoiKhongCoNhomMoCoi() throws Exception {
        assertThat(dongThoi(() -> tao(1), () -> tao(1)))
                .containsExactlyInAnyOrder("OK", "XUNG_DOT");
        assertThat(f.count("SELECT COUNT(*) FROM nhom_sinh_vien WHERE ma_nguoi_tao=?", f.sv(1)))
                .isEqualTo(1);
        assertThat(
                        f.count(
                                "SELECT COUNT(*) FROM thanh_vien_nhom WHERE sv_dang_tham_gia=?",
                                f.sv(1)))
                .isEqualTo(1);
    }

    @Test
    void yeuCauTrungVaTuChoiDuocGiuLichSu() {
        String ma = tao(1);
        yeuCau(ma, 2);
        loi(NghiepVuNhomException.Loai.XUNG_DOT, () -> yeuCau(ma, 2));
        as(f.sv(1), "SINH_VIEN", () -> nhom.xuLyYeuCau(ma, f.sv(2), false));
        loi(NghiepVuNhomException.Loai.XUNG_DOT, () -> yeuCau(ma, 2));
        assertThat(repo.thanhVien(ma, f.sv(2), false))
                .get()
                .extracting(ThanhVien::trangThai)
                .isEqualTo("TU_CHOI");
    }

    @Test
    void khongChoNguoiNgoaiDuyetThanhVien() {
        String ma = tao(1);
        yeuCau(ma, 2);
        loi(
                NghiepVuNhomException.Loai.CAM_TRUY_CAP,
                () -> as(f.sv(3), "SINH_VIEN", () -> nhom.xuLyYeuCau(ma, f.sv(2), true)));
        assertThat(repo.nhom(ma).orElseThrow().soThanhVien()).isEqualTo(1);
    }

    @Test
    void nhom_03_haiNhomDuyetCungSinhVienDongThoi() throws Exception {
        String a = tao(1), b = tao(2);
        yeuCau(a, 8);
        yeuCau(b, 8);
        var results =
                dongThoi(
                        () -> as(f.sv(1), "SINH_VIEN", () -> nhom.xuLyYeuCau(a, f.sv(8), true)),
                        () -> as(f.sv(2), "SINH_VIEN", () -> nhom.xuLyYeuCau(b, f.sv(8), true)));
        assertThat(results).containsExactlyInAnyOrder("OK", "XUNG_DOT");
        assertThat(
                        f.count(
                                "SELECT COUNT(*) FROM thanh_vien_nhom WHERE sv_dang_tham_gia=?",
                                f.sv(8)))
                .isEqualTo(1);
        assertThat(
                        f.count(
                                "SELECT COUNT(*) FROM thanh_vien_nhom WHERE ma_sinh_vien=? AND"
                                        + " trang_thai='CHO_DUYET'",
                                f.sv(8)))
                .isEqualTo(1);
    }

    @Test
    void nhom_04_khongVuotNamThanhVienCaKhiDuyetDongThoi() throws Exception {
        String ma = tao(1);
        them(ma, 1, 2);
        them(ma, 1, 3);
        them(ma, 1, 4);
        yeuCau(ma, 5);
        yeuCau(ma, 6);
        assertThat(
                        dongThoi(
                                () ->
                                        as(
                                                f.sv(1),
                                                "SINH_VIEN",
                                                () -> nhom.xuLyYeuCau(ma, f.sv(5), true)),
                                () ->
                                        as(
                                                f.sv(1),
                                                "SINH_VIEN",
                                                () -> nhom.xuLyYeuCau(ma, f.sv(6), true))))
                .containsExactlyInAnyOrder("OK", "XUNG_DOT");
        assertThat(repo.nhom(ma).orElseThrow().soThanhVien()).isEqualTo(5);
        int conLai =
                repo.thanhVien(ma, f.sv(5), false).orElseThrow().trangThai().equals("CHO_DUYET")
                        ? 5
                        : 6;
        loi(
                NghiepVuNhomException.Loai.XUNG_DOT,
                () -> as(f.sv(1), "SINH_VIEN", () -> nhom.xuLyYeuCau(ma, f.sv(conLai), true)));
        assertThat(repo.nhom(ma).orElseThrow().soThanhVien()).isEqualTo(5);
    }

    @Test
    void nhom_05_thanhVienKhongDuocChotDangKyHoacNop() {
        String ma = chot();
        for (Runnable action :
                List.<Runnable>of(
                        () -> nhom.chot(ma),
                        () -> dangKy.dangKy(f.topic),
                        () -> baoCao.nop(file(pdf("cấm"))))) {
            loi(NghiepVuNhomException.Loai.CAM_TRUY_CAP, () -> as(f.sv(2), "SINH_VIEN", action));
        }
        assertThat(repo.dangKy(ma)).isEmpty();
        assertThat(storage.objects).isEmpty();
    }

    @Test
    void nhom_06_haiThanhVienKhongTheChotVaDangKy() {
        String ma = tao(1);
        them(ma, 1, 2);
        loi(NghiepVuNhomException.Loai.DU_LIEU, () -> as(f.sv(1), "SINH_VIEN", () -> nhom.chot(ma)));
        loi(
                NghiepVuNhomException.Loai.DU_LIEU,
                () -> as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(f.topic)));
        assertThat(repo.nhom(ma).orElseThrow().trangThai()).isEqualTo("DANG_LAP");
    }

    @Test
    void xoaThanhVienTruocChotGiuLichSuVaKhongXoaTruong() {
        String ma = tao(1);
        them(ma, 1, 2);
        loi(
                NghiepVuNhomException.Loai.DU_LIEU,
                () -> as(f.sv(1), "SINH_VIEN", () -> nhom.xoaThanhVien(ma, f.sv(1))));
        as(f.sv(1), "SINH_VIEN", () -> nhom.xoaThanhVien(ma, f.sv(2)));
        assertThat(repo.thanhVien(ma, f.sv(2), false).orElseThrow().trangThai())
                .isEqualTo("KET_THUC");
        assertThat(tao(2)).isNotBlank();
    }

    @Test
    void daChotThiThanhVienBatBienVaYeuCauChoDuocTuChoi() {
        String ma = tao(1);
        them(ma, 1, 2);
        them(ma, 1, 3);
        yeuCau(ma, 4);
        as(f.sv(1), "SINH_VIEN", () -> nhom.chot(ma));
        assertThat(repo.nhom(ma).orElseThrow().soThanhVienChot()).isEqualTo(3);
        assertThat(repo.thanhVien(ma, f.sv(4), false).orElseThrow().trangThai())
                .isEqualTo("TU_CHOI");
        loi(
                NghiepVuNhomException.Loai.XUNG_DOT,
                () -> as(f.sv(1), "SINH_VIEN", () -> nhom.xoaThanhVien(ma, f.sv(2))));
    }

    @Test
    void nhom_07_deTaiChuaCongBoHoacNgoaiHanKhongDuocDangKy() {
        String ma = chot();
        String an = f.prefix + "AN";
        f.topic(an, f.dot, "NHAP");
        loi(NghiepVuNhomException.Loai.DU_LIEU, () -> as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(an)));
        jdbc.update("UPDATE dot_dang_ky SET trang_thai='DONG' WHERE ma_dot=?", f.dot);
        loi(
                NghiepVuNhomException.Loai.DU_LIEU,
                () -> as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(f.topic)));
        jdbc.update(
                "UPDATE dot_dang_ky SET"
                    + " trang_thai='CONG_BO',bat_dau_gv=?,ket_thuc_gv=?,bat_dau_sv=?,ket_thuc_sv=?"
                    + " WHERE ma_dot=?",
                NOW.minusDays(10),
                NOW.minusDays(5),
                NOW.minusDays(4),
                NOW.minusDays(1),
                f.dot);
        loi(
                NghiepVuNhomException.Loai.DU_LIEU,
                () -> as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(f.topic)));
        assertThat(repo.dangKy(ma)).isEmpty();
    }

    @Test
    void nhom_08_khongCoBanDangKyThuHaiSauTuChoi() {
        String ma = chot();
        as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(f.topic));
        as(f.dean, "TRUONG_KHOA", () -> dangKy.tuChoi(ma, "Cần điều chỉnh nội dung"));
        String khac = f.prefix + "DT2";
        f.topic(khac, f.dot, "CONG_BO");
        loi(NghiepVuNhomException.Loai.XUNG_DOT, () -> as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(khac)));
        assertThat(repo.dangKy(ma).orElseThrow().lyDoTuChoi()).isEqualTo("Cần điều chỉnh nội dung");
        assertThat(f.count("SELECT COUNT(*) FROM dang_ky_de_tai WHERE ma_nhom=?", ma)).isEqualTo(1);
    }

    @Test
    void haiDangKyDongThoiChiCoMotBanGhi() throws Exception {
        String ma = chot();
        String khac = f.prefix + "DT2";
        f.topic(khac, f.dot, "CONG_BO");
        assertThat(
                        dongThoi(
                                () -> as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(f.topic)),
                                () -> as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(khac))))
                .containsExactlyInAnyOrder("OK", "XUNG_DOT");
        assertThat(f.count("SELECT COUNT(*) FROM dang_ky_de_tai WHERE ma_nhom=?", ma)).isEqualTo(1);
    }

    @Test
    void quyenDuyetLayTuDbVaLyDoTuChoiDuocKiemTra() {
        String ma = chot();
        as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(f.topic));
        loi(
                NghiepVuNhomException.Loai.CAM_TRUY_CAP,
                () -> as(f.sv(2), "TRUONG_KHOA", () -> dangKy.duyet(ma)));
        loi(
                NghiepVuNhomException.Loai.DU_LIEU,
                () -> as(f.dean, "TRUONG_KHOA", () -> dangKy.tuChoi(ma, " ")));
        loi(
                NghiepVuNhomException.Loai.DU_LIEU,
                () -> as(f.dean, "TRUONG_KHOA", () -> dangKy.tuChoi(ma, "a".repeat(1001))));
        assertThat(repo.dangKy(ma).orElseThrow().trangThai()).isEqualTo("CHO_DUYET");
    }

    @Test
    void nhom_09_pdfGiaRongVaQuaLonKhongCoMetadata() {
        String ma = daDuyet();
        for (byte[] bytes :
                List.of(
                        new byte[0],
                        "Đổi đuôi thành PDF".getBytes(),
                        new byte[PdfValidator.MAX_BYTES + 1]))
            loi(NghiepVuNhomException.Loai.DU_LIEU, () -> nop(bytes));
        assertThat(f.count("SELECT COUNT(*) FROM nop_bao_cao WHERE ma_nhom=?", ma)).isZero();
        assertThat(storage.objects).isEmpty();
    }

    @Test
    void nhom_10_pdfDuoiNguongMotByteCoShaVaTaiDuoc() {
        String ma = daDuyet();
        byte[] bytes = pdfSized(PdfValidator.MAX_BYTES - 1, "nhom-10");
        assertThat(bytes).hasSize(PdfValidator.MAX_BYTES - 1);
        var bc = nop(bytes);
        assertThat(bc.phienBan()).isEqualTo(1);
        assertThat(bc.sha256()).isEqualTo(PdfValidator.sha256(bytes));
        assertThat(as(f.sv(2), "SINH_VIEN", () -> baoCao.tai(ma, 1)).bytes()).isEqualTo(bytes);
        assertThat(
                        f.count(
                                "SELECT COUNT(*) FROM nop_bao_cao WHERE ma_nhom=? AND"
                                        + " kich_thuoc_byte=? AND sha256=?",
                                ma,
                                bytes.length,
                                PdfValidator.sha256(bytes)))
                .isEqualTo(1);
    }

    @Test
    void nhom_11_haiUploadDongThoiPhienBanRiengVaKhongCoObjectThua() throws Exception {
        String ma = daDuyet();
        assertThat(dongThoi(() -> nop(pdf("lần một")), () -> nop(pdf("lần hai"))))
                .containsExactly("OK", "OK");
        var rows = repo.danhSachBaoCao(ma, 0);
        assertThat(rows).extracting(BaoCao::phienBan).containsExactly(2, 1);
        assertThat(storage.objects.keySet())
                .containsExactlyInAnyOrderElementsOf(rows.stream().map(BaoCao::objectKey).toList());
        for (var row : rows)
            assertThat(PdfValidator.sha256(storage.objects.get(row.objectKey())))
                    .isEqualTo(row.sha256());
    }

    @Test
    void guiLapPdfKhongTaoPhienBanMoi() {
        String ma = daDuyet();
        byte[] bytes = pdf("gửi lặp");
        var mot = nop(bytes);
        var hai = nop(bytes);
        assertThat(hai).isEqualTo(mot);
        assertThat(repo.danhSachBaoCao(ma, 0)).hasSize(1);
        assertThat(storage.objects).hasSize(1);
    }

    @Test
    void nopLaiPdfSeedThieuObjectTaoPhienBanMoiCoTepThat() {
        String ma = daDuyet();
        byte[] bytes = pdf("seed thiếu object");
        repo.themBaoCao(
                new BaoCao(
                        ma,
                        1,
                        f.sv(1),
                        "seed.pdf",
                        "demo/" + f.prefix + "-seed-v1.pdf",
                        "application/pdf",
                        bytes.length,
                        PdfValidator.sha256(bytes),
                        NOW,
                        "HOP_LE"));
        var bc = nop(bytes);
        assertThat(bc.phienBan()).isEqualTo(2);
        assertThat(repo.danhSachBaoCao(ma, 0)).hasSize(2);
        assertThat(storage.objects.get(bc.objectKey())).isEqualTo(bytes);
    }

    @Test
    void rollbackDbDonObjectVaGiuPhienBanCu() {
        String ma = daDuyet();
        var cu = nop(pdf("cũ"));
        doThrow(new DataIntegrityViolationException("Chủ động gây lỗi ghi metadata"))
                .when(repo)
                .themBaoCao(argThat(b -> b.maNhom().equals(ma) && b.phienBan() == 2));
        assertThatThrownBy(() -> nop(pdf("mới")))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repo.danhSachBaoCao(ma, 0)).hasSize(1);
        assertThat(storage.objects.keySet()).containsExactly(cu.objectKey());
    }

    @Test
    void putDaNhanNhungMatPhanHoiVanDuocDonSauRollback() {
        String ma = daDuyet();
        storage.failAfterPut = true;
        storage.deleteFailures.set(2);
        loi(NghiepVuNhomException.Loai.LUU_TRU, () -> nop(pdf("lỗi kho")));
        assertThat(repo.danhSachBaoCao(ma, 0)).isEmpty();
        assertThat(storage.objects).isEmpty();
    }

    @Test
    void nhom_12_nguoiNgoaiKhongDuocDoanUrlPdfVaGvPhaiDuocPhanCong() throws Exception {
        String ma = daDuyet();
        var bc = nop(pdf("bí mật"));
        loi(
                NghiepVuNhomException.Loai.CAM_TRUY_CAP,
                () -> as(f.sv(9), "SINH_VIEN", () -> baoCao.tai(ma, bc.phienBan())));
        loi(
                NghiepVuNhomException.Loai.CAM_TRUY_CAP,
                () -> as(f.lecturer, "GIANG_VIEN", () -> baoCao.tai(ma, bc.phienBan())));
        jdbc.update(
                "INSERT INTO"
                    + " de_tai_giang_vien(ma_de_tai,ma_giang_vien,loai_phan_cong,ma_nguoi_phan_cong,phan_cong_luc)"
                    + " VALUES(?,?,'HUONG_DAN',?,?)",
                f.topic,
                f.lecturer,
                f.dean,
                NOW);
        assertThat(as(f.lecturer, "GIANG_VIEN", () -> baoCao.tai(ma, 1)).bytes())
                .isEqualTo(pdf("bí mật"));
        mvc.perform(get("/bao-cao/{ma}/1/tai", ma).with(testUser(f.sv(9), "SINH_VIEN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void nhom_13_moiPostThieuHoacSaiCsrfDeu403VaDbKhongDoi() throws Exception {
        String ma = chot();
        var urls =
                List.of(
                        "/nhom/tao",
                        "/nhom/" + ma + "/yeu-cau",
                        "/nhom/" + ma + "/yeu-cau/" + f.sv(4) + "/duyet",
                        "/nhom/" + ma + "/yeu-cau/" + f.sv(4) + "/tu-choi",
                        "/nhom/" + ma + "/thanh-vien/" + f.sv(2) + "/xoa",
                        "/nhom/" + ma + "/chot",
                        "/dang-ky-de-tai",
                        "/dang-ky-de-tai/" + ma + "/duyet",
                        "/dang-ky-de-tai/" + ma + "/tu-choi",
                        "/bao-cao/nop");
        for (String url : urls) {
            mvc.perform(post(url).with(testUser(f.sv(1), "SINH_VIEN")))
                    .andExpect(status().isForbidden());
            mvc.perform(
                            post(url)
                                    .with(testUser(f.sv(1), "SINH_VIEN"))
                                    .with(csrf().useInvalidToken()))
                    .andExpect(status().isForbidden());
        }
        assertThat(repo.dangKy(ma)).isEmpty();
        assertThat(repo.nhom(ma).orElseThrow().soThanhVienChot()).isEqualTo(3);
    }

    @Test
    void nhom_14_seedChiCoMetadataThi404KhongTraFileRong() throws Exception {
        String ma = daDuyet();
        repo.themBaoCao(
                new BaoCao(
                        ma,
                        1,
                        f.sv(1),
                        "seed.pdf",
                        "demo/" + f.prefix + "-seed-v1.pdf",
                        "application/pdf",
                        500,
                        "a".repeat(64),
                        NOW,
                        "HOP_LE"));
        mvc.perform(get("/bao-cao/{ma}/1/tai", ma).with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isNotFound())
                .andExpect(
                        content()
                                .string(
                                        containsString(
                                                "Không tìm thấy tệp PDF trong kho lưu trữ")));
        assertThat(repo.baoCao(ma, 1)).isPresent();
    }

    @Test
    void hetHanHoacDangChamThiKhongDuocNopThem() {
        String ma = daDuyet();
        jdbc.update("UPDATE dang_ky_de_tai SET trang_thai='DANG_CHAM' WHERE ma_nhom=?", ma);
        loi(NghiepVuNhomException.Loai.DU_LIEU, () -> nop(pdf("muộn")));
        jdbc.update("UPDATE dang_ky_de_tai SET trang_thai='DANG_THUC_HIEN' WHERE ma_nhom=?", ma);
        jdbc.update(
                "UPDATE dot_dang_ky SET ket_thuc_sv=?,han_nop_bao_cao=? WHERE ma_dot=?",
                NOW.minusDays(2),
                NOW.minusDays(1),
                f.dot);
        loi(NghiepVuNhomException.Loai.DU_LIEU, () -> nop(pdf("muộn")));
        assertThat(storage.objects).isEmpty();
    }

    @Test
    void fileSaiChecksumKhongDuocTaiThanhCong() {
        String ma = daDuyet();
        var bc = nop(pdf("đúng"));
        storage.objects.put(bc.objectKey(), pdf("sai"));
        loi(NghiepVuNhomException.Loai.LUU_TRU, () -> as(f.sv(1), "SINH_VIEN", () -> baoCao.tai(ma, 1)));
    }

    @Test
    void tamTemplateRenderBangDuLieuMySqlVaModelThat() throws Exception {
        String ma = tao(1);
        yeuCau(ma, 2);
        mvc.perform(
                        get("/nhom")
                                .param("tuKhoa", f.sv(1))
                                .param("maDot", f.dot)
                                .with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/list"))
                .andExpect(content().string(containsString(ma)));
        mvc.perform(get("/nhom/{ma}", ma).with(testUser(f.sv(3), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/detail"));
        mvc.perform(get("/nhom/tao").with(testUser(f.sv(9), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/form"))
                .andExpect(content().string(containsString("name=\"tenNhom\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(get("/nhom/cua-toi").with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/cua-toi"))
                .andExpect(content().string(containsString("/yeu-cau/" + f.sv(2) + "/duyet")));
        as(f.sv(1), "SINH_VIEN", () -> nhom.xuLyYeuCau(ma, f.sv(2), true));
        them(ma, 1, 3);
        as(f.sv(1), "SINH_VIEN", () -> nhom.chot(ma));
        mvc.perform(
                        get("/dang-ky-de-tai")
                                .param("maDot", f.dot)
                                .with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(view().name("registrations/list"))
                .andExpect(content().string(containsString("name=\"maDeTai\"")))
                .andExpect(content().string(not(containsString("name=\"maNhom\""))));
        mvc.perform(
                        post("/dang-ky-de-tai")
                                .param("maDeTai", f.topic)
                                .param("maNhom", "nhom-gia")
                                .param("maNguoiDangKy", f.sv(9))
                                .with(testUser(f.sv(1), "SINH_VIEN"))
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dang-ky-de-tai"));
        assertThat(repo.dangKy(ma)).isPresent();
        mvc.perform(
                        get("/dang-ky-de-tai/duyet")
                                .param("tuKhoa", ma)
                                .with(testUser(f.dean, "TRUONG_KHOA")))
                .andExpect(status().isOk())
                .andExpect(view().name("registrations/review"));
        as(f.dean, "TRUONG_KHOA", () -> dangKy.duyet(ma));
        nop(pdf("lịch sử"));
        mvc.perform(get("/bao-cao").with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/list"))
                .andExpect(content().string(containsString("/bao-cao/" + ma + "/1/tai")));
        mvc.perform(get("/bao-cao/nop").with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/form"))
                .andExpect(content().string(containsString("name=\"tepBaoCao\"")));
    }

    @Test
    void formLoiGiuBindingVaFlashSauTao() throws Exception {
        mvc.perform(
                        post("/nhom/tao")
                                .param("tenNhom", "")
                                .param("maDot", f.dot)
                                .with(testUser(f.sv(1), "SINH_VIEN"))
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/form"))
                .andExpect(model().attributeHasFieldErrors("taoNhomForm", "tenNhom"))
                .andExpect(content().string(containsString("Vui lòng nhập tên nhóm")));
        mvc.perform(
                        post("/nhom/tao")
                                .param("tenNhom", "Nhóm HTTP")
                                .param("maDot", f.dot)
                                .param("maNguoiTao", f.sv(2))
                                .with(testUser(f.sv(1), "SINH_VIEN"))
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nhom/cua-toi"))
                .andExpect(flash().attributeExists("successMessage"));
        assertThat(repo.nhomHienTai(f.sv(1))).isPresent();
        assertThat(repo.nhomHienTai(f.sv(2))).isEmpty();
    }

    @Test
    void phanQuyenVanCamDuyetVaGhiNhomCuaNguoiKhac() throws Exception {
        String ma = chot();
        as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(f.topic));
        mvc.perform(
                        post("/dang-ky-de-tai/{ma}/duyet", ma)
                                .with(testUser(f.sv(9), "TRUONG_KHOA"))
                                .with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/nhom/{ma}/chot", ma).with(testUser(f.sv(9), "SINH_VIEN")).with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(repo.dangKy(ma).orElseThrow().trangThai()).isEqualTo("CHO_DUYET");
    }

    @Test
    void locTaiDbVaPhanTrangCoGioiHan() throws Exception {
        for (int i = 0; i < 23; i++)
            repo.taoNhom(f.prefix + "N" + i, "Lọc " + f.prefix, f.dot, f.sv(1));
        mvc.perform(
                        get("/nhom")
                                .param("tuKhoa", f.prefix)
                                .param("maDot", f.dot)
                                .with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("coTrangSau", true))
                .andExpect(model().attribute("danhSachNhom", org.hamcrest.Matchers.hasSize(20)));
        mvc.perform(
                        get("/nhom")
                                .param("tuKhoa", f.prefix)
                                .param("maDot", f.dot)
                                .param("trang", "1")
                                .with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("danhSachNhom", org.hamcrest.Matchers.hasSize(3)));
    }

    @Test
    void routeTuChoiYeuCauVaXoaThanhVienCapNhatDb() throws Exception {
        String ma = tao(1);
        them(ma, 1, 2);
        yeuCau(ma, 3);
        mvc.perform(
                        post("/nhom/{ma}/yeu-cau/{sv}/tu-choi", ma, f.sv(3))
                                .with(testUser(f.sv(1), "SINH_VIEN"))
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nhom/cua-toi"));
        assertThat(repo.thanhVien(ma, f.sv(3), false).orElseThrow().trangThai())
                .isEqualTo("TU_CHOI");
        mvc.perform(
                        post("/nhom/{ma}/thanh-vien/{sv}/xoa", ma, f.sv(2))
                                .with(testUser(f.sv(1), "SINH_VIEN"))
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nhom/cua-toi"));
        assertThat(repo.thanhVien(ma, f.sv(2), false).orElseThrow().trangThai())
                .isEqualTo("KET_THUC");
    }

    @Test
    void routeTuChoiDangKyLuuVaHienThiLyDo() throws Exception {
        String ma = chot();
        as(f.sv(1), "SINH_VIEN", () -> dangKy.dangKy(f.topic));
        mvc.perform(
                        post("/dang-ky-de-tai/{ma}/tu-choi", ma)
                                .param("lyDoTuChoi", "   ")
                                .with(testUser(f.dean, "TRUONG_KHOA"))
                                .with(csrf()))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/dang-ky-de-tai/{ma}/tu-choi", ma)
                                .param("lyDoTuChoi", "Cần bổ sung phạm vi")
                                .with(testUser(f.dean, "TRUONG_KHOA"))
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dang-ky-de-tai/duyet"));
        mvc.perform(
                        get("/dang-ky-de-tai")
                                .param("maDot", f.dot)
                                .with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cần bổ sung phạm vi")));
        assertThat(repo.dangKy(ma).orElseThrow().trangThai()).isEqualTo("TU_CHOI");
    }

    @Test
    void nhomKetThucVanXemDuocLichSuVaTaiPdfDaChot() throws Exception {
        String ma = daDuyet();
        byte[] bytes = pdf("lịch sử đã kết thúc");
        nop(bytes);
        jdbc.update(
                "UPDATE thanh_vien_nhom SET trang_thai='KET_THUC',ket_thuc_luc=? WHERE ma_nhom=?"
                    + " AND trang_thai='THAM_GIA'",
                NOW,
                ma);
        jdbc.update(
                "UPDATE nhom_sinh_vien SET trang_thai='HOAN_THANH',ket_thuc_luc=? WHERE ma_nhom=?",
                NOW,
                ma);
        mvc.perform(get("/bao-cao").with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/bao-cao/" + ma + "/1/tai")))
                .andExpect(model().attribute("coTheNopBaoCao", false));
        assertThat(as(f.sv(2), "SINH_VIEN", () -> baoCao.tai(ma, 1)).bytes()).isEqualTo(bytes);
        assertThat(tao(1)).isNotEqualTo(ma);
        assertThat(repo.baoCao(ma, 1)).isPresent();
    }

    @Test
    void taiKhoanBiKhoaKhongTheGhiDuQaOpen() throws Exception {
        jdbc.update("UPDATE nguoi_dung SET trang_thai='KHOA' WHERE ma_nguoi_dung=?", f.sv(1));
        mvc.perform(
                        post("/nhom/tao")
                                .param("tenNhom", "Tài khoản khóa")
                                .param("maDot", f.dot)
                                .with(testUser(f.sv(1), "SINH_VIEN"))
                                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(repo.nhomHienTai(f.sv(1))).isEmpty();
    }

    private List<String> dongThoi(Runnable a, Runnable b) throws Exception {
        try (var pool = Executors.newFixedThreadPool(2)) {
            var gate = new CountDownLatch(1);
            var ready = new CountDownLatch(2);
            var futures = new ArrayList<Future<String>>();
            for (Runnable action : List.of(a, b))
                futures.add(
                        pool.submit(
                                () -> {
                                    ready.countDown();
                                    gate.await();
                                    try {
                                        action.run();
                                        return "OK";
                                    } catch (NghiepVuNhomException ex) {
                                        return ex.getLoai().name();
                                    }
                                }));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            gate.countDown();
            return List.of(
                    futures.get(0).get(40, TimeUnit.SECONDS),
                    futures.get(1).get(40, TimeUnit.SECONDS));
        }
    }
}
