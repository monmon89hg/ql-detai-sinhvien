package vn.edu.group12.qldt.service.hoidong;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import vn.edu.group12.qldt.dto.hoidong.HoiDongSuaForm;
import vn.edu.group12.qldt.entity.hoidong.HoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiHoiDong;
import vn.edu.group12.qldt.repository.hoidong.HoiDongRepository;
import vn.edu.group12.qldt.repository.hoidong.ThanhVienHoiDongRepository;
import vn.edu.group12.qldt.support.SecurityTestUsers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class HoiDongSuaServiceTests {
    private final HoiDongRepository repository = mock(HoiDongRepository.class);
    private final HoiDongService service = new HoiDongService(
        repository, mock(ThanhVienHoiDongRepository.class));
    private final HoiDong hoiDong = new HoiDong();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
            SecurityTestUsers.authentication("TK", "TRUONG_KHOA"));
        when(repository.demTruongKhoaHoatDong("TK")).thenReturn(1L);
        hoiDong.setMaHoiDong("HD01");
        hoiDong.setMaDot("DOT01");
        hoiDong.setMaNguoiTao("NGUOI_TAO");
        ReflectionTestUtils.setField(hoiDong, "version", 2L);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void editTrimsPermittedFieldsWithoutMutatingManagedIdOrRound() {
        when(repository.timVaKhoaTheoMa("HD01")).thenReturn(Optional.of(hoiDong));
        when(repository.suaThongTin("HD01", "HD02", "Tên mới", null, 2L)).thenReturn(1);
        HoiDong daLuu = new HoiDong();
        daLuu.setMaHoiDong("HD02");
        when(repository.findById("HD02")).thenReturn(Optional.of(daLuu));

        assertThat(service.suaThongTin("HD01", form())).isSameAs(daLuu);
        assertThat(hoiDong.getMaHoiDong()).isEqualTo("HD01");
        assertThat(hoiDong.getMaDot()).isEqualTo("DOT01");
        assertThat(hoiDong.getMaNguoiTao()).isEqualTo("NGUOI_TAO");
        verify(repository).suaThongTin("HD01", "HD02", "Tên mới", null, 2L);
        verify(repository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @EnumSource(value = TrangThaiHoiDong.class, names = "NHAP", mode = EnumSource.Mode.EXCLUDE)
    void nonDraftCouncilsCannotBeEditedEvenWithAValidForm(TrangThaiHoiDong trangThai) {
        hoiDong.setTrangThai(trangThai);
        when(repository.timVaKhoaTheoMa("HD01")).thenReturn(Optional.of(hoiDong));
        when(repository.findById("HD01")).thenReturn(Optional.of(hoiDong));

        assertThatThrownBy(() -> service.suaThongTin("HD01", form()))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("nháp");
        assertThatThrownBy(() -> service.layDeSua("HD01"))
            .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).suaThongTin(any(), any(), any(), any(), any());
    }

    @Test
    void duplicateCodeAndStaleVersionCannotOverwriteData() {
        when(repository.timVaKhoaTheoMa("HD01")).thenReturn(Optional.of(hoiDong));
        when(repository.existsById("HD02")).thenReturn(true);
        assertThatThrownBy(() -> service.suaThongTin("HD01", form()))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("đã tồn tại");

        HoiDongSuaForm cu = form();
        cu.setVersion(1L);
        assertThatThrownBy(() -> service.suaThongTin("HD01", cu))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("đã thay đổi");
        verify(repository, never()).suaThongTin(any(), any(), any(), any(), any());
    }

    @Test
    void inactiveDeanCannotOpenOrSaveEdits() {
        when(repository.demTruongKhoaHoatDong("TK")).thenReturn(0L);
        assertThatThrownBy(() -> service.suaThongTin("HD01", form()))
            .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.layDeSua("HD01"))
            .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).timVaKhoaTheoMa(any());
        verify(repository, never()).findById(any());
    }

    private HoiDongSuaForm form() {
        HoiDongSuaForm form = new HoiDongSuaForm();
        form.setMaHoiDong(" HD02 ");
        form.setTenHoiDong(" Tên mới ");
        form.setDiaDiem("   ");
        form.setVersion(2L);
        return form;
    }
}
