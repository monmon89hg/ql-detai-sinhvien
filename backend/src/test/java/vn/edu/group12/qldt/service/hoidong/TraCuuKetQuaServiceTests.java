package vn.edu.group12.qldt.service.hoidong;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import static vn.edu.group12.qldt.support.SecurityTestUsers.authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import vn.edu.group12.qldt.repository.hoidong.KetQuaRepository;

@ExtendWith(MockitoExtension.class)
class TraCuuKetQuaServiceTests {

    @Mock
    private KetQuaRepository ketQuaRepository;

    @InjectMocks
    private TraCuuKetQuaService traCuuKetQuaService;

    @BeforeEach
    void dangNhapSinhVien() {
        SecurityContextHolder.getContext().setAuthentication(
            authentication("DEMO_SV26", "SINH_VIEN")
        );
    }

    @AfterEach
    void xoaPhienGiaLap() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void traCuuDungMaSinhVienTuPhienDangNhap() {
        traCuuKetQuaService.traCuuTheoDot(" DEMO_DOT_KLH26 ");

        verify(ketQuaRepository).traCuuKetQuaCuaSinhVien(
            "DEMO_SV26", "DEMO_DOT_KLH26"
        );
    }

    @Test
    void danhSachDotDungMaSinhVienTuPhienDangNhap() {
        traCuuKetQuaService.layDotCuaToi();

        verify(ketQuaRepository).layDotCoKetQuaCuaSinhVien("DEMO_SV26");
    }

    @Test
    void khongDangNhapThiKhongDuocTraCuu() {
        SecurityContextHolder.clearContext();

        assertThrows(
            AccessDeniedException.class,
            () -> traCuuKetQuaService.traCuuTheoDot("DEMO_DOT_KLH26")
        );

        verifyNoInteractions(ketQuaRepository);
    }
}