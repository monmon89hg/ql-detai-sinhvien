package vn.edu.group12.qldt.service.hoidong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import vn.edu.group12.qldt.dto.hoidong.NhomKetQuaHienThi;

class XuatExcelKetQuaServiceTests {

    private final XuatExcelKetQuaService service = new XuatExcelKetQuaService();

    @Test
    void twoPagesHaveSameFiveColumnsAndTheirOwnStatusLabels() throws Exception {
        NhomKetQuaHienThi nhom = mock(NhomKetQuaHienThi.class);
        when(nhom.getMaNhom()).thenReturn("N01");
        when(nhom.getTenNhom()).thenReturn("Nhóm một");
        when(nhom.getMaDeTai()).thenReturn("DT01");
        when(nhom.getTenDeTai()).thenReturn("Đề tài một");
        when(nhom.getMaHoiDong()).thenReturn("HD01");
        when(nhom.getTenHoiDong()).thenReturn("Hội đồng một");
        when(nhom.getTrangThaiKetQua()).thenReturn("DA_TONG_HOP");
        when(nhom.getDiemCuoi()).thenReturn(new BigDecimal("8.25"));

        for (boolean trangQuanLy : List.of(false, true)) {
            byte[] file = service.xuat(List.of(nhom), trangQuanLy);
            try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
                var sheet = workbook.getSheetAt(0);
                assertThat(sheet.getRow(0).getPhysicalNumberOfCells()).isEqualTo(5);
                assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Nhóm");
                assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Đề tài");
                assertThat(sheet.getRow(0).getCell(2).getStringCellValue()).isEqualTo("Hội đồng");
                assertThat(sheet.getRow(0).getCell(3).getStringCellValue()).isEqualTo("Trạng thái");
                assertThat(sheet.getRow(0).getCell(4).getStringCellValue()).isEqualTo("Điểm");
                assertThat(sheet.getRow(1).getCell(0).getStringCellValue())
                    .isEqualTo("N01\nNhóm một");
                assertThat(sheet.getRow(1).getCell(1).getStringCellValue())
                    .isEqualTo("DT01\nĐề tài một");
                assertThat(sheet.getRow(1).getCell(2).getStringCellValue())
                    .isEqualTo("HD01\nHội đồng một");
                assertThat(sheet.getRow(1).getCell(3).getStringCellValue())
                    .isEqualTo(trangQuanLy ? "Chờ công bố" : "Đã tổng hợp");
                assertThat(sheet.getRow(1).getCell(4).getCellType())
                    .isEqualTo(CellType.NUMERIC);
                assertThat(sheet.getRow(1).getCell(4).getNumericCellValue())
                    .isEqualTo(8.25);
            }
        }
    }

    @Test
    void pendingRowsKeepScoreBlankAndEmptyListsStillHaveHeaders() throws Exception {
        NhomKetQuaHienThi nhom = mock(NhomKetQuaHienThi.class);
        when(nhom.getMaNhom()).thenReturn("N02");

        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(service.xuat(List.of(nhom), true)))) {
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(1).getCell(3).getStringCellValue())
                .isEqualTo("Chưa tổng hợp");
            assertThat(sheet.getRow(1).getCell(4)).isNull();
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(service.xuat(List.of(), false)))) {
            assertThat(workbook.getSheetAt(0).getLastRowNum()).isZero();
        }
    }
}
