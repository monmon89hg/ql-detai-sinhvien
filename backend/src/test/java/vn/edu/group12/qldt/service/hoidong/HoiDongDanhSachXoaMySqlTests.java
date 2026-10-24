package vn.edu.group12.qldt.service.hoidong;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.entity.hoidong.HoiDong;
import vn.edu.group12.qldt.support.CiDatabaseGuard;
import vn.edu.group12.qldt.support.SecurityTestUsers;

import static org.assertj.core.api.Assertions.*;

@Tag("mysql")
@SpringBootTest
@ActiveProfiles({"ci-mysql", "staging"})
@ContextConfiguration(initializers = CiDatabaseGuard.class)
@Transactional
class HoiDongDanhSachXoaMySqlTests {
    @Autowired JdbcTemplate jdbc;
    @Autowired HoiDongService service;

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void latestCreationTimeWinsOverCodeAndStatusWithoutFallingBackToOldCouncils() {
        DuLieu data = taoDuLieu();
        taoHoiDong(data.maHoiDong(), data.maCu(), data.maTk(), "DA_KHOA");
        var latest = service.traCuuDanhSach(null);
        assertThat(latest.maDotDuocChon()).isEqualTo(data.maMoi());
        assertThat(latest.danhSachHoiDong()).isEmpty();
        assertThat(latest.danhSachDot().getFirst().getMaDot()).isEqualTo(data.maMoi());

        var history = service.traCuuDanhSach(data.maCu());
        assertThat(history.danhSachHoiDong()).extracting(HoiDong::getMaHoiDong)
            .containsExactly(data.maHoiDong());
        assertThat(history.maHoiDongCoTheXoa()).isEmpty();
        assertThat(service.traCuuDanhSach("").danhSachHoiDong())
            .extracting(HoiDong::getMaHoiDong).contains(data.maHoiDong());
    }

    @Test
    void onlyAnEmptyDraftCanBeDeletedAndExistingMemberLinksSurviveRejections() {
        DuLieu data = taoDuLieu();
        String empty = data.maHoiDong();
        String linked = empty + "TV";
        String locked = empty + "LOCK";
        taoHoiDong(empty, data.maMoi(), data.maTk(), "NHAP");
        taoHoiDong(linked, data.maMoi(), data.maTk(), "NHAP");
        taoHoiDong(locked, data.maMoi(), data.maTk(), "DA_KHOA");
        String gv = empty + "GV";
        jdbc.update("""
            INSERT INTO nguoi_dung (ma_nguoi_dung, ho_ten, email, mat_khau_hash, loai_nguoi_dung)
            VALUES (?, 'Giảng viên kiểm thử', ?, 'not-a-login-password', 'GV')
            """, gv, gv + "@ci.invalid");
        jdbc.update("""
            INSERT INTO thanh_vien_hoi_dong (ma_hoi_dong, ma_giang_vien, chuc_danh)
            VALUES (?, ?, 'CHU_TICH')
            """, linked, gv);

        assertThat(service.traCuuDanhSach(data.maMoi()).maHoiDongCoTheXoa()).containsExactly(empty);
        assertThatThrownBy(() -> service.xoaHoiDong(linked, 0L))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("thành viên");
        assertThatThrownBy(() -> service.xoaHoiDong(locked, 0L))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("nháp");
        assertThatThrownBy(() -> service.xoaHoiDong(empty, 1L))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("đã thay đổi");

        service.xoaHoiDong(empty, 0L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM hoi_dong WHERE ma_hoi_dong = ?",
            Integer.class, empty)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM thanh_vien_hoi_dong WHERE ma_hoi_dong = ?",
            Integer.class, linked)).isEqualTo(1);
        assertThat(service.traCuuDanhSach(data.maMoi()).danhSachHoiDong())
            .extracting(HoiDong::getMaHoiDong).containsExactlyInAnyOrder(linked, locked);
    }

    private DuLieu taoDuLieu() {
        String ma = "CL_" + UUID.randomUUID().toString().substring(0, 10);
        DuLieu data = new DuLieu(ma + "TK", ma + "ZOLD", ma + "ANEW", ma + "HD");
        jdbc.update("""
            INSERT INTO nguoi_dung (ma_nguoi_dung, ho_ten, email, mat_khau_hash, loai_nguoi_dung)
            VALUES (?, 'Trưởng khoa kiểm thử', ?, 'not-a-login-password', 'CAN_BO')
            """, data.maTk(), data.maTk() + "@ci.invalid");
        jdbc.update("INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro) VALUES (?, 'TRUONG_KHOA')",
            data.maTk());
        for (String maDot : new String[]{data.maCu(), data.maMoi()}) {
            jdbc.update("""
                INSERT INTO dot_dang_ky (ma_dot, ten_dot, loai_de_tai, bat_dau_gv, ket_thuc_gv,
                    bat_dau_sv, ket_thuc_sv, han_nop_diem_pb, ngay_bao_cao_hd,
                    ma_nguoi_tao, trang_thai, created_at)
                VALUES (?, 'Đợt kiểm thử', 'KLTN', '2026-01-01', '2026-01-02', '2026-01-03',
                    '2026-01-04', '2026-01-06', '2026-01-07', ?, ?, ?)
                """, maDot, data.maTk(), maDot.equals(data.maMoi()) ? "DONG" : "CONG_BO",
                    maDot.equals(data.maMoi()) ? "9999-01-01 00:00:00" : "2025-01-01 00:00:00");
        }
        SecurityContextHolder.getContext().setAuthentication(
            SecurityTestUsers.authentication(data.maTk(), "TRUONG_KHOA"));
        return data;
    }

    private void taoHoiDong(String ma, String maDot, String maTk, String trangThai) {
        jdbc.update("""
            INSERT INTO hoi_dong (ma_hoi_dong, ten_hoi_dong, ma_dot, ma_nguoi_tao, trang_thai)
            VALUES (?, 'Hội đồng kiểm thử', ?, ?, ?)
            """, ma, maDot, maTk, trangThai);
    }

    private record DuLieu(String maTk, String maCu, String maMoi, String maHoiDong) { }
}
