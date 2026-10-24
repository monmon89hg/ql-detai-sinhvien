package vn.edu.group12.qldt.service.hoidong;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import vn.edu.group12.qldt.dto.hoidong.DotDangKyLuaChon;
import vn.edu.group12.qldt.entity.hoidong.HoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiHoiDong;
import vn.edu.group12.qldt.repository.hoidong.HoiDongRepository;
import vn.edu.group12.qldt.repository.hoidong.ThanhVienHoiDongRepository;
import vn.edu.group12.qldt.support.SecurityTestUsers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class HoiDongDanhSachXoaServiceTests {
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
        ReflectionTestUtils.setField(hoiDong, "version", 2L);
        when(repository.timVaKhoaTheoMa("HD01")).thenReturn(Optional.of(hoiDong));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void defaultUsesNewestRoundEvenWhenItHasNoCouncil() {
        when(repository.layDanhSachDotMoiNhatTruoc()).thenReturn(List.of(dot("NEW"), dot("OLD")));
        when(repository.demDotTheoMa("NEW")).thenReturn(1L);
        var result = service.traCuuDanhSach(null);
        assertThat(result.maDotDuocChon()).isEqualTo("NEW");
        assertThat(result.danhSachHoiDong()).isEmpty();
        verify(repository).findByMaDotOrderByMaHoiDongAsc("NEW");
        verify(repository, never()).findAll(any(Sort.class));
        verify(repository, never()).findByMaDotOrderByMaHoiDongAsc("OLD");
    }

    @Test
    void historicalRoundIsQueriedDirectlyWithoutLoadingOtherRoundsCouncils() {
        hoiDong.setTrangThai(TrangThaiHoiDong.DA_KHOA);
        when(repository.layDanhSachDotMoiNhatTruoc()).thenReturn(List.of(dot("NEW"), dot("OLD")));
        when(repository.demDotTheoMa("OLD")).thenReturn(1L);
        when(repository.findByMaDotOrderByMaHoiDongAsc("OLD")).thenReturn(List.of(hoiDong));
        var result = service.traCuuDanhSach(" OLD ");
        assertThat(result.danhSachHoiDong()).containsExactly(hoiDong);
        assertThat(result.maHoiDongCoTheXoa()).isEmpty();
        verify(repository).layMaHoiDongCoTheXoa("OLD");
        verify(repository, never()).findAll(any(Sort.class));
    }

    @Test
    void explicitAllOptionLoadsAllButAnUnknownRoundDoesNot() {
        when(repository.layDanhSachDotMoiNhatTruoc()).thenReturn(List.of(dot("NEW")));
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(hoiDong));
        when(repository.layMaHoiDongCoTheXoa(null)).thenReturn(List.of("HD01"));
        var result = service.traCuuDanhSach("");
        assertThat(result.maDotDuocChon()).isEmpty();
        assertThat(result.maHoiDongCoTheXoa()).containsExactly("HD01");
        assertThatThrownBy(() -> service.traCuuDanhSach("UNKNOWN"))
            .isInstanceOf(EntityNotFoundException.class);
        verify(repository, never()).findByMaDotOrderByMaHoiDongAsc(any());
    }

    @Test
    void noRoundsReturnsAnEmptyPageAndRejectsAnUnknownSelection() {
        assertThat(service.traCuuDanhSach(null).danhSachHoiDong()).isEmpty();
        assertThatThrownBy(() -> service.traCuuDanhSach("UNKNOWN"))
            .isInstanceOf(EntityNotFoundException.class);
        verify(repository, never()).findAll(any(Sort.class));
    }

    @Test
    void onlyAnUnreferencedDraftWithTheCurrentVersionIsDeleted() {
        service.xoaHoiDong("HD01", 2L);
        verify(repository).delete(hoiDong);
        verify(repository).flush();
    }

    @ParameterizedTest
    @EnumSource(value = TrangThaiHoiDong.class, names = "NHAP", mode = EnumSource.Mode.EXCLUDE)
    void everyNonDraftStatePreventsDeletion(TrangThaiHoiDong state) {
        hoiDong.setTrangThai(state);
        assertThatThrownBy(() -> service.xoaHoiDong("HD01", 2L))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("nháp");
        verify(repository, never()).delete(any());
    }

    @Test
    void staleVersionMembersAndAssignmentsAllPreventDeletion() {
        assertThatThrownBy(() -> service.xoaHoiDong("HD01", 1L))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("đã thay đổi");
        when(repository.demThanhVien("HD01")).thenReturn(1L);
        assertThatThrownBy(() -> service.xoaHoiDong("HD01", 2L))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("thành viên");
        when(repository.demThanhVien("HD01")).thenReturn(0L);
        when(repository.demNhomDaPhanCong("HD01")).thenReturn(1L);
        assertThatThrownBy(() -> service.xoaHoiDong("HD01", 2L))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("phân công");
        verify(repository, never()).delete(any());
    }

    @Test
    void inactiveDeanAndMissingCouncilCannotBeDeleted() {
        when(repository.demTruongKhoaHoatDong("TK")).thenReturn(0L);
        assertThatThrownBy(() -> service.xoaHoiDong("HD01", 2L)).isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).timVaKhoaTheoMa(any());
        when(repository.demTruongKhoaHoatDong("TK")).thenReturn(1L);
        assertThatThrownBy(() -> service.xoaHoiDong("MISSING", 0L))
            .isInstanceOf(EntityNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    private DotDangKyLuaChon dot(String maDot) {
        return new DotDangKyLuaChon() {
            public String getMaDot() { return maDot; }
            public String getTenDot() { return maDot; }
        };
    }
}
