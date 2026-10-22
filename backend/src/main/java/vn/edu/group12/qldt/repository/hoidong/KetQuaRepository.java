package vn.edu.group12.qldt.repository.hoidong;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.group12.qldt.entity.hoidong.KetQua;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.group12.qldt.dto.hoidong.NguoiNhanKetQua;

public interface KetQuaRepository extends JpaRepository<KetQua, String> {

    // Khóa chính là mã nhóm; JpaRepository đã có findById và save.
    // Chỉ lấy thành viên đã chốt và còn tham gia nhóm.
    @Query(
        value = """
            SELECT
                tv.ma_sinh_vien AS maSinhVien,
                nd.ho_ten AS hoTen,
                nd.email AS email
            FROM thanh_vien_nhom tv
            JOIN nguoi_dung nd
                ON nd.ma_nguoi_dung = tv.ma_sinh_vien
            WHERE tv.ma_nhom = :maNhom
                AND tv.trang_thai = 'THAM_GIA'
                AND tv.la_thanh_vien_chot = 1
            ORDER BY tv.ma_sinh_vien
            """,
        nativeQuery = true
    )
    List<NguoiNhanKetQua> layNguoiNhanTheoNhom(
        @Param("maNhom") String maNhom
    );

}