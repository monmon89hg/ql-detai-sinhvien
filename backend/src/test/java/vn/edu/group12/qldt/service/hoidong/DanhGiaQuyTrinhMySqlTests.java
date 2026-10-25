package vn.edu.group12.qldt.service.hoidong;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.group12.qldt.dto.hoidong.PhieuChamDiemForm;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiKetQua;
import vn.edu.group12.qldt.repository.hoidong.*;
import vn.edu.group12.qldt.repository.nhom.NhomBaoCaoRepository;
import vn.edu.group12.qldt.service.detai.DeTaiGiangVienService;
import vn.edu.group12.qldt.support.CiDatabaseGuard;
import vn.edu.group12.qldt.support.SecurityTestUsers;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Chạy với MySQL loopback/schema qldt_ci được CiDatabaseGuard bảo vệ, không gửi thư thật. */
@Tag("mysql")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"ci-mysql","staging"})
@ContextConfiguration(initializers=CiDatabaseGuard.class)
@Transactional
class DanhGiaQuyTrinhMySqlTests {
    @Autowired JdbcTemplate jdbc;
    @Autowired MoChamService moCham;
    @Autowired HoiDongService hoiDongService;
    @Autowired ChamDiemHoiDongService cham;
    @Autowired TongHopKetQuaService tongHop;
    @Autowired DeTaiGiangVienService giaoGiangVien;
    @Autowired HoiDongDeTaiRepository ds;
    @Autowired PhieuChamDiemRepository phieu;
    @Autowired NhomBaoCaoRepository nhom;
    @Autowired DanhGiaRepository danhGia;
    @Autowired MockMvc mvc;
    @Autowired jakarta.persistence.EntityManager em;
    @Autowired vn.edu.group12.qldt.service.nhom.NhomService nhomService;
    @Autowired vn.edu.group12.qldt.service.nhom.BaoCaoService baoCaoService;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    void sign(String id,String role) { SecurityContextHolder.getContext().setAuthentication(SecurityTestUsers.authentication(id,role)); }
    PhieuChamDiemForm form(String diem) { var f=new PhieuChamDiemForm(); f.setDiem(new BigDecimal(diem)); f.setNhanXet("Nhận xét kiểm thử"); return f; }

    @ParameterizedTest
    @ValueSource(strings={"MON_HOC","NCKH","TLCN","KLTN"})
    void allTypesCompleteOpeningGradingAggregationPublicationAndHistoricalLookup(String loai) throws Exception {
        var f=new Fixture(loai);
        sign(f.dean,"TRUONG_KHOA");
        assertThat(tongHop.layDanhSachNhomChoTruongKhoa()).anyMatch(n -> f.group.equals(n.getMaNhom()));
        moCham.moCham(f.group);
        assertThat(jdbc.queryForObject("SELECT bat_dau_cham_luc FROM dang_ky_de_tai WHERE ma_nhom=?",LocalDateTime.class,f.group)).isNotNull();
        assertThatThrownBy(() -> giaoGiangVien.xoaPhanCong(f.topic,f.marker)).isInstanceOf(IllegalArgumentException.class);
        sign(f.students[0],"SINH_VIEN");
        assertThatThrownBy(() -> baoCaoService.nop(new org.springframework.mock.web.MockMultipartFile("file","test.pdf","application/pdf",new byte[]{1})))
            .isInstanceOf(vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException.class);
        sign(f.marker,"GIANG_VIEN");
        assertThat(cham.layDanhSachNhomDuocCham()).anyMatch(n -> f.group.equals(n.getMaNhom()) && !"HOI_DONG".equals(n.getLoaiPhieu()));
        cham.luuNhap(f.group,form("8.00"));
        assertThatThrownBy(() -> tongHop.tongHop(f.group,"Chưa đủ phiếu")).isInstanceOfAny(IllegalArgumentException.class,AccessDeniedException.class);
        cham.nopPhieu(f.group,form("8.00"));
        assertThatThrownBy(() -> cham.nopPhieu(f.group,form("9.00"))).isInstanceOf(IllegalArgumentException.class);
        if (loai.equals("KLTN")) {
            for (String gv:f.councilTeachers) { sign(gv,"GIANG_VIEN"); cham.nopPhieu(f.group,form("9.00")); }
            sign(f.councilTeachers[0],"GIANG_VIEN");
        }
        mvc.perform(get("/ket-qua/"+f.group+"/chi-tiet").with(user(SecurityTestUsers.user(loai.equals("KLTN")?f.councilTeachers[0]:f.marker,"GIANG_VIEN"))))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Xác nhận tổng hợp")));
        sign(loai.equals("KLTN")?f.councilTeachers[0]:f.marker,"GIANG_VIEN");
        var k=tongHop.tongHop(f.group,"Đã đối chiếu các phiếu");
        assertThat(k.getDiemCuoi()).isEqualByComparingTo(loai.equals("KLTN")?"8.75":"8.00");
        sign(f.dean,"TRUONG_KHOA");
        assertThat(tongHop.locDanhSach(f.round,loai)).extracting(n -> n.getMaNhom()).containsExactly(f.group);
        assertThat(tongHop.locDanhSach(f.round,loai.equals("KLTN")?"MON_HOC":"KLTN")).isEmpty();
        assertThat(tongHop.congBo(f.group).getTrangThai()).isEqualTo(TrangThaiKetQua.CONG_BO);
        em.flush();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM thanh_vien_nhom WHERE ma_nhom=? AND trang_thai='KET_THUC' AND la_thanh_vien_chot=1",Integer.class,f.group)).isEqualTo(3);
        assertThat(nhom.nhomHienTai(f.students[0])).isEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM email_thong_bao_ket_qua WHERE ma_nhom=? AND trang_thai='CHO_GUI'",Integer.class,f.group)).isEqualTo(3);
        assertThatThrownBy(() -> tongHop.congBo(f.group)).isInstanceOf(IllegalArgumentException.class);
        if (loai.equals("KLTN")) moCham.khoaHoiDong(f.council);
        sign(f.dean,"TRUONG_KHOA");
        jdbc.update("UPDATE email_thong_bao_ket_qua SET trang_thai='LOI',so_lan_thu=5 WHERE ma_nhom=? AND ma_sinh_vien=?",f.group,f.students[0]);
        mvc.perform(get("/ket-qua/"+f.group+"/chi-tiet").with(user(SecurityTestUsers.user(f.dean,"TRUONG_KHOA"))))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Thử gửi lại")));
        sign(f.dean,"TRUONG_KHOA");
        moCham.thuLaiEmail(f.group,f.students[0]);
        assertThat(jdbc.queryForObject("SELECT so_lan_thu FROM email_thong_bao_ket_qua WHERE ma_nhom=? AND ma_sinh_vien=?",Integer.class,f.group,f.students[0])).isZero();
        sign(f.students[0],"SINH_VIEN");
        var taoNhom=new vn.edu.group12.qldt.dto.nhom.TaoNhomForm();
        taoNhom.setTenNhom("Nhóm sau công bố"); taoNhom.setMaDot(f.round);
        assertThat(nhomService.tao(taoNhom)).isNotBlank();
        mvc.perform(get("/ket-qua/cua-toi").param("maDot",f.round).with(user(SecurityTestUsers.user(f.students[0],"SINH_VIEN"))))
            .andExpect(status().isOk()).andExpect(content().string(containsString(f.group)));
    }

    @Test void openingRejectsMissingReportAndUnfinalizedMembers() {
        var f=new Fixture("MON_HOC"); sign(f.dean,"TRUONG_KHOA");
        jdbc.update("DELETE FROM nop_bao_cao WHERE ma_nhom=?",f.group);
        assertThatThrownBy(() -> moCham.moCham(f.group)).isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbc.queryForObject("SELECT trang_thai FROM dang_ky_de_tai WHERE ma_nhom=?",String.class,f.group)).isEqualTo("DA_DUYET");
        var g=new Fixture("MON_HOC"); sign(g.dean,"TRUONG_KHOA");
        jdbc.update("UPDATE thanh_vien_nhom SET la_thanh_vien_chot=0 WHERE ma_nhom=? AND ma_sinh_vien=?",g.group,g.students[1]);
        assertThatThrownBy(() -> moCham.moCham(g.group)).isInstanceOf(IllegalArgumentException.class);

    }

    @Test void onlyGraduationRoundsCanCreateCouncils() {
        var course=new Fixture("MON_HOC"); sign(course.dean,"TRUONG_KHOA");
        var form=new vn.edu.group12.qldt.dto.hoidong.HoiDongForm();
        form.setMaHoiDong(course.p+"NEWHD"); form.setTenHoiDong("Hội đồng mới"); form.setMaDot(course.round);
        assertThatThrownBy(() -> hoiDongService.taoHoiDong(form)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("khóa luận");
        var thesis=new Fixture("KLTN"); sign(thesis.dean,"TRUONG_KHOA");
        form.setMaHoiDong(thesis.p+"NEWHD"); form.setMaDot(thesis.round);
        assertThat(hoiDongService.taoHoiDong(form).getMaDot()).isEqualTo(thesis.round);
        assertThat(hoiDongService.layDanhSachDot()).noneMatch(d -> course.round.equals(d.getMaDot()));
    }

    @Test void reviewerCannotAlsoBeCouncilMemberAndAssignmentStaysUnchanged() {
        var f=new Fixture("KLTN"); sign(f.dean,"TRUONG_KHOA");
        jdbc.update("DELETE FROM thanh_vien_hoi_dong WHERE ma_hoi_dong=? AND chuc_danh='UY_VIEN'",f.council);
        jdbc.update("INSERT INTO thanh_vien_hoi_dong(ma_hoi_dong,ma_giang_vien,chuc_danh) VALUES(?,?,'UY_VIEN')",f.council,f.marker);
        assertThatThrownBy(() -> moCham.moCham(f.group)).isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbc.queryForObject("SELECT trang_thai FROM hoi_dong WHERE ma_hoi_dong=?",String.class,f.council)).isEqualTo("SAN_SANG");
    }

    @Test void onlyAssignedLecturerMayReadAllGradesOrAggregate() throws Exception {
        var f=new Fixture("MON_HOC"); sign(f.dean,"TRUONG_KHOA"); moCham.moCham(f.group);
        sign(f.supervisor,"GIANG_VIEN");
        assertThatThrownBy(() -> cham.layThongTinNhom(f.group)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> tongHop.tongHop(f.group,null)).isInstanceOf(AccessDeniedException.class);
        mvc.perform(get("/ket-qua/"+f.group+"/chi-tiet").with(user(SecurityTestUsers.user(f.supervisor,"GIANG_VIEN"))))
            .andExpect(status().isForbidden());
        mvc.perform(post("/ket-qua/"+f.group+"/mo-cham").with(csrf()).with(user(SecurityTestUsers.user(f.marker,"GIANG_VIEN"))))
            .andExpect(status().isForbidden());
    }

    @Test void publicViewsAndExcelIncludeGroupsWithoutCouncils() throws Exception {
        var f=new Fixture("TLCN"); sign(f.dean,"TRUONG_KHOA");
        mvc.perform(get("/ket-qua/quan-ly").param("maDot",f.round).param("loai","TLCN").with(user(SecurityTestUsers.user(f.dean,"TRUONG_KHOA"))))
            .andExpect(status().isOk()).andExpect(content().string(containsString(f.group))).andExpect(content().string(containsString("Mở chấm")));
        byte[] excel=mvc.perform(get("/ket-qua/quan-ly/xuat-excel").param("maDot",f.round).param("loai","TLCN").with(user(SecurityTestUsers.user(f.dean,"TRUONG_KHOA"))))
            .andExpect(status().isOk()).andExpect(header().string("Content-Disposition",containsString("quan-ly-ket-qua.xlsx")))
            .andReturn().getResponse().getContentAsByteArray();
        try (var wb=new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(excel))) {
            assertThat(wb.getSheetAt(0).getLastRowNum()).isEqualTo(1);
            assertThat(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue()).contains(f.group);
            assertThat(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue()).isEqualTo("—");
        }
    }

    @Test void assignmentPolicyAllowsSupervisionBeforeOpeningButRejectsWrongOrDuplicateMarkers() {
        var f=new Fixture("MON_HOC"); sign(f.dean,"TRUONG_KHOA");
        f.user(f.p+"NEW","GV","GIANG_VIEN");
        assertThatThrownBy(() -> giaoGiangVien.phanCong(f.topic,f.p+"NEW","PHAN_BIEN",f.dean)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> giaoGiangVien.phanCong(f.topic,f.p+"NEW","CHAM_DOC_LAP",f.dean)).isInstanceOf(IllegalArgumentException.class);
        giaoGiangVien.phanCong(f.topic,f.p+"NEW","HUONG_DAN",f.dean);
        em.flush();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM de_tai_giang_vien WHERE ma_de_tai=? AND loai_phan_cong='HUONG_DAN'",Integer.class,f.topic)).isEqualTo(2);
    }

    @Test void automaticSupervisionStillWorksForANewPendingJpaProposal() {
        var f=new Fixture("MON_HOC");
        var dt=new vn.edu.group12.qldt.entity.detai.DeTai();
        dt.setMaDeTai(f.p+"DT2"); dt.setMaDot(f.round); dt.setMaBoMon(f.p+"BM");
        dt.setTenDeTai("Đề xuất mới"); dt.setNoiDung("Nội dung mới");
        dt.setMaNguoiDeXuat(f.supervisor); dt.setTrangThai("CHO_DUYET");
        em.persist(dt);
        sign(f.supervisor,"GIANG_VIEN");
        giaoGiangVien.phanCongNguoiDeXuat(dt.getMaDeTai()); em.flush();
        assertThat(jdbc.queryForObject("SELECT loai_phan_cong FROM de_tai_giang_vien WHERE ma_de_tai=? AND ma_giang_vien=?",String.class,dt.getMaDeTai(),f.supervisor)).isEqualTo("HUONG_DAN");
    }

    @Test void reviewerDeadlineIsCheckedWhenSavingAndSubmitting() {
        var f=new Fixture("TLCN"); sign(f.dean,"TRUONG_KHOA"); moCham.moCham(f.group);
        var now=LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        jdbc.update("UPDATE dot_dang_ky SET ket_thuc_sv=?,han_nop_bao_cao=?,han_nop_diem_pb=? WHERE ma_dot=?",now.minusDays(4),now.minusDays(3),now.minusDays(2),f.round);
        sign(f.marker,"GIANG_VIEN");
        assertThatThrownBy(() -> cham.luuNhap(f.group,form("8.00"))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("hết hạn");
        assertThatThrownBy(() -> cham.nopPhieu(f.group,form("8.00"))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("hết hạn");
    }

    @Test void councilCanBeReassignedBeforeOpeningAndReturnsToDraftButCannotChangeAfterOpening() {
        var f=new Fixture("KLTN"); sign(f.dean,"TRUONG_KHOA");
        String other=f.p+"CD2";
        jdbc.update("INSERT INTO hoi_dong(ma_hoi_dong,ten_hoi_dong,ma_dot,trang_thai,ma_nguoi_tao) VALUES(?,? ,?,'SAN_SANG',?)",other,"Hội đồng thay thế",f.round,f.dean);
        for (int i=0;i<3;i++) jdbc.update("INSERT INTO thanh_vien_hoi_dong(ma_hoi_dong,ma_giang_vien,chuc_danh) VALUES(?,?,?)",other,f.councilTeachers[i],new String[]{"CHU_TICH","THU_KY","UY_VIEN"}[i]);
        moCham.doiHoiDong(f.council,f.group,other);
        assertThat(phanCongHienTai(f.group)).isEqualTo(other);
        moCham.suaLaiHoiDong(f.council);
        assertThat(jdbc.queryForObject("SELECT trang_thai FROM hoi_dong WHERE ma_hoi_dong=?",String.class,f.council)).isEqualTo("NHAP");
        moCham.moCham(f.group);
        assertThatThrownBy(() -> moCham.doiHoiDong(other,f.group,f.council)).isInstanceOf(IllegalArgumentException.class);
        assertThat(phanCongHienTai(f.group)).isEqualTo(other);
    }

    String phanCongHienTai(String group) { return jdbc.queryForObject("SELECT ma_hoi_dong FROM hoi_dong_de_tai WHERE ma_nhom=?",String.class,group); }

    @Test
    @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void publicationRollsBackGroupMembersAndResultIfEmailQueueCannotBeCreated() {
        var tx=new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        var f=tx.execute(status -> new Fixture("MON_HOC"));
        try {
            sign(f.dean,"TRUONG_KHOA"); moCham.moCham(f.group);
            sign(f.marker,"GIANG_VIEN"); cham.nopPhieu(f.group,form("8.00")); tongHop.tongHop(f.group,null);
            jdbc.update("UPDATE nguoi_dung SET email='' WHERE ma_nguoi_dung=?",f.students[0]);
            sign(f.dean,"TRUONG_KHOA");
            assertThatThrownBy(() -> tongHop.congBo(f.group)).isInstanceOf(IllegalStateException.class);
            assertThat(jdbc.queryForObject("SELECT trang_thai FROM ket_qua WHERE ma_nhom=?",String.class,f.group)).isEqualTo("DA_TONG_HOP");
            assertThat(jdbc.queryForObject("SELECT trang_thai FROM nhom_sinh_vien WHERE ma_nhom=?",String.class,f.group)).isEqualTo("DANG_THUC_HIEN");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM thanh_vien_nhom WHERE ma_nhom=? AND trang_thai='THAM_GIA'",Integer.class,f.group)).isEqualTo(3);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM email_thong_bao_ket_qua WHERE ma_nhom=?",Integer.class,f.group)).isZero();
        } finally { tx.executeWithoutResult(status -> f.clean()); }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void concurrentPublicationCreatesOnlyOneResultAndOneEmailPerStudent() throws Exception {
        var tx=new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        var f=tx.execute(status -> new Fixture("MON_HOC"));
        var executor=java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            sign(f.dean,"TRUONG_KHOA"); moCham.moCham(f.group);
            sign(f.marker,"GIANG_VIEN"); cham.nopPhieu(f.group,form("8.00")); tongHop.tongHop(f.group,null);
            java.util.concurrent.Callable<Boolean> publish=() -> {
                sign(f.dean,"TRUONG_KHOA");
                try { tongHop.congBo(f.group); return true; }
                catch (IllegalArgumentException ex) { return false; }
                finally { SecurityContextHolder.clearContext(); }
            };
            var a=executor.submit(publish); var b=executor.submit(publish);
            assertThat(java.util.List.of(a.get(15,java.util.concurrent.TimeUnit.SECONDS),b.get(15,java.util.concurrent.TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder(true,false);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM email_thong_bao_ket_qua WHERE ma_nhom=?",Integer.class,f.group)).isEqualTo(3);
        } finally { executor.shutdownNow(); tx.executeWithoutResult(status -> f.clean()); }
    }

    final class Fixture {
        final String p="E"+UUID.randomUUID().toString().replace("-","").substring(0,9);
        final String dean=p+"TK",marker=p+"PB",supervisor=p+"HD",round=p+"DOT",topic=p+"DT",group=p+"N",council=p+"CD";
        final String[] students={p+"S1",p+"S2",p+"S3"}, councilTeachers={p+"G1",p+"G2",p+"G3"};
        Fixture(String loai) {
            var now=LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            user(dean,"CAN_BO","TRUONG_KHOA"); user(marker,"GV","GIANG_VIEN"); user(supervisor,"GV","GIANG_VIEN");
            for (String sv:students) user(sv,"SV","SINH_VIEN");
            jdbc.update("INSERT INTO bo_mon(ma_bo_mon,ten_bo_mon) VALUES(?,?)",p+"BM","Bộ môn thử");
            jdbc.update("""
                INSERT INTO dot_dang_ky(ma_dot,ten_dot,loai_de_tai,bat_dau_gv,ket_thuc_gv,bat_dau_sv,ket_thuc_sv,
                    han_nop_bao_cao,han_nop_diem_pb,ngay_bao_cao_hd,trang_thai,ma_nguoi_tao)
                VALUES(?,?,?,?,?,?,?,?,?,?,'CONG_BO',?)
                """,round,"Đợt thử "+loai,loai,now.minusDays(20),now.minusDays(15),now.minusDays(12),now.plusDays(2),
                now.plusDays(7),loai.equals("TLCN")||loai.equals("KLTN")?now.plusDays(10):null,loai.equals("KLTN")?now.plusDays(12).toLocalDate():null,dean);
            jdbc.update("INSERT INTO de_tai(ma_de_tai,ma_dot,ma_bo_mon,ten_de_tai,noi_dung,ma_nguoi_de_xuat) VALUES(?,?,?,?,?,?)",topic,round,p+"BM","Đề tài thử "+loai,"Nội dung thử",supervisor);
            jdbc.update("INSERT INTO de_tai_giang_vien(ma_de_tai,ma_giang_vien,loai_phan_cong,ma_nguoi_phan_cong,phan_cong_luc) VALUES(?,?,'HUONG_DAN',?,?)",topic,supervisor,dean,now.minusDays(4));
            jdbc.update("INSERT INTO de_tai_giang_vien(ma_de_tai,ma_giang_vien,loai_phan_cong,ma_nguoi_phan_cong,phan_cong_luc) VALUES(?,?,?,?,?)",topic,marker,loai.equals("MON_HOC")||loai.equals("NCKH")?"CHAM_DOC_LAP":"PHAN_BIEN",dean,now.minusDays(4));
            jdbc.update("INSERT INTO nhom_sinh_vien(ma_nhom,ten_nhom,ma_dot,ma_nguoi_tao,trang_thai,chot_luc) VALUES(?,?,?,?,'DANG_THUC_HIEN',?)",group,"Nhóm thử "+loai,round,students[0],now.minusDays(5));
            for (int i=0;i<students.length;i++) jdbc.update("INSERT INTO thanh_vien_nhom(ma_nhom,ma_sinh_vien,trang_thai,la_nhom_truong,la_thanh_vien_chot,yeu_cau_luc,tham_gia_luc) VALUES(?,?,'THAM_GIA',?,1,?,?)",group,students[i],i==0,now.minusDays(6),now.minusDays(5));
            jdbc.update("INSERT INTO dang_ky_de_tai(ma_nhom,ma_de_tai,ma_nguoi_dang_ky,trang_thai,dang_ky_luc) VALUES(?,?,?,'DA_DUYET',?)",group,topic,students[0],now.minusDays(4));
            jdbc.update("INSERT INTO nop_bao_cao(ma_nhom,phien_ban,ma_nguoi_nop,ten_file_goc,object_key,mime_type,kich_thuoc_byte,sha256,nop_luc) VALUES(?,1,?,'test.pdf',?,'application/pdf',100,?,?)",group,students[0],p+"/report.pdf","0".repeat(64),now.minusDays(1));
            if (loai.equals("KLTN")) {
                jdbc.update("INSERT INTO hoi_dong(ma_hoi_dong,ten_hoi_dong,ma_dot,trang_thai,ma_nguoi_tao) VALUES(?,?,?,'SAN_SANG',?)",council,"Hội đồng thử",round,dean);
                String[] chucDanh={"CHU_TICH","THU_KY","UY_VIEN"};
                for (int i=0;i<councilTeachers.length;i++) { user(councilTeachers[i],"GV","GIANG_VIEN"); jdbc.update("INSERT INTO thanh_vien_hoi_dong(ma_hoi_dong,ma_giang_vien,chuc_danh) VALUES(?,?,?)",council,councilTeachers[i],chucDanh[i]); }
                jdbc.update("INSERT INTO hoi_dong_de_tai(ma_nhom,ma_hoi_dong,ma_nguoi_phan_cong,phan_cong_luc) VALUES(?,?,?,?)",group,council,dean,now.minusDays(1));
            }
        }
        void clean() {
            for (String table:new String[]{"email_thong_bao_ket_qua","ket_qua","phieu_cham_diem","hoi_dong_de_tai","nop_bao_cao","thanh_vien_nhom","dang_ky_de_tai","nhom_sinh_vien"})
                jdbc.update("DELETE FROM "+table+" WHERE ma_nhom=?",group);
            jdbc.update("DELETE FROM thanh_vien_hoi_dong WHERE ma_hoi_dong=?",council);
            jdbc.update("DELETE FROM hoi_dong WHERE ma_hoi_dong=?",council);
            jdbc.update("DELETE FROM de_tai_giang_vien WHERE ma_de_tai=?",topic);
            jdbc.update("DELETE FROM de_tai WHERE ma_de_tai=?",topic);
            jdbc.update("DELETE FROM dot_dang_ky WHERE ma_dot=?",round);
            jdbc.update("DELETE FROM bo_mon WHERE ma_bo_mon=?",p+"BM");
            jdbc.update("DELETE FROM nguoi_dung_vai_tro WHERE ma_nguoi_dung LIKE ?",p+"%");
            jdbc.update("DELETE FROM nguoi_dung WHERE ma_nguoi_dung LIKE ?",p+"%");
        }
        void user(String id,String type,String role) {
            jdbc.update("INSERT INTO nguoi_dung(ma_nguoi_dung,ho_ten,email,mat_khau_hash,loai_nguoi_dung,trang_thai) VALUES(?,?,?,'unused',?,'HOAT_DONG')",id,"Tài khoản thử "+id,id+"@example.test",type);
            jdbc.update("INSERT INTO nguoi_dung_vai_tro(ma_nguoi_dung,ma_vai_tro) VALUES(?,?)",id,role);
        }
    }
}
