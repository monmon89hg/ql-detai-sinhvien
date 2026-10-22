package vn.edu.group12.qldt.repository.hoidong;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.group12.qldt.entity.hoidong.KetQua;

public interface KetQuaRepository extends JpaRepository<KetQua, String> {

    // Khóa chính là mã nhóm; JpaRepository đã có findById và save.
}