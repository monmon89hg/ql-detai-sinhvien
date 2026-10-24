package vn.edu.group12.qldt.service.detai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.group12.qldt.entity.detai.DotDangKy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dịch vụ tạo file lịch iCalendar (.ics) cho đợt đăng ký đề tài.
 * Phục vụ tính năng sáng tạo: Xuất lịch hạn nộp và các mốc thời gian vào Google Calendar / Outlook / Apple Calendar.
 */
@Service
public class LichIcsService {

    @Autowired
    private DotDangKyService dotDangKyService;

    private static final DateTimeFormatter ICS_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter ICS_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String taoLichIcsChoDot(String maDot) {
        DotDangKy dot = dotDangKyService.getDotDangKyByMa(maDot);
        return taoLichIcs(dot);
    }

    public String taoLichIcs(DotDangKy dot) {
        StringBuilder sb = new StringBuilder();
        String crlf = "\r\n";
        String nowStamp = LocalDateTime.now().format(ICS_DATE_TIME) + "Z";

        sb.append("BEGIN:VCALENDAR").append(crlf);
        sb.append("PRODID:-//QLDT Sinh Vien - Nhom 12//VN").append(crlf);
        sb.append("VERSION:2.0").append(crlf);
        sb.append("CALSCALE:GREGORIAN").append(crlf);
        sb.append("METHOD:PUBLISH").append(crlf);
        sb.append("X-WR-CALNAME:Lich Dot ").append(escapeText(dot.getTenDot())).append(crlf);
        sb.append("X-WR-TIMEZONE:Asia/Ho_Chi_Minh").append(crlf);

        // 1. Giai đoạn 1: Giảng viên đề xuất đề tài
        if (dot.getBatDauGv() != null && dot.getKetThucGv() != null) {
            themSuKien(sb, crlf, nowStamp,
                    "gv-" + dot.getMaDot() + "@qldt.edu.vn",
                    dot.getBatDauGv(), dot.getKetThucGv(),
                    "[QLDT] Giai đoạn 1: GV đề xuất đề tài (" + dot.getTenDot() + ")",
                    "Thời gian giảng viên nộp đề xuất đề tài cho đợt đăng ký: " + dot.getTenDot() + ". Mã đợt: " + dot.getMaDot());
        }

        // 2. Giai đoạn 2: Sinh viên đăng ký đề tài
        if (dot.getBatDauSv() != null && dot.getKetThucSv() != null) {
            themSuKien(sb, crlf, nowStamp,
                    "sv-" + dot.getMaDot() + "@qldt.edu.vn",
                    dot.getBatDauSv(), dot.getKetThucSv(),
                    "[QLDT] Giai đoạn 2: SV đăng ký đề tài (" + dot.getTenDot() + ")",
                    "Thời gian mở cổng đăng ký đề tài chính thức cho sinh viên. Mã đợt: " + dot.getMaDot());
        }

        // 3. Hạn nộp báo cáo đề tài
        if (dot.getHanNopBaoCao() != null) {
            LocalDateTime startBaoCao = dot.getHanNopBaoCao().minusHours(2);
            themSuKien(sb, crlf, nowStamp,
                    "baocao-" + dot.getMaDot() + "@qldt.edu.vn",
                    startBaoCao, dot.getHanNopBaoCao(),
                    "[QLDT NHẮC HẸN] Hạn chót nộp báo cáo (" + dot.getTenDot() + ")",
                    "Hạn chót sinh viên nộp báo cáo (PDF) cho đề tài thuộc đợt: " + dot.getTenDot());
        }

        // 4. Hạn nộp điểm phản biện (nếu có)
        if (dot.getHanNopDiemPb() != null) {
            LocalDateTime startPb = dot.getHanNopDiemPb().minusHours(2);
            themSuKien(sb, crlf, nowStamp,
                    "diempb-" + dot.getMaDot() + "@qldt.edu.vn",
                    startPb, dot.getHanNopDiemPb(),
                    "[QLDT] Hạn nộp điểm phản biện (" + dot.getTenDot() + ")",
                    "Hạn giảng viên phản biện nộp phiếu chấm điểm cho đợt: " + dot.getTenDot());
        }

        // 5. Ngày báo cáo hội đồng (nếu có)
        if (dot.getNgayBaoCaoHd() != null) {
            LocalDate ngayHd = dot.getNgayBaoCaoHd();
            sb.append("BEGIN:VEVENT").append(crlf);
            sb.append("UID:hoidong-").append(dot.getMaDot()).append("@qldt.edu.vn").append(crlf);
            sb.append("DTSTAMP:").append(nowStamp).append(crlf);
            sb.append("DTSTART;VALUE=DATE:").append(ngayHd.format(ICS_DATE)).append(crlf);
            sb.append("DTEND;VALUE=DATE:").append(ngayHd.plusDays(1).format(ICS_DATE)).append(crlf);
            sb.append("SUMMARY:").append(escapeText("[QLDT] Ngày báo cáo Hội đồng (" + dot.getTenDot() + ")")).append(crlf);
            sb.append("DESCRIPTION:").append(escapeText("Buổi lễ báo cáo và bảo vệ đề tài trước Hội đồng đánh giá.")).append(crlf);
            sb.append("STATUS:CONFIRMED").append(crlf);
            sb.append("END:VEVENT").append(crlf);
        }

        sb.append("END:VCALENDAR").append(crlf);
        return sb.toString();
    }

    private void themSuKien(StringBuilder sb, String crlf, String nowStamp,
                            String uid, LocalDateTime start, LocalDateTime end,
                            String summary, String description) {
        sb.append("BEGIN:VEVENT").append(crlf);
        sb.append("UID:").append(uid).append(crlf);
        sb.append("DTSTAMP:").append(nowStamp).append(crlf);
        sb.append("DTSTART:").append(start.format(ICS_DATE_TIME)).append(crlf);
        sb.append("DTEND:").append(end.format(ICS_DATE_TIME)).append(crlf);
        sb.append("SUMMARY:").append(escapeText(summary)).append(crlf);
        sb.append("DESCRIPTION:").append(escapeText(description)).append(crlf);
        sb.append("STATUS:CONFIRMED").append(crlf);
        sb.append("END:VEVENT").append(crlf);
    }

    private String escapeText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(";", "\\;")
                   .replace(",", "\\,")
                   .replace("\n", "\\n");
    }
}
