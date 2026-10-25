package vn.edu.group12.qldt.service.hoidong;

import java.sql.Timestamp;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import vn.edu.group12.qldt.dto.hoidong.HoiDongSuaForm;
import vn.edu.group12.qldt.support.CiDatabaseGuard;
import vn.edu.group12.qldt.support.SecurityTestUsers;

import static org.assertj.core.api.Assertions.*;

@Tag("mysql")
@SpringBootTest
@ActiveProfiles({"ci-mysql", "staging"})
@ContextConfiguration(initializers = CiDatabaseGuard.class)
@Transactional
class HoiDongSuaMySqlTests {
    @Autowired JdbcTemplate jdbc;
    @Autowired HoiDongService service;
    @Autowired PlatformTransactionManager transactionManager;

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void renamePreservesRoundCreatorCreationTimeAndAllMembers() {
        DuLieu data = taoDuLieu();
        dangNhap(data);
        Timestamp createdAt = jdbc.queryForObject(
            "SELECT created_at FROM hoi_dong WHERE ma_hoi_dong = ?", Timestamp.class, data.maCu());

        var daLuu = service.suaThongTin(data.maCu(), form(data.maMoi()));

        assertThat(daLuu.getMaHoiDong()).isEqualTo(data.maMoi());
        assertThat(daLuu.getTenHoiDong()).isEqualTo("Tên mới");
        assertThat(daLuu.getDiaDiem()).isEqualTo("Phòng mới");
        assertThat(daLuu.getMaDot()).isEqualTo(data.maDot());
        assertThat(daLuu.getMaNguoiTao()).isEqualTo(data.maTk());
        assertThat(daLuu.getCreatedAt()).isEqualTo(createdAt.toLocalDateTime());
        assertThat(daLuu.getVersion()).isEqualTo(1L);
        assertThat(daLuu.getTrangThai().name()).isEqualTo("NHAP");
        assertThat(demHoiDong(data.maCu())).isZero();
        assertThat(jdbc.queryForList(
            "SELECT chuc_danh FROM thanh_vien_hoi_dong WHERE ma_hoi_dong = ?", String.class, data.maMoi()))
            .containsExactlyInAnyOrder("CHU_TICH", "THU_KY", "UY_VIEN");
        assertThat(jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.referential_constraints
            WHERE constraint_schema = DATABASE()
              AND constraint_name IN ('fk_tvhd_hoi_dong_cascade', 'fk_hddt_hoi_dong_cascade')
              AND update_rule = 'CASCADE' AND delete_rule = 'RESTRICT'
            """, Integer.class)).isEqualTo(2);
    }

    @Test
    void unchangedCodeCanUpdateNameAndLocationWithoutMovingMembers() {
        DuLieu data = taoDuLieu();
        dangNhap(data);
        service.suaThongTin(data.maCu(), form(data.maCu()));
        assertThat(demHoiDong(data.maCu())).isEqualTo(1);
        assertThat(jdbc.queryForObject(
            "SELECT COUNT(*) FROM thanh_vien_hoi_dong WHERE ma_hoi_dong = ?", Integer.class, data.maCu()))
            .isEqualTo(3);
    }

    @Test
    void duplicateCodeDoesNotMergeTwoCouncilsOrTheirMembers() {
        DuLieu data = taoDuLieu();
        jdbc.update("""
            INSERT INTO hoi_dong (ma_hoi_dong, ten_hoi_dong, ma_dot, ma_nguoi_tao)
            VALUES (?, 'Hội đồng khác', ?, ?)
            """, data.maMoi(), data.maDot(), data.maTk());
        dangNhap(data);
        assertThatThrownBy(() -> service.suaThongTin(data.maCu(), form(data.maMoi())))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("đã tồn tại");
        assertThat(jdbc.queryForObject(
            "SELECT COUNT(*) FROM thanh_vien_hoi_dong WHERE ma_hoi_dong = ?", Integer.class, data.maCu()))
            .isEqualTo(3);
        assertThat(jdbc.queryForObject(
            "SELECT ten_hoi_dong FROM hoi_dong WHERE ma_hoi_dong = ?", String.class, data.maMoi()))
            .isEqualTo("Hội đồng khác");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void transactionRollbackRestoresOriginalCodeAndMemberLinks() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        DuLieu data = transaction.execute(status -> taoDuLieu());
        try {
            dangNhap(data);
            assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
                service.suaThongTin(data.maCu(), form(data.maMoi()));
                throw new IllegalStateException("Chủ động rollback kiểm thử");
            })).isInstanceOf(IllegalStateException.class);

            assertThat(demHoiDong(data.maCu())).isEqualTo(1);
            assertThat(demHoiDong(data.maMoi())).isZero();
            assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM thanh_vien_hoi_dong WHERE ma_hoi_dong = ?", Integer.class, data.maCu()))
                .isEqualTo(3);
        } finally {
            transaction.executeWithoutResult(status -> {
                jdbc.update("DELETE FROM thanh_vien_hoi_dong WHERE ma_hoi_dong IN (?, ?)", data.maCu(), data.maMoi());
                jdbc.update("DELETE FROM hoi_dong WHERE ma_hoi_dong IN (?, ?)", data.maCu(), data.maMoi());
                jdbc.update("DELETE FROM dot_dang_ky WHERE ma_dot = ?", data.maDot());
                jdbc.update("DELETE FROM nguoi_dung_vai_tro WHERE ma_nguoi_dung = ?", data.maTk());
                jdbc.update("DELETE FROM nguoi_dung WHERE ma_nguoi_dung IN (?, ?, ?, ?)",
                    data.maTk(), data.maCu() + "G1", data.maCu() + "G2", data.maCu() + "G3");
            });
        }
    }

    private DuLieu taoDuLieu() {
        String ma = "CE_" + UUID.randomUUID().toString().substring(0, 10);
        DuLieu data = new DuLieu(ma, ma + "NEW", ma + "DOT", ma + "TK");
        jdbc.update("""
            INSERT INTO nguoi_dung (ma_nguoi_dung, ho_ten, email, mat_khau_hash, loai_nguoi_dung)
            VALUES (?, 'Trưởng khoa kiểm thử', ?, 'not-a-login-password', 'CAN_BO')
            """, data.maTk(), data.maTk() + "@ci.invalid");
        jdbc.update("INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro) VALUES (?, 'TRUONG_KHOA')", data.maTk());
        jdbc.update("""
            INSERT INTO dot_dang_ky (ma_dot, ten_dot, loai_de_tai, bat_dau_gv, ket_thuc_gv,
                bat_dau_sv, ket_thuc_sv, han_nop_diem_pb, ngay_bao_cao_hd, ma_nguoi_tao)
            VALUES (?, 'Đợt kiểm thử', 'KLTN', '2026-01-01', '2026-01-02', '2026-01-03',
                '2026-01-04', '2026-01-06', '2026-01-07', ?)
            """, data.maDot(), data.maTk());
        jdbc.update("""
            INSERT INTO hoi_dong (ma_hoi_dong, ten_hoi_dong, ma_dot, ma_nguoi_tao)
            VALUES (?, 'Tên ban đầu', ?, ?)
            """, data.maCu(), data.maDot(), data.maTk());
        String[] chucDanh = {"CHU_TICH", "THU_KY", "UY_VIEN"};
        for (int i = 0; i < chucDanh.length; i++) {
            String maGv = data.maCu() + "G" + (i + 1);
            jdbc.update("""
                INSERT INTO nguoi_dung (ma_nguoi_dung, ho_ten, email, mat_khau_hash, loai_nguoi_dung)
                VALUES (?, 'Giảng viên kiểm thử', ?, 'not-a-login-password', 'GV')
                """, maGv, maGv + "@ci.invalid");
            jdbc.update("""
                INSERT INTO thanh_vien_hoi_dong (ma_hoi_dong, ma_giang_vien, chuc_danh)
                VALUES (?, ?, ?)
                """, data.maCu(), maGv, chucDanh[i]);
        }
        return data;
    }

    private void dangNhap(DuLieu data) {
        SecurityContextHolder.getContext().setAuthentication(
            SecurityTestUsers.authentication(data.maTk(), "TRUONG_KHOA"));
    }

    private HoiDongSuaForm form(String maMoi) {
        HoiDongSuaForm form = new HoiDongSuaForm();
        form.setMaHoiDong(maMoi);
        form.setTenHoiDong(" Tên mới ");
        form.setDiaDiem(" Phòng mới ");
        form.setVersion(0L);
        return form;
    }

    private int demHoiDong(String ma) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM hoi_dong WHERE ma_hoi_dong = ?", Integer.class, ma);
    }

    private record DuLieu(String maCu, String maMoi, String maDot, String maTk) { }
}
