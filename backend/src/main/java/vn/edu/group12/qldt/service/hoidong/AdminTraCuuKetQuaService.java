package vn.edu.group12.qldt.service.hoidong;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.dto.hoidong.DotDangKyLuaChon;
import vn.edu.group12.qldt.dto.hoidong.KetQuaTraCuuHienThi;
import vn.edu.group12.qldt.repository.hoidong.KetQuaRepository;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
@Transactional(readOnly = true)
public class AdminTraCuuKetQuaService {

    private final KetQuaRepository ketQuaRepository;

    public AdminTraCuuKetQuaService(KetQuaRepository ketQuaRepository) {
        this.ketQuaRepository = ketQuaRepository;
    }

    public List<DotDangKyLuaChon> layDotCoKetQuaCongBo() {
        return ketQuaRepository.layDotCoKetQuaCongBo();
    }

    public List<KetQuaTraCuuHienThi> traCuuTheoDot(String maDot) {
        if (maDot == null || maDot.isBlank()) {
            return List.of();
        }

        return ketQuaRepository.traCuuKetQuaCongBoTheoDot(maDot.strip());
    }
}
