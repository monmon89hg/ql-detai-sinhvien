package vn.edu.group12.qldt.service.hoidong;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.group12.qldt.dto.hoidong.*;
import vn.edu.group12.qldt.entity.hoidong.*;
import vn.edu.group12.qldt.enums.hoidong.*;
import vn.edu.group12.qldt.repository.hoidong.*;
import vn.edu.group12.qldt.service.auth.CurrentUser;

@Service
@Transactional(readOnly = true)
public class MoChamService {
    private final DanhGiaRepository danhGia;
    private final HoiDongRepository hoiDong;
    private final HoiDongDeTaiRepository phanCong;
    private final ThanhVienHoiDongRepository thanhVien;
    private final PhieuChamDiemRepository phieu;
    private final TinhDiemTrungBinhService tinhDiem;
    private final KetQuaRepository ketQua;

    public MoChamService(DanhGiaRepository danhGia, HoiDongRepository hoiDong,
            HoiDongDeTaiRepository phanCong, ThanhVienHoiDongRepository thanhVien,
            PhieuChamDiemRepository phieu, TinhDiemTrungBinhService tinhDiem, KetQuaRepository ketQua) {
        this.danhGia=danhGia; this.hoiDong=hoiDong; this.phanCong=phanCong;
        this.thanhVien=thanhVien; this.phieu=phieu; this.tinhDiem=tinhDiem; this.ketQua=ketQua;
    }

    public record ChiTiet(NhomKetQuaHienThi nhom, List<PhieuChamDiem> cacPhieu,
            List<PhieuChamYeuCau> phieuBatBuoc, KetQuaTinhDiem diemDuKien,
            String lyDoChuaDu, Integer phienBanBaoCao, boolean coQuyenTongHop,
            List<DanhGiaRepository.EmailHienThi> email) {}

    private boolean coVaiTro(String role) {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_"+role));
    }

    private void truongKhoa() {
        if (hoiDong.demTruongKhoaHoatDong(CurrentUser.maNguoiDung()) == 0)
            throw new AccessDeniedException("Chỉ Trưởng khoa đang hoạt động được thực hiện thao tác này.");
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void moCham(String maNhom) {
        truongKhoa();
        var pc=phanCong.findById(maNhom);
        HoiDong hd=pc.map(p -> hoiDong.timVaKhoaTheoMa(p.getMaHoiDong()).orElseThrow(() -> new IllegalArgumentException("Hội đồng không còn tồn tại."))).orElse(null);
        danhGia.khoaDauVao(maNhom);
        if (hd!=null && phanCong.timMaHoiDongHienTai(maNhom).filter(hd.getMaHoiDong()::equals).isEmpty())
            throw new IllegalArgumentException("Phân công hội đồng vừa thay đổi. Vui lòng tải lại trang.");
        var d=danhGia.dauVao(maNhom).orElseThrow(() -> new IllegalArgumentException("Nhóm chưa có đăng ký hợp lệ."));
        if (!java.util.Set.of("DA_DUYET","DANG_THUC_HIEN").contains(d.trangThaiDangKy()) || !"DANG_THUC_HIEN".equals(d.trangThaiNhom())
                || "NHAP".equals(d.trangThaiDot()))
            throw new IllegalArgumentException("Chỉ mở chấm khi nhóm và đăng ký đang thực hiện trong đợt đã công bố.");
        if (d.soThanhVien()<3 || d.soThanhVien()>5 || d.soThanhVien()!=d.soThanhVienChot() || d.phienBanBaoCao()==null)
            throw new IllegalArgumentException("Nhóm phải có 3–5 thành viên đã chốt và đã nộp báo cáo hợp lệ.");
        if (ketQua.existsById(maNhom) || !phieu.findById_MaNhomOrderById_MaGiangVienAsc(maNhom).isEmpty())
            throw new IllegalArgumentException("Nhóm đã có dữ liệu chấm hoặc kết quả.");
        if ("KLTN".equals(d.loaiDeTai())) {
            if (hd==null || !hd.getMaDot().equals(d.maDot()) || (hd.getTrangThai()!=TrangThaiHoiDong.SAN_SANG && hd.getTrangThai()!=TrangThaiHoiDong.DANG_CHAM))
                throw new IllegalArgumentException("Khóa luận phải được giao cho hội đồng sẵn sàng hoặc đang chấm.");
            kiemTraThanhVien(hd.getMaHoiDong());
        } else if (hd!=null) throw new IllegalArgumentException("Loại đề tài này không chấm qua hội đồng.");
        var yeuCau=phieu.layPhieuChamYeuCau(maNhom);
        var daGap=new HashSet<String>();
        if (yeuCau.stream().filter(p -> !"HOI_DONG".equals(p.getLoaiPhieu())).count()!=1)
            throw new IllegalArgumentException("Phải phân công đúng một giảng viên phản biện hoặc chấm độc lập phù hợp loại đề tài.");
        for (var p:yeuCau) {
            if (!daGap.add(p.getMaGiangVien()) || thanhVien.demGiangVienHopLe(p.getMaGiangVien())==0
                    || phieu.demPhanCongHuongDan(maNhom,p.getMaGiangVien())>0)
                throw new IllegalArgumentException("Phân công người chấm bị trùng, không còn hoạt động hoặc xung đột hướng dẫn.");
            if ("PHAN_BIEN".equals(p.getLoaiPhieu()) && (d.hanPhanBien()==null
                    || LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).isAfter(d.hanPhanBien())))
                throw new IllegalArgumentException("Đã hết hạn nộp điểm phản biện của đợt.");
        }
        if (danhGia.moCham(maNhom,LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))!=1) throw new IllegalArgumentException("Trạng thái đăng ký vừa thay đổi.");
        if (hd!=null) { hd.setTrangThai(TrangThaiHoiDong.DANG_CHAM); hoiDong.saveAndFlush(hd); }
    }

    private void kiemTraThanhVien(String maHoiDong) {
        var tv=thanhVien.findById_MaHoiDongOrderById_MaGiangVienAsc(maHoiDong);
        if (tv.size()<3 || tv.size()>5
                || tv.stream().filter(t -> t.getChucDanh()==ChucDanhHoiDong.CHU_TICH).count()!=1
                || tv.stream().filter(t -> t.getChucDanh()==ChucDanhHoiDong.THU_KY).count()!=1)
            throw new IllegalArgumentException("Hội đồng phải có 3–5 thành viên, một chủ tịch và một thư ký.");
        for (var t:tv) if (thanhVien.demGiangVienHopLe(t.getId().getMaGiangVien())==0
                || thanhVien.demXungDotHuongDan(maHoiDong,t.getId().getMaGiangVien())>0)
            throw new IllegalArgumentException("Hội đồng có giảng viên không hợp lệ hoặc xung đột phân công.");
    }

    @PreAuthorize("hasAnyRole('GIANG_VIEN','TRUONG_KHOA','ADMIN')")
    public ChiTiet chiTiet(String maNhom) {
        boolean quanLy=coVaiTro("TRUONG_KHOA") || coVaiTro("ADMIN");
        if (coVaiTro("TRUONG_KHOA")) truongKhoa();
        if (!quanLy && thanhVien.demGiangVienHopLe(CurrentUser.maNguoiDung())==0)
            throw new AccessDeniedException("Tài khoản không còn quyền xem kết quả.");
        var ds=quanLy ? phanCong.layDanhSachNhomChoTruongKhoa()
            : phanCong.layDanhSachNhomChoChuTich(CurrentUser.maNguoiDung());
        var nhom=ds.stream().filter(n -> maNhom.equals(n.getMaNhom())).findFirst()
            .orElseThrow(() -> new AccessDeniedException("Nhóm không thuộc phạm vi tổng hợp hoặc quản lý của bạn."));
        KetQuaTinhDiem diem=null;
        String loi=null;
        try { diem=tinhDiem.tinhDiem(maNhom); } catch (IllegalArgumentException ex) { loi=ex.getMessage(); }
        return new ChiTiet(nhom,phieu.findById_MaNhomOrderById_MaGiangVienAsc(maNhom),
            phieu.layPhieuChamYeuCau(maNhom),diem,loi,danhGia.dauVao(maNhom).orElseThrow(() -> new IllegalArgumentException("Nhóm chưa có đăng ký hợp lệ.")).phienBanBaoCao(),
            !quanLy && "DANG_CHAM".equals(nhom.getTrangThaiDangKy()) && nhom.getTrangThaiKetQua()==null,
            quanLy ? danhGia.emailTheoNhom(maNhom) : List.of());
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void suaLaiHoiDong(String maHoiDong) {
        truongKhoa();
        var hd=hoiDong.timVaKhoaTheoMa(maHoiDong).orElseThrow(() -> new IllegalArgumentException("Hội đồng không tồn tại."));
        if (hd.getTrangThai()!=TrangThaiHoiDong.SAN_SANG || danhGia.daCoDuLieuCham(maHoiDong))
            throw new IllegalArgumentException("Chỉ đưa hội đồng sẵn sàng về nháp khi chưa mở chấm.");
        hd.setTrangThai(TrangThaiHoiDong.NHAP); hoiDong.saveAndFlush(hd);
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void khoaHoiDong(String maHoiDong) {
        truongKhoa();
        var hd=hoiDong.timVaKhoaTheoMa(maHoiDong).orElseThrow(() -> new IllegalArgumentException("Hội đồng không tồn tại."));
        if (hd.getTrangThai()!=TrangThaiHoiDong.DANG_CHAM || !danhGia.tatCaDaCongBo(maHoiDong))
            throw new IllegalArgumentException("Chỉ khóa hội đồng đang chấm khi tất cả nhóm đã được công bố kết quả.");
        hd.setTrangThai(TrangThaiHoiDong.DA_KHOA); hoiDong.saveAndFlush(hd);
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void thuLaiEmail(String maNhom, String maSinhVien) {
        truongKhoa();
        if (ketQua.findById(maNhom).map(k -> k.getTrangThai()!=TrangThaiKetQua.CONG_BO).orElse(true)
                || danhGia.thuLaiEmail(maNhom,maSinhVien)!=1)
            throw new IllegalArgumentException("Chỉ thử lại thư gửi lỗi của kết quả đã công bố.");
    }
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public java.util.Map<String,List<DanhGiaRepository.HoiDongThayThe>> luaChonDoiHoiDong(String maHoiDong) {
        truongKhoa();
        var ds=new java.util.LinkedHashMap<String,List<DanhGiaRepository.HoiDongThayThe>>();
        for (String maNhom:danhGia.nhomCuaHoiDong(maHoiDong))
            ds.put(maNhom,danhGia.hoiDongThayThe(maNhom).stream().filter(h -> !maHoiDong.equals(h.maHoiDong())).toList());
        return ds;
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void doiHoiDong(String maCu, String maNhom, String maMoi) {
        truongKhoa();
        if (maMoi==null || maMoi.isBlank() || maCu.equals(maMoi))
            throw new IllegalArgumentException("Hãy chọn hội đồng thay thế.");
        var daKhoa=new java.util.HashMap<String,HoiDong>();
        java.util.stream.Stream.of(maCu,maMoi).sorted().forEach(ma -> daKhoa.put(ma,
            hoiDong.timVaKhoaTheoMa(ma).orElseThrow(() -> new IllegalArgumentException("Hội đồng không tồn tại."))));
        danhGia.khoaDauVao(maNhom);
        if (phanCong.timMaHoiDongHienTai(maNhom).filter(maCu::equals).isEmpty()
                || daKhoa.get(maCu).getTrangThai()==TrangThaiHoiDong.DANG_CHAM
                || daKhoa.get(maCu).getTrangThai()==TrangThaiHoiDong.DA_KHOA
                || java.util.Set.of("DANG_CHAM","HOAN_THANH").contains(danhGia.dauVao(maNhom).orElseThrow(() -> new IllegalArgumentException("Nhóm chưa có đăng ký hợp lệ.")).trangThaiDangKy())
                || !phieu.findById_MaNhomOrderById_MaGiangVienAsc(maNhom).isEmpty() || ketQua.existsById(maNhom))
            throw new IllegalArgumentException("Phân công đã thay đổi hoặc đã khóa chấm.");
        if (danhGia.hoiDongThayThe(maNhom).stream().noneMatch(h -> maMoi.equals(h.maHoiDong())))
            throw new IllegalArgumentException("Hội đồng mới phải sẵn sàng, cùng đợt và không xung đột phân công.");
        kiemTraThanhVien(maMoi);
        var pc=phanCong.findById(maNhom).orElseThrow(() -> new IllegalArgumentException("Phân công không còn tồn tại."));
        pc.setMaHoiDong(maMoi); pc.setMaNguoiPhanCong(CurrentUser.maNguoiDung());
        pc.setPhanCongLuc(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        phanCong.saveAndFlush(pc);
    }

    @PreAuthorize("hasAnyRole('GIANG_VIEN','TRUONG_KHOA','ADMIN')")
    public List<DotDangKyLuaChon> danhSachDot() { return hoiDong.layDanhSachDotMoiNhatTruoc(); }

}
