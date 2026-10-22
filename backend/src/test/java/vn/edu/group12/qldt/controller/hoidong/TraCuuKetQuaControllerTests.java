package vn.edu.group12.qldt.controller.hoidong;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ExtendedModelMap;

import vn.edu.group12.qldt.dto.hoidong.DotDangKyLuaChon;
import vn.edu.group12.qldt.service.hoidong.TraCuuKetQuaService;

@ExtendWith(MockitoExtension.class)
class TraCuuKetQuaControllerTests {

    @Mock
    private TraCuuKetQuaService traCuuKetQuaService;

    @InjectMocks
    private TraCuuKetQuaController controller;

    @Test
    void dotKhongThuocQuyenBiChanTruocKhiLayKetQua() {
        DotDangKyLuaChon dotDuocPhep = new DotDangKyLuaChon() {
            @Override
            public String getMaDot() {
                return "DEMO_DOT_KLH26";
            }

            @Override
            public String getTenDot() {
                return "Khóa luận lịch sử tháng 9";
            }
        };

        when(traCuuKetQuaService.layDotCuaToi())
            .thenReturn(List.of(dotDuocPhep));

        assertThrows(
            AccessDeniedException.class,
            () -> controller.cuaToi(
                "DEMO_DOT_MH26",
                new ExtendedModelMap(),
                () -> "DEMO_SV26"
            )
        );

        verify(traCuuKetQuaService, never()).traCuuTheoDot(any());
    }
}