package vn.edu.group12.qldt.repository.admin;

import vn.edu.group12.qldt.entity.admin.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {

    Optional<NguoiDung> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}