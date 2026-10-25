package vn.edu.group12.qldt;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import vn.edu.group12.qldt.config.SecurityConfig;
import vn.edu.group12.qldt.config.auth.PasswordConfig;
import vn.edu.group12.qldt.controller.auth.AuthController;
import vn.edu.group12.qldt.dto.detai.DeTaiDto;
import vn.edu.group12.qldt.dto.hoidong.HoiDongSuaForm;
import vn.edu.group12.qldt.entity.detai.DeTai;
import vn.edu.group12.qldt.entity.detai.DotDangKy;
import vn.edu.group12.qldt.repository.admin.*;
import vn.edu.group12.qldt.repository.detai.*;
import vn.edu.group12.qldt.repository.hoidong.*;
import vn.edu.group12.qldt.service.admin.BoMonService;
import vn.edu.group12.qldt.service.auth.CustomUserDetailsService;
import vn.edu.group12.qldt.service.detai.*;
import vn.edu.group12.qldt.service.hoidong.*;
import vn.edu.group12.qldt.support.SecurityTestUsers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Call real proxied services directly so HTTP rules cannot hide missing method authorization. */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, PasswordConfig.class, CustomUserDetailsService.class,
        DeTaiService.class, DeTaiGiangVienService.class, DotDangKyService.class, BoMonService.class,
        HoiDongService.class, ChamDiemHoiDongService.class, TraCuuKetQuaService.class,
        AdminTraCuuKetQuaService.class, TongHopKetQuaService.class})
class SecurityMethodAuthorizationTests {
    @Autowired DeTaiService topics;
    @Autowired DeTaiGiangVienService assignments;
    @Autowired DotDangKyService rounds;
    @Autowired BoMonService departments;
    @Autowired HoiDongService councils;
    @Autowired ChamDiemHoiDongService marking;
    @Autowired TraCuuKetQuaService studentResults;
    @Autowired AdminTraCuuKetQuaService publishedResults;
    @Autowired TongHopKetQuaService results;
    @MockitoBean vn.edu.group12.qldt.repository.hoidong.DanhGiaRepository danhGiaRepo;
    @MockitoBean NguoiDungRepository users;
    @MockitoBean NguoiDungVaiTroRepository roles;
    @MockitoBean BoMonRepository departmentRepo;
    @MockitoBean DeTaiRepository topicRepo;
    @MockitoBean DeTaiGiangVienRepository assignmentRepo;
    @MockitoBean DotDangKyRepository roundRepo;
    @MockitoBean HoiDongRepository councilRepo;
    @MockitoBean ThanhVienHoiDongRepository membershipRepo;
    @MockitoBean HoiDongDeTaiRepository councilAssignmentRepo;
    @MockitoBean PhieuChamDiemRepository marksRepo;
    @MockitoBean KetQuaRepository resultRepo;
    @MockitoBean TinhDiemTrungBinhService scoring;
    @MockitoBean TaoEmailThongBaoKetQuaService notifications;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SINH_VIEN", "GIANG_VIEN", "TRUONG_BO_MON"})
    void nonDeanCannotCallDeanServicesDirectly(String role) {
        signIn("DB_USER", role);
        assertThatThrownBy(() -> topics.duyetDeTai("DT01", "DB_USER")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> topics.tuChoiDeTai("DT01", "reason", "DB_USER")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> topics.congBoDeTai("DT01")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> assignments.phanCong("DT01", "OTHER", "HUONG_DAN", "DB_USER"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> assignments.xoaPhanCong("DT01", "OTHER")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> rounds.congBoDot("DOT01")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> councils.layDanhSach()).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> councils.traCuuDanhSach(null)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> results.congBo("N01")).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(topicRepo, assignmentRepo, roundRepo, councilRepo, resultRepo);
    }

    @Test
    void studentCannotCallAdminOrMarkingServices() {
        signIn("DB_SV", "SINH_VIEN");
        assertThatThrownBy(() -> departments.capNhatTrangThai("BM", true)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> marking.layDanhSachNhomDuocCham()).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> results.tongHop("N01", null)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> topics.deXuatDeTai(new DeTaiDto(), "DB_SV")).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(departmentRepo, marksRepo, resultRepo, topicRepo);
    }

    @Test
    void studentResultsUseTheDatabaseIdOfTheSignedInStudent() {
        signIn("DB_SV", "SINH_VIEN");
        studentResults.layDotCuaToi();
        studentResults.traCuuTheoDot("DOT01");
        verify(resultRepo).layDotCoKetQuaCuaSinhVien("DB_SV");
        verify(resultRepo).traCuuKetQuaCuaSinhVien("DB_SV", "DOT01");
    }

    @Test
    void lecturerCanReadPublishedResultsButStudentCannotUseTheAllGroupsService() {
        signIn("DB_GV", "GIANG_VIEN");
        publishedResults.layDotCoKetQuaCongBo();
        publishedResults.traCuuTheoDot("DOT01");
        verify(resultRepo).layDotCoKetQuaCongBo();
        verify(resultRepo).traCuuKetQuaCongBoTheoDot("DOT01");

        signIn("DB_SV", "SINH_VIEN");
        assertThatThrownBy(() -> publishedResults.traCuuTheoDot("DOT01"))
            .isInstanceOf(AccessDeniedException.class);
        verifyNoMoreInteractions(resultRepo);
    }

    @Test
    void lecturerWithoutAssignmentCannotMarkAnotherGroup() {
        signIn("DB_GV", "GIANG_VIEN");
        when(membershipRepo.demGiangVienHopLe("DB_GV")).thenReturn(1L);
        assertThatThrownBy(() -> marking.layThongTinNhom("UNASSIGNED"))
                .isInstanceOf(AccessDeniedException.class);
        verify(marksRepo).layDanhSachNhomDuocCham("DB_GV");
    }

    @Test
    void deanCanCallProtectedCouncilReadService() {
        signIn("DB_TK", "TRUONG_KHOA");
        councils.layDanhSach();
        verify(councilRepo).findAll(any(org.springframework.data.domain.Sort.class));
    }

    @Test
    void onlyDeanCanCallCouncilEditingServicesDirectly() {
        HoiDongSuaForm form = new HoiDongSuaForm();
        form.setMaHoiDong("HD02");
        form.setTenHoiDong("Hội đồng mới");
        form.setVersion(0L);
        for (String role : new String[]{"ADMIN", "GIANG_VIEN", "SINH_VIEN", "TRUONG_BO_MON"}) {
            signIn("USER", role);
            assertThatThrownBy(() -> councils.layDeSua("HD01"))
                .isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(() -> councils.suaThongTin("HD01", form))
                .isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(() -> councils.xoaHoiDong("HD01", 0L))
                .isInstanceOf(AccessDeniedException.class);
        }
        verifyNoInteractions(councilRepo);
    }

    @Test
    void adminCanReadCouncilListButCannotChangeCouncil() {
        signIn("DB_ADMIN", "ADMIN");

        councils.layDanhSach();
        verify(councilRepo).findAll(any(org.springframework.data.domain.Sort.class));

        assertThatThrownBy(() -> councils.chuyenSangSanSang("HD01"))
            .isInstanceOf(AccessDeniedException.class);
        verifyNoMoreInteractions(councilRepo);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GIANG_VIEN", "TRUONG_BO_MON"})
    void proposalKeepsAutomaticSelfAssignmentWithoutDeanPermission(String role) {
        signIn("DB_GV", role);
        DotDangKy round = new DotDangKy();
        round.setBatDauGv(LocalDateTime.now().minusDays(1));
        round.setKetThucGv(LocalDateTime.now().plusDays(1));
        when(roundRepo.findById("DOT01")).thenReturn(Optional.of(round));
        when(topicRepo.save(any())).thenAnswer(invocation -> {
            DeTai saved = invocation.getArgument(0);
            when(topicRepo.findById(saved.getMaDeTai())).thenReturn(Optional.of(saved));
            return saved;
        });
        DeTaiDto dto = new DeTaiDto();
        dto.setMaDeTai("DT01");
        dto.setMaDot("DOT01");
        dto.setMaBoMon("CNPM");
        var boMon = new vn.edu.group12.qldt.entity.admin.BoMon();
        boMon.setMaBoMon("CNPM");
        when(departmentRepo.findById("CNPM")).thenReturn(Optional.of(boMon));
        dto.setTenDeTai("Security regression fixture");
        when(danhGiaRepo.giangVienHuongDanHopLe("DB_GV")).thenReturn(true);
        topics.deXuatDeTai(dto, "DB_GV");
        verify(assignmentRepo).save(argThat(assignment ->
                "DB_GV".equals(assignment.getId().getMaGiangVien())
                && "HUONG_DAN".equals(assignment.getLoaiPhanCong())));
    }

    @Test
    void selfAssignmentCannotBeUsedOnAnotherLecturersTopic() {
        signIn("DB_GV", "GIANG_VIEN");
        DeTai topic = new DeTai();
        topic.setMaNguoiDeXuat("OTHER");
        topic.setTrangThai("CHO_DUYET");
        when(topicRepo.findById("DT01")).thenReturn(Optional.of(topic));
        assertThatThrownBy(() -> assignments.phanCongNguoiDeXuat("DT01"))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(assignmentRepo);
    }

    private void signIn(String id, String role) {
        SecurityContextHolder.getContext().setAuthentication(SecurityTestUsers.authentication(id, role));
    }
}
