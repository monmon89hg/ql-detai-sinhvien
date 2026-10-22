package vn.edu.group12.qldt.service.hoidong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.edu.group12.qldt.dto.hoidong.NguoiNhanKetQua;
import vn.edu.group12.qldt.entity.hoidong.EmailThongBaoKetQua;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiEmailKetQua;
import vn.edu.group12.qldt.repository.hoidong.EmailThongBaoKetQuaRepository;
import vn.edu.group12.qldt.repository.hoidong.KetQuaRepository;

@ExtendWith(MockitoExtension.class)
class TaoEmailThongBaoKetQuaServiceTests {

    @Mock
    private KetQuaRepository ketQuaRepository;

    @Mock
    private EmailThongBaoKetQuaRepository emailRepository;

    @InjectMocks
    private TaoEmailThongBaoKetQuaService service;

    @Test
    void taoMotEmailChoMoiSinhVienTrongNhom() {
        NguoiNhanKetQua sv1 = mock(NguoiNhanKetQua.class);
        when(sv1.getMaSinhVien()).thenReturn("SV01");
        when(sv1.getEmail()).thenReturn("sv01@example.test");

        NguoiNhanKetQua sv2 = mock(NguoiNhanKetQua.class);
        when(sv2.getMaSinhVien()).thenReturn("SV02");
        when(sv2.getEmail()).thenReturn("sv02@example.test");

        when(ketQuaRepository.layNguoiNhanTheoNhom("N01"))
                .thenReturn(List.of(sv1, sv2));

        service.taoChoNhom("N01");

        ArgumentCaptor<EmailThongBaoKetQua> captor =
                ArgumentCaptor.forClass(EmailThongBaoKetQua.class);
        verify(emailRepository, times(2)).save(captor.capture());

        List<EmailThongBaoKetQua> emails = captor.getAllValues();
        Set<String> maSinhVien = emails.stream()
                .map(EmailThongBaoKetQua::getMaSinhVien)
                .collect(Collectors.toSet());

        assertEquals(Set.of("SV01", "SV02"), maSinhVien);
        assertTrue(emails.stream().allMatch(email ->
                email.getTrangThai() == TrangThaiEmailKetQua.CHO_GUI));
    }

    @Test
    void thieuDiaChiEmailThiKhongTaoBanGhi() {
        NguoiNhanKetQua sinhVien = mock(NguoiNhanKetQua.class);
        when(sinhVien.getMaSinhVien()).thenReturn("SV01");
        when(sinhVien.getEmail()).thenReturn(" ");

        when(ketQuaRepository.layNguoiNhanTheoNhom("N01"))
                .thenReturn(List.of(sinhVien));

        assertThrows(IllegalStateException.class,
                () -> service.taoChoNhom("N01"));

        verifyNoInteractions(emailRepository);
    }
}