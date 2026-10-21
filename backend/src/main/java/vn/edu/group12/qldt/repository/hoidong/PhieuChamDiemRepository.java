package vn.edu.group12.qldt.repository.hoidong;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.group12.qldt.dto.hoidong.NhomChamDiemHienThi;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiem;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiemId;

public interface PhieuChamDiemRepository
        extends JpaRepository<PhieuChamDiem, PhieuChamDiemId> {

    // Lấy các phiếu của một nhóm, sắp xếp theo mã giảng viên.
    List<PhieuChamDiem>
        findById_MaNhomOrderById_MaGiangVienAsc(String maNhom);

    // Chỉ liệt kê nhóm đang chấm thuộc hội đồng của giảng viên.
    // Loại các đề tài mà chính giảng viên đó đang hướng dẫn.
    @Query(
        value = """
            SELECT
                nh.ma_nhom AS maNhom,
                nh.ten_nhom AS tenNhom,
                dt.ma_de_tai AS maDeTai,
                dt.ten_de_tai AS tenDeTai,
                hd.ma_hoi_dong AS maHoiDong,
                hd.ten_hoi_dong AS tenHoiDong
            FROM hoi_dong_de_tai pc
            JOIN hoi_dong hd
                ON hd.ma_hoi_dong = pc.ma_hoi_dong
            JOIN thanh_vien_hoi_dong tv
                ON tv.ma_hoi_dong = hd.ma_hoi_dong
            JOIN dang_ky_de_tai dk
                ON dk.ma_nhom = pc.ma_nhom
            JOIN nhom_sinh_vien nh
                ON nh.ma_nhom = dk.ma_nhom
            JOIN de_tai dt
                ON dt.ma_de_tai = dk.ma_de_tai
            JOIN dot_dang_ky dot
                ON dot.ma_dot = hd.ma_dot
            WHERE tv.ma_giang_vien = :maGiangVien
                AND hd.trang_thai = 'DANG_CHAM'
                AND dk.trang_thai = 'DANG_CHAM'
                AND nh.ma_dot = hd.ma_dot
                AND dt.ma_dot = hd.ma_dot
                AND dot.loai_de_tai = 'KLTN'
                AND NOT EXISTS (
                    SELECT 1
                    FROM de_tai_giang_vien gv
                    WHERE gv.ma_de_tai = dk.ma_de_tai
                        AND gv.ma_giang_vien = :maGiangVien
                        AND gv.loai_phan_cong = 'HUONG_DAN'
                )
            ORDER BY hd.ma_hoi_dong, nh.ma_nhom
            """,
        nativeQuery = true
    )
    List<NhomChamDiemHienThi> layDanhSachNhomDuocCham(
        @Param("maGiangVien") String maGiangVien
    );

    // Kiểm tra giảng viên có hướng dẫn đề tài mà nhóm đăng ký không.
    // Service sẽ dùng kết quả này để chặn lưu hoặc nộp điểm.
    @Query(
        value = """
            SELECT COUNT(*)
            FROM dang_ky_de_tai dk
            JOIN de_tai_giang_vien gv
                ON gv.ma_de_tai = dk.ma_de_tai
            WHERE dk.ma_nhom = :maNhom
                AND gv.ma_giang_vien = :maGiangVien
                AND gv.loai_phan_cong = 'HUONG_DAN'
            """,
        nativeQuery = true
    )
    long demPhanCongHuongDan(
        @Param("maNhom") String maNhom,
        @Param("maGiangVien") String maGiangVien
    );

}