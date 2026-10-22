package vn.edu.group12.qldt.repository.admin;

import vn.edu.group12.qldt.entity.admin.NguoiDungVaiTro;
import vn.edu.group12.qldt.entity.admin.NguoiDungVaiTroId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NguoiDungVaiTroRepository
        extends JpaRepository<NguoiDungVaiTro, NguoiDungVaiTroId> {

    List<NguoiDungVaiTro> findAllById_MaNguoiDung(String maNguoiDung);

    boolean existsById_MaNguoiDungAndId_MaVaiTro(
            String maNguoiDung,
            String maVaiTro
    );

    @Query("""
        select count(link)
        from NguoiDungVaiTro link
        where link.vaiTro.maVaiTro = :maVaiTro
          and link.nguoiDung.trangThai = 'HOAT_DONG'
        """)
    long countNguoiDungHoatDongCoVaiTro(
            @Param("maVaiTro") String maVaiTro
    );
}