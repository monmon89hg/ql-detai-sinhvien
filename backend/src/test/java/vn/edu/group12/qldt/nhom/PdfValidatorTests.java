package vn.edu.group12.qldt.nhom;

import static org.assertj.core.api.Assertions.*;

import static vn.edu.group12.qldt.nhom.NhomTestSupport.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockMultipartFile;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.service.nhom.PdfValidator;

import java.util.stream.Stream;

class PdfValidatorTests {
    final PdfValidator validator = new PdfValidator();

    static Stream<MockMultipartFile> sai() {
        return Stream.of(
                file(new byte[0]),
                file("Không phải PDF".getBytes()),
                file("%PDF-1.7\nkhông có cấu trúc".getBytes()),
                file(new byte[PdfValidator.MAX_BYTES + 1]),
                new MockMultipartFile("tepBaoCao", "a.pdf", "text/plain", pdf("a")),
                new MockMultipartFile("tepBaoCao", "../a.pdf", "application/pdf", pdf("a")),
                new MockMultipartFile("tepBaoCao", "a.txt", "application/pdf", pdf("a")));
    }

    @ParameterizedTest
    @MethodSource("sai")
    void tuChoiTepKhongHopLe(MockMultipartFile file) {
        assertThatThrownBy(() -> validator.kiemTra(file)).isInstanceOf(NghiepVuNhomException.class);
    }

    @Test
    void chapNhanPdfThatVaTinhChecksum() {
        byte[] bytes = pdf("hợp lệ");
        var result = validator.kiemTra(file(bytes));
        assertThat(result.bytes()).isEqualTo(bytes);
        assertThat(result.sha256()).isEqualTo(PdfValidator.sha256(bytes));
    }

    @Test
    void gioiHanDocKhongTinKichThuocClient() {
        var oversized =
                new MockMultipartFile(
                        "tepBaoCao",
                        "a.pdf",
                        "application/pdf",
                        new byte[PdfValidator.MAX_BYTES + 1]) {
                    @Override
                    public long getSize() {
                        return 5;
                    }
                };
        assertThatThrownBy(() -> validator.kiemTra(oversized)).isInstanceOf(NghiepVuNhomException.class);
    }
}
