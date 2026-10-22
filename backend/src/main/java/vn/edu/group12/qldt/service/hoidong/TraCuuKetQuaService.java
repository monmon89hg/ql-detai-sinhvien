package vn.edu.group12.qldt.service.hoidong;

import vn.edu.group12.qldt.service.auth.CurrentUser;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.dto.hoidong.DotDangKyLuaChon;
import vn.edu.group12.qldt.dto.hoidong.KetQuaTraCuuHienThi;
import vn.edu.group12.qldt.repository.hoidong.KetQuaRepository;

@Service
@PreAuthorize("hasRole('SINH_VIEN')")
@Transactional(readOnly = true)
public class TraCuuKetQuaService {

    private final KetQuaRepository ketQuaRepository;

    public TraCuuKetQuaService(KetQuaRepository ketQuaRepository) {
        this.ketQuaRepository = ketQuaRepository;
    }

    // Chỉ trả về đợt có kết quả đã công bố của sinh viên đang đăng nhập.
    public List<DotDangKyLuaChon> layDotCuaToi() {
        return ketQuaRepository.layDotCoKetQuaCuaSinhVien(
                layMaSinhVienDangNhap());
    }

    // Mã đợt là bộ lọc; mã sinh viên luôn lấy từ phiên đăng nhập.
    public List<KetQuaTraCuuHienThi> traCuuTheoDot(String maDot) {
        String maSinhVien = layMaSinhVienDangNhap();

        if (maDot == null || maDot.isBlank()) {
            return List.of();
        }

        return ketQuaRepository.traCuuKetQuaCuaSinhVien(
                maSinhVien, maDot.strip());
    }

    private String layMaSinhVienDangNhap() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Bạn cần đăng nhập để xem kết quả.");
        }

        return CurrentUser.maNguoiDung();
    }
}