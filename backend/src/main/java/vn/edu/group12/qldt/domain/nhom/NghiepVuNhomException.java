package vn.edu.group12.qldt.domain.nhom;

public class NghiepVuNhomException extends RuntimeException {
    public enum Loai {
        KHONG_DANG_NHAP,
        CAM_TRUY_CAP,
        KHONG_TIM_THAY,
        XUNG_DOT,
        DU_LIEU,
        LUU_TRU
    }

    private final Loai loai;

    public NghiepVuNhomException(Loai loai, String message) {
        super(message);
        this.loai = loai;
    }

    public NghiepVuNhomException(Loai loai, String message, Throwable cause) {
        super(message, cause);
        this.loai = loai;
    }

    public Loai getLoai() {
        return loai;
    }

    public static NghiepVuNhomException cam(String message) {
        return new NghiepVuNhomException(Loai.CAM_TRUY_CAP, message);
    }

    public static NghiepVuNhomException thieu(String message) {
        return new NghiepVuNhomException(Loai.KHONG_TIM_THAY, message);
    }

    public static NghiepVuNhomException xungDot(String message) {
        return new NghiepVuNhomException(Loai.XUNG_DOT, message);
    }

    public static NghiepVuNhomException duLieu(String message) {
        return new NghiepVuNhomException(Loai.DU_LIEU, message);
    }

    public static NghiepVuNhomException luuTru(String message, Throwable cause) {
        return new NghiepVuNhomException(Loai.LUU_TRU, message, cause);
    }
}
