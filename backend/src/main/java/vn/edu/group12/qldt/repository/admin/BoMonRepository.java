package vn.edu.group12.qldt.repository.admin;

import vn.edu.group12.qldt.entity.admin.BoMon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoMonRepository extends JpaRepository<BoMon, String> {

    List<BoMon> findAllByHoatDongTrueOrderByTenBoMonAsc();
}