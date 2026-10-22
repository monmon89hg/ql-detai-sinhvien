package vn.edu.group12.qldt.service.hoidong;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.dto.hoidong.KetQuaTinhDiem;
import vn.edu.group12.qldt.entity.hoidong.HoiDong;
import vn.edu.group12.qldt.entity.hoidong.HoiDongDeTai;
import vn.edu.group12.qldt.entity.hoidong.KetQua;
import vn.edu.group12.qldt.enums.hoidong.ChucDanhHoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiHoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiKetQua;
import vn.edu.group12.qldt.repository.hoidong.HoiDongDeTaiRepository;
import vn.edu.group12.qldt.repository.hoidong.HoiDongRepository;
import vn.edu.group12.qldt.repository.hoidong.KetQuaRepository;
import vn.edu.group12.qldt.repository.hoidong.ThanhVienHoiDongRepository;

import java.util.List;
import vn.edu.group12.qldt.dto.hoidong.NhomKetQuaHienThi;

@Service
public class TongHopKetQuaService {

    private final HoiDongDeTaiRepository hoiDongDeTaiRepository;
    private final HoiDongRepository hoiDongRepository;
    private final ThanhVienHoiDongRepository thanhVienHoiDongRepository;
    private final KetQuaRepository ketQuaRepository;
    private final TinhDiemTrungBinhService tinhDiemTrungBinhService;

    public TongHopKetQuaService(
            HoiDongDeTaiRepository hoiDongDeTaiRepository,
            HoiDongRepository hoiDongRepository,
            ThanhVienHoiDongRepository thanhVienHoiDongRepository,
            KetQuaRepository ketQuaRepository,
            TinhDiemTrungBinhService tinhDiemTrungBinhService) {
        this.hoiDongDeTaiRepository = hoiDongDeTaiRepository;
        this.hoiDongRepository = hoiDongRepository;
        this.thanhVienHoiDongRepository = thanhVienHoiDongRepository;
        this.ketQuaRepository = ketQuaRepository;
        this.tinhDiemTrungBinhService = tinhDiemTrungBinhService;
    }

    // Chủ tịch chỉ thấy các nhóm thuộc hội đồng mình phụ trách.
    @PreAuthorize("hasRole('GIANG_VIEN')")
    @Transactional(readOnly = true)
    public List<NhomKetQuaHienThi> layDanhSachNhomChoChuTich() {
        String maGiangVien = layMaNguoiDungDangNhap();

        if (thanhVienHoiDongRepository.demGiangVienHopLe(maGiangVien) == 0) {
            throw new AccessDeniedException(
                "Tài khoản giảng viên không còn quyền xem danh sách."
            );
        }

        return hoiDongDeTaiRepository
            .layDanhSachNhomChoChuTich(maGiangVien);
    }

    // Trưởng khoa xem các nhóm đã được phân công hội đồng.
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(readOnly = true)
    public List<NhomKetQuaHienThi> layDanhSachNhomChoTruongKhoa() {
        String maTruongKhoa = layMaNguoiDungDangNhap();

        if (hoiDongRepository.demTruongKhoaHoatDong(maTruongKhoa) == 0) {
            throw new AccessDeniedException(
                "Tài khoản không còn quyền quản lý kết quả."
            );
        }

        return hoiDongDeTaiRepository
            .layDanhSachNhomChoTruongKhoa();
    }

    // Chỉ chủ tịch hội đồng được tổng hợp; toàn bộ kiểm tra và lưu cùng transaction.
    @PreAuthorize("hasRole('GIANG_VIEN')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public KetQua tongHop(String maNhom, String nhanXetTongHop) {
        if (maNhom == null || maNhom.isBlank()) {
            throw new IllegalArgumentException("Mã nhóm không được để trống.");
        }

        String maGiangVien = layMaNguoiDungDangNhap();

        if (thanhVienHoiDongRepository.demGiangVienHopLe(maGiangVien) == 0) {
            throw new AccessDeniedException(
                "Tài khoản giảng viên không còn quyền tổng hợp."
            );
        }

        HoiDongDeTai phanCong = hoiDongDeTaiRepository.findById(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Nhóm chưa được phân công hội đồng."
            ));

        String maHoiDong = phanCong.getMaHoiDong();

        // Cùng thứ tự khóa với chức năng chấm: hội đồng trước, đăng ký sau.
        HoiDong hoiDong = hoiDongRepository.timVaKhoaTheoMa(maHoiDong)
            .orElseThrow(() -> new IllegalArgumentException(
                "Hội đồng không còn tồn tại."
            ));

        hoiDongDeTaiRepository.khoaDangKyTheoMaNhom(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Đăng ký đề tài không còn tồn tại."
            ));

        // Đọc lại trực tiếp từ database sau khi giữ khóa đăng ký.
        String maHoiDongHienTai = hoiDongDeTaiRepository
            .timMaHoiDongHienTai(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Phân công hội đồng đã thay đổi."
            ));

        if (!maHoiDong.equals(maHoiDongHienTai)) {
            throw new IllegalArgumentException(
                "Hội đồng của nhóm đã thay đổi. Vui lòng tải lại trang."
            );
        }

        String trangThaiDangKy = hoiDongDeTaiRepository
            .timTrangThaiDangKyTheoMaNhom(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Đăng ký đề tài không còn tồn tại."
            ));

        if (hoiDong.getTrangThai() != TrangThaiHoiDong.DANG_CHAM
                || !"DANG_CHAM".equals(trangThaiDangKy)) {
            throw new IllegalArgumentException(
                "Chỉ tổng hợp khi hội đồng và đăng ký đang ở trạng thái chấm."
            );
        }

        if (!thanhVienHoiDongRepository
                .existsById_MaHoiDongAndId_MaGiangVienAndChucDanh(
                    maHoiDong,
                    maGiangVien,
                    ChucDanhHoiDong.CHU_TICH
                )) {
            throw new AccessDeniedException(
                "Chỉ chủ tịch hội đồng được tổng hợp kết quả."
            );
        }

        if (ketQuaRepository.existsById(maNhom)) {
            throw new IllegalArgumentException(
                "Nhóm đã có kết quả tổng hợp."
            );
        }

        // Service tính điểm sẽ kiểm tra đủ phiếu đã nộp trước khi tính.
        KetQuaTinhDiem diem = tinhDiemTrungBinhService.tinhDiem(maNhom);

        KetQua ketQua = new KetQua();
        ketQua.setMaNhom(maNhom);
        ketQua.setSoPhieu(diem.soPhieu());
        ketQua.setTongDiem(diem.tongDiem());
        ketQua.setDiemCuoi(diem.diemCuoi());
        ketQua.setNhanXetTongHop(
            nhanXetTongHop == null || nhanXetTongHop.isBlank()
                ? null
                : nhanXetTongHop.strip()
        );
        ketQua.setMaNguoiTongHop(maGiangVien);
        ketQua.setTongHopLuc(
            LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        );
        ketQua.setTrangThai(TrangThaiKetQua.DA_TONG_HOP);
        ketQua.setMaNguoiCongBo(null);
        ketQua.setCongBoLuc(null);

        return ketQuaRepository.saveAndFlush(ketQua);
    }

    // Trưởng khoa công bố kết quả; ba bảng được cập nhật trong một transaction.
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public KetQua congBo(String maNhom) {
        if (maNhom == null || maNhom.isBlank()) {
            throw new IllegalArgumentException("Mã nhóm không được để trống.");
        }

        String maNguoiCongBo = layMaNguoiDungDangNhap();

        // Kiểm tra lại tài khoản và vai trò hiện tại trong database.
        if (hoiDongRepository.demTruongKhoaHoatDong(maNguoiCongBo) == 0) {
            throw new AccessDeniedException(
                "Chỉ trưởng khoa đang hoạt động được công bố kết quả."
            );
        }

        HoiDongDeTai phanCong = hoiDongDeTaiRepository.findById(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Nhóm chưa được phân công hội đồng."
            ));

        String maHoiDong = phanCong.getMaHoiDong();

        // Giữ cùng thứ tự khóa với thao tác chấm và tổng hợp.
        hoiDongRepository.timVaKhoaTheoMa(maHoiDong)
            .orElseThrow(() -> new IllegalArgumentException(
                "Hội đồng không còn tồn tại."
            ));

        hoiDongDeTaiRepository.khoaDangKyTheoMaNhom(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Đăng ký đề tài không còn tồn tại."
            ));

        String maHoiDongHienTai = hoiDongDeTaiRepository
            .timMaHoiDongHienTai(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Phân công hội đồng đã thay đổi."
            ));

        if (!maHoiDong.equals(maHoiDongHienTai)) {
            throw new IllegalArgumentException(
                "Hội đồng của nhóm đã thay đổi. Vui lòng tải lại trang."
            );
        }

        String trangThaiDangKy = hoiDongDeTaiRepository
            .timTrangThaiDangKyTheoMaNhom(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Đăng ký đề tài không còn tồn tại."
            ));

        if (!"DANG_CHAM".equals(trangThaiDangKy)) {
            throw new IllegalArgumentException(
                "Chỉ công bố kết quả khi đăng ký đang ở giai đoạn chấm."
            );
        }

        KetQua ketQua = ketQuaRepository.findById(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Nhóm chưa có kết quả tổng hợp."
            ));

        if (ketQua.getTrangThai() != TrangThaiKetQua.DA_TONG_HOP) {
            throw new IllegalArgumentException(
                "Kết quả đã công bố hoặc không còn ở trạng thái tổng hợp."
            );
        }

        // Tính lại dưới khóa để tránh công bố điểm đã lỗi thời.
        KetQuaTinhDiem diemHienTai =
            tinhDiemTrungBinhService.tinhDiem(maNhom);

        if (ketQua.getSoPhieu() != diemHienTai.soPhieu()
                || ketQua.getTongDiem().compareTo(
                    diemHienTai.tongDiem()
                ) != 0
                || ketQua.getDiemCuoi().compareTo(
                    diemHienTai.diemCuoi()
                ) != 0) {
            throw new IllegalArgumentException(
                "Điểm tổng hợp không còn khớp các phiếu chấm."
            );
        }

        LocalDateTime congBoLuc =
            LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        if (congBoLuc.isBefore(ketQua.getTongHopLuc())) {
            throw new IllegalArgumentException(
                "Thời điểm công bố không được trước lúc tổng hợp."
            );
        }

        // Mỗi UPDATE phải đổi đúng một dòng; lỗi sẽ rollback cả transaction.
        if (hoiDongDeTaiRepository.hoanThanhDangKyKhiDangCham(
                maNhom, congBoLuc) != 1) {
            throw new IllegalArgumentException(
                "Không thể hoàn thành đăng ký của nhóm."
            );
        }

        if (hoiDongDeTaiRepository.hoanThanhNhomKhiDangThucHien(
                maNhom, congBoLuc) != 1) {
            throw new IllegalArgumentException(
                "Không thể hoàn thành nhóm sinh viên."
            );
        }

        ketQua.setTrangThai(TrangThaiKetQua.CONG_BO);
        ketQua.setMaNguoiCongBo(maNguoiCongBo);
        ketQua.setCongBoLuc(congBoLuc);

        return ketQuaRepository.saveAndFlush(ketQua);
    }

    private String layMaNguoiDungDangNhap() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException(
                "Bạn cần đăng nhập."
            );
        }

        return authentication.getName();
    }
}