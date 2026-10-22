package vn.edu.group12.qldt.service.nhom;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Locale;

@Component
public class PdfValidator {
    public static final int MAX_BYTES = 20 * 1024 * 1024;

    public record Pdf(String tenFile, byte[] bytes, String sha256) {}

    public Pdf kiemTra(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES)
            throw NghiepVuNhomException.duLieu("Tệp PDF phải có dung lượng từ 1 byte đến 20 MiB.");
        String ten =
                Normalizer.normalize(
                        file.getOriginalFilename() == null ? "" : file.getOriginalFilename(),
                        Normalizer.Form.NFC);
        if (ten.isBlank()
                || ten.length() > 255
                || ten.contains("/")
                || ten.contains("\\")
                || ten.codePoints().anyMatch(Character::isISOControl)
                || !ten.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw NghiepVuNhomException.duLieu(
                    "Tên tệp PDF không hợp lệ; tối đa 255 ký tự và không chứa đường dẫn.");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()))
            throw NghiepVuNhomException.duLieu("Tệp phải có kiểu nội dung application/pdf.");
        byte[] bytes;
        try (var in = file.getInputStream()) {
            bytes = in.readNBytes(MAX_BYTES + 1);
        } catch (IOException ex) {
            throw new NghiepVuNhomException(
                    NghiepVuNhomException.Loai.DU_LIEU, "Không đọc được tệp PDF vừa gửi.", ex);
        }
        if (bytes.length == 0 || bytes.length > MAX_BYTES)
            throw NghiepVuNhomException.duLieu("Tệp PDF phải có dung lượng từ 1 byte đến 20 MiB.");
        if (bytes.length < 5 || !new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-"))
            throw NghiepVuNhomException.duLieu("Nội dung tệp không phải PDF hợp lệ.");
        try (var source = new RandomAccessReadBuffer(bytes)) {
            var parser = new PDFParser(source);
            try (var doc = parser.parse(false)) {
                if (doc.isEncrypted() || doc.getNumberOfPages() < 1)
                    throw NghiepVuNhomException.duLieu(
                            "PDF phải có ít nhất một trang và không được mã hóa.");
                doc.getPages().forEach(page -> page.getCOSObject());
            }
        } catch (IOException | IllegalArgumentException ex) {
            throw new NghiepVuNhomException(
                    NghiepVuNhomException.Loai.DU_LIEU, "Nội dung hoặc cấu trúc PDF không hợp lệ.", ex);
        }
        return new Pdf(ten, bytes, sha256(bytes));
    }

    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Máy chủ không hỗ trợ SHA-256.", ex);
        }
    }
}
