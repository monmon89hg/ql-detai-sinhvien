package vn.edu.group12.qldt.service.detai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.edu.group12.qldt.entity.detai.DeTai;
import vn.edu.group12.qldt.entity.detai.DotDangKy;
import vn.edu.group12.qldt.repository.detai.DeTaiRepository;

@ExtendWith(MockitoExtension.class)
class DeTaiStateTransitionTests {

    @Mock
    private DeTaiRepository deTaiRepository;

    @Mock
    private DotDangKyService dotDangKyService;

    @Mock
    private DeTaiGiangVienService deTaiGiangVienService;

    @InjectMocks
    private DeTaiService deTaiService;

    @Test
    void duyetDeTai_ThanhCong_KhiDangChoDuyet() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setTrangThai("CHO_DUYET");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));
        when(deTaiRepository.save(any(DeTai.class))).thenAnswer(i -> i.getArgument(0));

        DeTai ketQua = deTaiService.duyetDeTai("DT01", "TK01");

        assertEquals("DA_DUYET", ketQua.getTrangThai());
        assertEquals("TK01", ketQua.getMaNguoiDuyet());
        assertNotNull(ketQua.getDuyetLuc());
        assertNull(ketQua.getLyDoTuChoi());
    }

    @Test
    void duyetDeTai_NemLoi_KhiKhongPhaiChoDuyet() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setTrangThai("TU_CHOI");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> deTaiService.duyetDeTai("DT01", "TK01"));

        assertTrue(ex.getMessage().contains("Chỉ có thể duyệt đề tài khi đang ở trạng thái 'CHO_DUYET'"));
        verify(deTaiRepository, never()).save(any());
    }

    @Test
    void tuChoiDeTai_ThanhCong_KhiDangChoDuyet() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setTrangThai("CHO_DUYET");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));
        when(deTaiRepository.save(any(DeTai.class))).thenAnswer(i -> i.getArgument(0));

        DeTai ketQua = deTaiService.tuChoiDeTai("DT01", "Chưa đạt yêu cầu", "TK01");

        assertEquals("TU_CHOI", ketQua.getTrangThai());
        assertEquals("Chưa đạt yêu cầu", ketQua.getLyDoTuChoi());
        assertEquals("TK01", ketQua.getMaNguoiDuyet());
    }

    @Test
    void tuChoiDeTai_NemLoi_KhiKhongPhaiChoDuyet() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setTrangThai("CONG_BO");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> deTaiService.tuChoiDeTai("DT01", "Lý do từ chối", "TK01"));

        assertTrue(ex.getMessage().contains("Chỉ có thể từ chối đề tài khi đang ở trạng thái 'CHO_DUYET'"));
        verify(deTaiRepository, never()).save(any());
    }

    @Test
    void congBoDeTai_ThanhCong_KhiDaDuyetVaDuGvhd() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setMaDot("DOT01");
        deTai.setTrangThai("DA_DUYET");

        DotDangKy dot = new DotDangKy();
        dot.setMaDot("DOT01");
        dot.setTrangThai("CONG_BO");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));
        when(dotDangKyService.getDotDangKyByMa("DOT01")).thenReturn(dot);
        when(deTaiGiangVienService.countGvhd("DT01")).thenReturn(1L);
        when(deTaiRepository.save(any(DeTai.class))).thenAnswer(i -> i.getArgument(0));

        DeTai ketQua = deTaiService.congBoDeTai("DT01");

        assertEquals("CONG_BO", ketQua.getTrangThai());
        assertNotNull(ketQua.getCongBoLuc());
    }

    @Test
    void congBoDeTai_NemLoi_KhiDotDangKyDaDong() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setMaDot("DOT01");
        deTai.setTrangThai("DA_DUYET");

        DotDangKy dot = new DotDangKy();
        dot.setMaDot("DOT01");
        dot.setTrangThai("DONG");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));
        when(dotDangKyService.getDotDangKyByMa("DOT01")).thenReturn(dot);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> deTaiService.congBoDeTai("DT01"));

        assertTrue(ex.getMessage().contains("Không thể công bố đề tài thuộc đợt đăng ký đã đóng"));
        verify(deTaiRepository, never()).save(any());
    }

    @Test
    void congBoDeTai_NemLoi_KhiChuaDuyet() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setTrangThai("CHO_DUYET");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> deTaiService.congBoDeTai("DT01"));

        assertTrue(ex.getMessage().contains("Chỉ được công bố đề tài đã ở trạng thái 'DA_DUYET'"));
        verify(deTaiRepository, never()).save(any());
    }

    @Test
    void capNhatDeTai_ThanhCong_BoiNguoiDeXuat() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setMaNguoiDeXuat("GV01");
        deTai.setTrangThai("CHO_DUYET");
        deTai.setTenDeTai("Tên cũ");

        vn.edu.group12.qldt.dto.detai.DeTaiDto dto = new vn.edu.group12.qldt.dto.detai.DeTaiDto();
        dto.setTenDeTai("Tên mới");
        dto.setNoiDung("Nội dung mới");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));
        when(deTaiRepository.countDangKyByMaDeTai("DT01")).thenReturn(0L);
        when(deTaiRepository.save(any(DeTai.class))).thenAnswer(i -> i.getArgument(0));

        DeTai ketQua = deTaiService.capNhatDeTai("DT01", dto, "GV01");

        assertEquals("Tên mới", ketQua.getTenDeTai());
        assertEquals("Nội dung mới", ketQua.getNoiDung());
        verify(deTaiRepository).save(deTai);
    }

    @Test
    void capNhatDeTai_ChuyenVeChoDuyet_KhiDeTaiDangBiTuChoi() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setMaNguoiDeXuat("GV01");
        deTai.setTrangThai("TU_CHOI");
        deTai.setLyDoTuChoi("Lý do từ chối cũ");

        vn.edu.group12.qldt.dto.detai.DeTaiDto dto = new vn.edu.group12.qldt.dto.detai.DeTaiDto();
        dto.setTenDeTai("Tên chỉnh sửa sau từ chối");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));
        when(deTaiRepository.countDangKyByMaDeTai("DT01")).thenReturn(0L);
        when(deTaiRepository.save(any(DeTai.class))).thenAnswer(i -> i.getArgument(0));

        DeTai ketQua = deTaiService.capNhatDeTai("DT01", dto, "GV01");

        assertEquals("CHO_DUYET", ketQua.getTrangThai());
        assertNull(ketQua.getLyDoTuChoi());
    }

    @Test
    void capNhatDeTai_CapNhatTrangThaiThanhCong_KhiChonTrangThaiMoi() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setMaNguoiDeXuat("GV01");
        deTai.setTrangThai("CHO_DUYET");

        vn.edu.group12.qldt.dto.detai.DeTaiDto dto = new vn.edu.group12.qldt.dto.detai.DeTaiDto();
        dto.setTrangThai("NGUNG_NHAN");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));
        when(deTaiRepository.countDangKyByMaDeTai("DT01")).thenReturn(0L);
        when(deTaiRepository.save(any(DeTai.class))).thenAnswer(i -> i.getArgument(0));

        DeTai ketQua = deTaiService.capNhatDeTai("DT01", dto, "GV01");

        assertEquals("NGUNG_NHAN", ketQua.getTrangThai());
        verify(deTaiRepository).save(deTai);
    }

    @Test
    void capNhatDeTai_NemLoi_KhiKhongPhaiNguoiDeXuat() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setMaNguoiDeXuat("GV01");
        deTai.setTrangThai("CHO_DUYET");

        vn.edu.group12.qldt.dto.detai.DeTaiDto dto = new vn.edu.group12.qldt.dto.detai.DeTaiDto();

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> deTaiService.capNhatDeTai("DT01", dto, "GV_KHAC"));
        verify(deTaiRepository, never()).save(any());
    }

    @Test
    void ngungNhanDeTai_ThanhCong() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setMaNguoiDeXuat("GV01");
        deTai.setTrangThai("CONG_BO");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));
        when(deTaiRepository.save(any(DeTai.class))).thenAnswer(i -> i.getArgument(0));

        DeTai ketQua = deTaiService.ngungNhanDeTai("DT01", "GV01");

        assertEquals("NGUNG_NHAN", ketQua.getTrangThai());
        verify(deTaiRepository).save(deTai);
    }

    @Test
    void xoaHoacHuyDeTai_ChuyenNgungNhanThayViXoaVatLy_KhiDaCoDangKy() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setMaNguoiDeXuat("GV01");
        deTai.setTrangThai("CONG_BO");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));
        when(deTaiRepository.countDangKyByMaDeTai("DT01")).thenReturn(2L);
        when(deTaiRepository.save(any(DeTai.class))).thenAnswer(i -> i.getArgument(0));

        String thongBao = deTaiService.xoaHoacHuyDeTai("DT01", "GV01");

        assertEquals("NGUNG_NHAN", deTai.getTrangThai());
        assertTrue(thongBao.contains("NGỪNG NHẬN thay vì xóa vật lý"));
        verify(deTaiRepository).save(deTai);
        verify(deTaiRepository, never()).delete(any());
    }

    @Test
    void xoaHoacHuyDeTai_XoaVatLyAnToan_KhiChuaCoDangKy() {
        DeTai deTai = new DeTai();
        deTai.setMaDeTai("DT01");
        deTai.setMaNguoiDeXuat("GV01");
        deTai.setTrangThai("CHO_DUYET");

        when(deTaiRepository.findById("DT01")).thenReturn(Optional.of(deTai));
        when(deTaiRepository.countDangKyByMaDeTai("DT01")).thenReturn(0L);

        String thongBao = deTaiService.xoaHoacHuyDeTai("DT01", "GV01");

        assertEquals("Đã xóa đề tài thành công!", thongBao);
        verify(deTaiGiangVienService).xoaTatCaPhanCongCuaDeTai("DT01");
        verify(deTaiRepository).delete(deTai);
    }
}
