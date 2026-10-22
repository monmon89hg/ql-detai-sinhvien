package vn.edu.group12.qldt.storage.nhom;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;

import java.io.IOException;

public class R2ReportStorage implements ReportStorageService {
    private final S3Client s3;
    private final String bucket;

    public R2ReportStorage(S3Client s3, String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public void put(String key, byte[] bytes) {
        ReportStorageService.kiemTraKhoa(key);
        try {
            s3.putObject(
                    r ->
                            r.bucket(bucket)
                                    .key(key)
                                    .contentType("application/pdf")
                                    .contentLength((long) bytes.length),
                    RequestBody.fromBytes(bytes));
        } catch (SdkException ex) {
            throw NghiepVuNhomException.luuTru(
                    "Không thể lưu PDF. Kho lưu trữ đang không sẵn sàng; vui lòng thử lại sau.",
                    ex);
        }
    }

    @Override
    public byte[] get(String key, int maxBytes) {
        ReportStorageService.kiemTraKhoa(key);
        try (var stream = s3.getObject(r -> r.bucket(bucket).key(key))) {
            if (stream.response().contentLength() != null
                    && stream.response().contentLength() > maxBytes) {
                stream.abort();
                throw NghiepVuNhomException.luuTru("Kích thước PDF lưu trữ không khớp metadata.", null);
            }
            var bytes = stream.readNBytes(maxBytes + 1);
            if (bytes.length > maxBytes) {
                stream.abort();
                throw NghiepVuNhomException.luuTru("Kích thước PDF lưu trữ không khớp metadata.", null);
            }
            return bytes;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404)
                throw NghiepVuNhomException.thieu(
                        "Không tìm thấy tệp PDF trong kho lưu trữ. Metadata được giữ nguyên.");
            throw NghiepVuNhomException.luuTru("Không thể tải PDF do kho lưu trữ đang không sẵn sàng.", ex);
        } catch (SdkException | IOException ex) {
            throw NghiepVuNhomException.luuTru("Không thể tải PDF do kho lưu trữ đang không sẵn sàng.", ex);
        }
    }

    @Override
    public void delete(String key) {
        ReportStorageService.kiemTraKhoa(key);
        try {
            s3.deleteObject(r -> r.bucket(bucket).key(key));
        } catch (SdkException ex) {
            throw NghiepVuNhomException.luuTru("Chưa thể dọn tệp PDF sau giao dịch thất bại.", ex);
        }
    }
}
