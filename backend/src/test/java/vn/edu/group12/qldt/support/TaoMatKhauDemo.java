package vn.edu.group12.qldt.support;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TaoMatKhauDemo {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String matKhauDemo = "HoiDongDemo@2026";
        String matKhauHash = encoder.encode(matKhauDemo);

        System.out.println(matKhauHash);
    }
}