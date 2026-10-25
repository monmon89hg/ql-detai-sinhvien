package vn.edu.group12.qldt.service.auth;

import vn.edu.group12.qldt.entity.admin.NguoiDung;
import vn.edu.group12.qldt.entity.admin.NguoiDungVaiTro;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;
import vn.edu.group12.qldt.repository.admin.NguoiDungVaiTroRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    public CustomUserDetailsService(
            NguoiDungRepository nguoiDungRepository,
            NguoiDungVaiTroRepository nguoiDungVaiTroRepository
    ) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        NguoiDung nguoiDung = nguoiDungRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Không tìm thấy tài khoản"));

        List<SimpleGrantedAuthority> authorities =
                nguoiDungVaiTroRepository
                        .findAllById_MaNguoiDung(nguoiDung.getMaNguoiDung())
                        .stream()
                        .map(NguoiDungVaiTro::getVaiTro)
                        .map(vaiTro -> new SimpleGrantedAuthority(
                                "ROLE_" + vaiTro.getMaVaiTro()
                        ))
                        .toList();

        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException(
                    "Tài khoản chưa được cấp vai trò"
            );
        }

        boolean taiKhoanHoatDong =
                "HOAT_DONG".equals(nguoiDung.getTrangThai());

        return new QldtUserDetails(
                nguoiDung.getMaNguoiDung(), nguoiDung.getEmail(),
                nguoiDung.getMatKhauHash(), taiKhoanHoatDong, authorities);
    }
}
