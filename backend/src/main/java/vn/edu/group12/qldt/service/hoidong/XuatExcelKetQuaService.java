package vn.edu.group12.qldt.service.hoidong;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import vn.edu.group12.qldt.dto.hoidong.NhomKetQuaHienThi;

@Service
public class XuatExcelKetQuaService {

    private static final String[] COT = {
        "Nhóm", "Đề tài", "Hội đồng", "Trạng thái", "Điểm"
    };

    public byte[] xuat(List<NhomKetQuaHienThi> danhSachNhom, boolean trangQuanLy)
            throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Kết quả");
            sheet.createFreezePane(0, 1);

            CellStyle tieuDeStyle = workbook.createCellStyle();
            tieuDeStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            tieuDeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font tieuDeFont = workbook.createFont();
            tieuDeFont.setBold(true);
            tieuDeFont.setColor(IndexedColors.WHITE.getIndex());
            tieuDeStyle.setFont(tieuDeFont);

            CellStyle noiDungStyle = workbook.createCellStyle();
            noiDungStyle.setWrapText(true);

            CellStyle diemStyle = workbook.createCellStyle();
            diemStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

            Row tieuDe = sheet.createRow(0);
            for (int i = 0; i < COT.length; i++) {
                var o = tieuDe.createCell(i);
                o.setCellValue(COT[i]);
                o.setCellStyle(tieuDeStyle);
            }

            int soDong = 1;
            for (NhomKetQuaHienThi nhom : danhSachNhom) {
                Row dong = sheet.createRow(soDong++);
                dong.setHeightInPoints(34);

                ghiMaVaTen(dong, 0, nhom.getMaNhom(), nhom.getTenNhom(), noiDungStyle);
                ghiMaVaTen(dong, 1, nhom.getMaDeTai(), nhom.getTenDeTai(), noiDungStyle);
                ghiMaVaTen(dong, 2, nhom.getMaHoiDong(), nhom.getTenHoiDong(), noiDungStyle);
                dong.createCell(3).setCellValue(
                    hienThiTrangThai(nhom.getTrangThaiKetQua(), trangQuanLy)
                );

                BigDecimal diem = nhom.getDiemCuoi();
                if (diem != null) {
                    var oDiem = dong.createCell(4);
                    oDiem.setCellValue(diem.doubleValue());
                    oDiem.setCellStyle(diemStyle);
                }
            }

            int[] doRong = {24, 42, 35, 20, 12};
            for (int i = 0; i < doRong.length; i++) {
                sheet.setColumnWidth(i, doRong[i] * 256);
            }

            workbook.write(output);
            return output.toByteArray();
        }
    }

    private void ghiMaVaTen(Row dong, int cot, String ma, String ten, CellStyle style) {
        String giaTri = ma == null ? "" : ma;
        if (ten != null && !ten.isBlank()) {
            giaTri += "\n" + ten;
        }
        if (giaTri.isBlank()) giaTri = "—";
        var o = dong.createCell(cot);
        o.setCellValue(giaTri);
        o.setCellStyle(style);
    }

    private String hienThiTrangThai(String trangThai, boolean trangQuanLy) {
        if (trangThai == null) {
            return "Chưa tổng hợp";
        }
        return switch (trangThai) {
            case "DA_TONG_HOP" -> trangQuanLy ? "Chờ công bố" : "Đã tổng hợp";
            case "CONG_BO" -> "Đã công bố";
            default -> trangThai;
        };
    }
}
