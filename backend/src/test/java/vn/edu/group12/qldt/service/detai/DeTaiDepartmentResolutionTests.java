package vn.edu.group12.qldt.service.detai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.group12.qldt.dto.detai.DeTaiDto;
import vn.edu.group12.qldt.entity.admin.BoMon;
import vn.edu.group12.qldt.entity.admin.NguoiDung;
import vn.edu.group12.qldt.entity.detai.DeTai;
import vn.edu.group12.qldt.entity.detai.DotDangKy;
import vn.edu.group12.qldt.repository.admin.BoMonRepository;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;
import vn.edu.group12.qldt.repository.detai.DeTaiRepository;

@ExtendWith(MockitoExtension.class)
class DeTaiDepartmentResolutionTests {
    @Mock DeTaiRepository deTaiRepository;
    @Mock DotDangKyService dotDangKyService;
    @Mock DeTaiGiangVienService deTaiGiangVienService;
    @Mock BoMonRepository boMonRepository;
    @Mock NguoiDungRepository nguoiDungRepository;
    @InjectMocks DeTaiService service;

    private DeTaiDto deXuat(String maBoMon) {
        var form = new DeTaiDto();
        form.setMaDot("DOT_MOI");
        form.setMaDeTai("DETAI_MOI");
        form.setMaBoMon(maBoMon);
        form.setTenDeTai("Cổng thông tin học tập");
        form.setNoiDung("Quản lý học liệu và lịch học của sinh viên.");
        var dot = new DotDangKy();
        when(dotDangKyService.getDotDangKyByMa("DOT_MOI")).thenReturn(dot);
        when(dotDangKyService.isGiaiDoan1(dot)).thenReturn(true);
        return form;
    }

    @Test void chonBoMonTuDanhMucMoiKhongCanDuLieuCu() {
        var form = deXuat(" cnpm ");
        var bm = new BoMon(); bm.setMaBoMon("CNPM");
        when(boMonRepository.findById("CNPM")).thenReturn(Optional.of(bm));
        when(deTaiRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("CNPM", service.deXuatDeTai(form, "GV_MOI").getMaBoMon());
        verifyNoInteractions(nguoiDungRepository);
    }

    @Test void thieuLuaChonThiLayBoMonCuaNguoiDeXuat() {
        var form = deXuat(null);
        var bm = new BoMon(); bm.setMaBoMon("HTTT");
        var gv = new NguoiDung(); gv.setBoMon(bm);
        when(nguoiDungRepository.findById("GV_MOI")).thenReturn(Optional.of(gv));
        when(boMonRepository.findById("HTTT")).thenReturn(Optional.of(bm));
        when(deTaiRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("HTTT", service.deXuatDeTai(form,"GV_MOI").getMaBoMon());
    }

    @Test void tuChoiBoMonKhongTonTaiVaKhongLuuDeTai() {
        var form = deXuat("KHONG_CO");
        when(boMonRepository.findById("KHONG_CO")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.deXuatDeTai(form,"GV_MOI"));
        verify(deTaiRepository,never()).save(any());
        verifyNoInteractions(deTaiGiangVienService);
    }

    @Test void tuChoiBoMonDaNgungHoatDong() {
        var form = deXuat("CNPM");
        var bm = new BoMon(); bm.setMaBoMon("CNPM"); bm.setHoatDong(false);
        when(boMonRepository.findById("CNPM")).thenReturn(Optional.of(bm));
        assertThrows(IllegalArgumentException.class, () -> service.deXuatDeTai(form,"GV_MOI"));
        verify(deTaiRepository,never()).save(any());
    }
}
