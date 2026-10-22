package vn.edu.group12.qldt.storage.nhom;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;

public interface ReportStorageService {
    void put(String objectKey, byte[] bytes);

    byte[] get(String objectKey, int maxBytes);

    void delete(String objectKey);

    static void kiemTraKhoa(String key) {
        if (key == null
                || !key.matches("[A-Za-z0-9][A-Za-z0-9_./-]{0,511}")
                || key.contains("..")
                || key.contains("//")
                || key.endsWith("/")) {
            throw NghiepVuNhomException.duLieu("Khóa tệp lưu trữ không hợp lệ.");
        }
    }
}
