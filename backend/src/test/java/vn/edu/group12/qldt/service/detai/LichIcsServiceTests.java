package vn.edu.group12.qldt.service.detai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.edu.group12.qldt.entity.detai.DotDangKy;

@ExtendWith(MockitoExtension.class)
class LichIcsServiceTests {

    @Mock
    private DotDangKyService dotDangKyService;

    @InjectMocks
    private LichIcsService lichIcsService;

    @Test
    void taoLichIcs_DayDuCacSuKien() {
        DotDangKy dot = new DotDangKy();
        dot.setMaDot("DOT_TEST_2026");
        dot.setTenDot("Khóa luận tốt nghiệp HK1 2026");
        dot.setLoaiDeTai("KLTN");
        dot.setBatDauGv(LocalDateTime.of(2026, 10, 1, 8, 0));
        dot.setKetThucGv(LocalDateTime.of(2026, 10, 15, 17, 0));
        dot.setBatDauSv(LocalDateTime.of(2026, 10, 16, 8, 0));
        dot.setKetThucSv(LocalDateTime.of(2026, 10, 31, 17, 0));
        dot.setHanNopBaoCao(LocalDateTime.of(2026, 12, 1, 23, 59));
        dot.setHanNopDiemPb(LocalDateTime.of(2026, 12, 10, 17, 0));
        dot.setNgayBaoCaoHd(LocalDate.of(2026, 12, 20));

        when(dotDangKyService.getDotDangKyByMa("DOT_TEST_2026")).thenReturn(dot);

        String ics = lichIcsService.taoLichIcsChoDot("DOT_TEST_2026");

        assertNotNull(ics);
        assertTrue(ics.startsWith("BEGIN:VCALENDAR"));
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"));

        // Kiểm tra sự kiện Giai đoạn 1 (GV)
        assertTrue(ics.contains("SUMMARY:[QLDT] Giai đoạn 1: GV đề xuất đề tài"));
        assertTrue(ics.contains("DTSTART:20261001T080000"));
        assertTrue(ics.contains("DTEND:20261015T170000"));

        // Kiểm tra sự kiện Giai đoạn 2 (SV)
        assertTrue(ics.contains("SUMMARY:[QLDT] Giai đoạn 2: SV đăng ký đề tài"));
        assertTrue(ics.contains("DTSTART:20261016T080000"));
        assertTrue(ics.contains("DTEND:20261031T170000"));

        // Kiểm tra sự kiện Hạn nộp báo cáo
        assertTrue(ics.contains("SUMMARY:[QLDT NHẮC HẸN] Hạn chót nộp báo cáo"));
        assertTrue(ics.contains("DTEND:20261201T235900"));

        // Kiểm tra sự kiện Hạn nộp điểm phản biện
        assertTrue(ics.contains("SUMMARY:[QLDT] Hạn nộp điểm phản biện"));

        // Kiểm tra sự kiện Ngày báo cáo Hội đồng
        assertTrue(ics.contains("SUMMARY:[QLDT] Ngày báo cáo Hội đồng"));
        assertTrue(ics.contains("DTSTART;VALUE=DATE:20261220"));
    }

    @Test
    void taoLichIcs_MonHoc_KhongCoPhanBienVaHoiDong() {
        DotDangKy dot = new DotDangKy();
        dot.setMaDot("DOT_MH_01");
        dot.setTenDot("Môn học chuyên ngành");
        dot.setLoaiDeTai("MON_HOC");
        dot.setBatDauGv(LocalDateTime.of(2026, 9, 1, 8, 0));
        dot.setKetThucGv(LocalDateTime.of(2026, 9, 10, 17, 0));
        dot.setBatDauSv(LocalDateTime.of(2026, 9, 11, 8, 0));
        dot.setKetThucSv(LocalDateTime.of(2026, 9, 20, 17, 0));
        // Không có hanNopDiemPb và ngayBaoCaoHd

        when(dotDangKyService.getDotDangKyByMa("DOT_MH_01")).thenReturn(dot);

        String ics = lichIcsService.taoLichIcsChoDot("DOT_MH_01");

        assertNotNull(ics);
        assertTrue(ics.contains("SUMMARY:[QLDT] Giai đoạn 1: GV đề xuất đề tài"));
        assertTrue(ics.contains("SUMMARY:[QLDT] Giai đoạn 2: SV đăng ký đề tài"));
        assertFalse(ics.contains("Hạn nộp điểm phản biện"));
        assertFalse(ics.contains("Ngày báo cáo Hội đồng"));
    }
}
