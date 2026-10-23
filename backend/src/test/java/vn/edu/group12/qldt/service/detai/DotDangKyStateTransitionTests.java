package vn.edu.group12.qldt.service.detai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.edu.group12.qldt.dto.detai.DotDangKyDto;
import vn.edu.group12.qldt.entity.detai.DotDangKy;
import vn.edu.group12.qldt.repository.detai.DotDangKyRepository;

@ExtendWith(MockitoExtension.class)
class DotDangKyStateTransitionTests {

    @Mock
    private DotDangKyRepository dotDangKyRepository;

    @InjectMocks
    private DotDangKyService dotDangKyService;

    @Test
    void congBoDot_ThanhCong_KhiDangNhap() {
        DotDangKy dot = new DotDangKy();
        dot.setMaDot("DOT01");
        dot.setTrangThai("NHAP");

        when(dotDangKyRepository.findById("DOT01")).thenReturn(Optional.of(dot));
        when(dotDangKyRepository.save(any(DotDangKy.class))).thenAnswer(i -> i.getArgument(0));

        dotDangKyService.congBoDot("DOT01");

        assertEquals("CONG_BO", dot.getTrangThai());
        verify(dotDangKyRepository).save(dot);
    }

    @Test
    void congBoDot_NemLoi_KhiKhongPhaiNhap() {
        DotDangKy dot = new DotDangKy();
        dot.setMaDot("DOT01");
        dot.setTrangThai("DONG");

        when(dotDangKyRepository.findById("DOT01")).thenReturn(Optional.of(dot));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dotDangKyService.congBoDot("DOT01"));

        assertTrue(ex.getMessage().contains("Chỉ có thể công bố đợt đăng ký khi đang ở trạng thái 'NHAP'"));
        verify(dotDangKyRepository, never()).save(any());
    }

    @Test
    void dongDot_ThanhCong_KhiDangCongBo() {
        DotDangKy dot = new DotDangKy();
        dot.setMaDot("DOT01");
        dot.setTrangThai("CONG_BO");

        when(dotDangKyRepository.findById("DOT01")).thenReturn(Optional.of(dot));
        when(dotDangKyRepository.save(any(DotDangKy.class))).thenAnswer(i -> i.getArgument(0));

        dotDangKyService.dongDot("DOT01");

        assertEquals("DONG", dot.getTrangThai());
        verify(dotDangKyRepository).save(dot);
    }

    @Test
    void dongDot_NemLoi_KhiKhongPhaiCongBo() {
        DotDangKy dot = new DotDangKy();
        dot.setMaDot("DOT01");
        dot.setTrangThai("NHAP");

        when(dotDangKyRepository.findById("DOT01")).thenReturn(Optional.of(dot));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dotDangKyService.dongDot("DOT01"));

        assertTrue(ex.getMessage().contains("Chỉ có thể đóng đợt đăng ký khi đang ở trạng thái 'CONG_BO'"));
        verify(dotDangKyRepository, never()).save(any());
    }

    @Test
    void updateDotDangKy_NemLoi_KhiDotDaDong() {
        DotDangKy dot = new DotDangKy();
        dot.setMaDot("DOT01");
        dot.setTrangThai("DONG");

        DotDangKyDto dto = new DotDangKyDto();
        dto.setMaDot("DOT01");
        dto.setTenDot("Cập nhật đợt");
        dto.setBatDauGv(LocalDateTime.now().plusDays(1));
        dto.setKetThucGv(LocalDateTime.now().plusDays(5));
        dto.setBatDauSv(LocalDateTime.now().plusDays(6));
        dto.setKetThucSv(LocalDateTime.now().plusDays(10));
        dto.setLoaiDeTai("MON_HOC");

        when(dotDangKyRepository.findById("DOT01")).thenReturn(Optional.of(dot));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dotDangKyService.updateDotDangKy("DOT01", dto));

        assertTrue(ex.getMessage().contains("Không thể chỉnh sửa đợt đăng ký đã đóng"));
        verify(dotDangKyRepository, never()).save(any());
    }
}
