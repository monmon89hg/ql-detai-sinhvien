# Bàn giao — Complete Demo Dataset / CSDL 2.0

Ngày thực hiện: **20/10/2026**. Repo: `monmon89hg/ql-detai-sinhvien`.
Nhánh duy nhất: `chore/add-demo-seed-data`; base `develop`.
PR hiện có: [#16](https://github.com/monmon89hg/ql-detai-sinhvien/pull/16), giữ **Draft**, không merge.

**Kết luận:** bộ dữ liệu QA local đủ 17/17 bảng; **30 PASS, 0 FAIL, 6 SKIP** trên MySQL **8.4.13**.
Kiểm tra snapshot gồm **89 checks, 0 FAIL**, trong đó có 17 count, mọi FK thật và đối chiếu PDF.
Chưa xác nhận luồng website hoạt động. Không deploy, không kết nối hay seed Railway.

## 1. Audit và phạm vi thay đổi

- Checkout ban đầu sạch, đúng nhánh và head `4897835a58e8b48fa656f4c8bcca14cf8b68c124`.
- PR #16 open, Draft, base `develop`; metadata repository báo push=true,
  nhưng thao tác ghi thực tế bị integration từ chối 403 (chi tiết mục 9).
- Đã đọc toàn bộ V1–V5, toàn bộ seed v1, README, cấu trúc Java/resources.
- Đã đọc `CSDL 2.0.pdf` (21 trang) và `DAC_TA_YEU_CAU_FR_NFR_NHOM12.md` (bản 11/10/2026)
  trong tài liệu dự án. Chúng không nằm trong repo; không thay bằng đặc tả cũ.
- Giữ business PK, composite PK, generated columns, FK/CHECK/UNIQUE; không sửa V1–V5.
- Chỉ thay đổi `database/seed/`, không triển khai tính năng của TV1/TV2/TV4 hoặc module TV3.

| File | Thay đổi / mục đích |
|---|---|
| `demo_data.sql` | Mở rộng seed v1 thành snapshot nhất quán 17 bảng, giữ mã/hash cũ; chỉ INSERT khi chưa có PK; gate schema/PDF và transaction |
| `validate_demo_data.sql` | Mới; truy vấn chỉ đọc cho count và các invariant nghiệp vụ |
| `demo_seed.py` | Mới; helper local-only, preflight, advisory lock, validate trước commit, rollback và test thật trên MySQL |
| `fixture_tools.py` | Mới; tạo PDF xác định, kiểm tra parser/size/SHA-256, provision kho fixture local riêng |
| `requirements.txt` | Mới; pin PyMySQL 1.2.3, ReportLab 4.4.9, pypdf 6.10.0 đã dùng |
| `.gitignore` | Mới; bỏ qua venv và Python cache trong seed folder |
| `README.md` | Mới; cách seed, kiểm tra, chạy lại, backup, xử lý lỗi, provisioning tài khoản và điều kiện trước Railway |
| `HANDOFF_VI.md` | Mới; báo cáo này |
| `test_results.json` | Mới; output thực tế 30 PASS/6 SKIP, counts và phiên bản DB |
| `PR_DESCRIPTION_VI.md` | Mới; mô tả sẵn để chủ repo cập nhật PR #16 sau khi push bundle |
| `fixtures/manifest.json` | Mới; metadata sáu PDF và SHA-256 thật |
| `fixtures/DEMO_N02-v1.pdf` | Mới; báo cáo nhóm đang thực hiện |
| `fixtures/DEMO_N08-v1.pdf` | Mới; phiên bản 1 của hồ sơ lịch sử |
| `fixtures/DEMO_N08-v2.pdf` | Mới; bản sửa trước khi mở chấm |
| `fixtures/DEMO_N09-v1.pdf` | Mới; hồ sơ đã tổng hợp, chưa công bố |
| `fixtures/DEMO_N10-v1.pdf` | Mới; hồ sơ còn phiếu nháp |
| `fixtures/DEMO_N11-v1.pdf` | Mới; hồ sơ chấm độc lập |

## 2. Số dòng chính xác

Count dưới đây là **dữ liệu DEMO_**; riêng `vai_tro` là đúng năm hàng đã có từ V5.
Tổng **262 hàng** = 257 demo + 5 role; seed không INSERT thêm vào `vai_tro`.
Các hàng `KEEP_*` đối chứng trong schema test không tính vào bảng này.

| # | Bảng | Số dòng | Đại diện |
|---|---|---:|---|
| 1 | `bo_mon` | 4 | Ba bộ môn hoạt động, `DEMO_OLD` ngừng hoạt động |
| 2 | `nguoi_dung` | 47 | 2 cán bộ, 6 GV, 39 SV; tất cả khóa, email .test |
| 3 | `vai_tro` | 5 | Giữ nguyên năm vai trò V5 |
| 4 | `nguoi_dung_vai_tro` | 48 | `DEMO_GV01` mang GIANG_VIEN và TRUONG_BO_MON |
| 5 | `thong_bao` | 4 | CONG_BO, NHAP, AN; các phạm vi người đọc |
| 6 | `dot_dang_ky` | 7 | CONG_BO, DONG, NHAP; đủ bốn loại đề tài |
| 7 | `de_tai` | 17 | NHAP, CHO_DUYET, DA_DUYET, CONG_BO, TU_CHOI |
| 8 | `de_tai_giang_vien` | 27 | HUONG_DAN, PHAN_BIEN, CHAM_DOC_LAP |
| 9 | `nhom_sinh_vien` | 11 | DANG_LAP, DA_CHOT, DANG_THUC_HIEN, HOAN_THANH |
| 10 | `thanh_vien_nhom` | 38 | THAM_GIA, CHO_DUYET, TU_CHOI, KET_THUC |
| 11 | `dang_ky_de_tai` | 9 | CHO_DUYET, DA_DUYET, TU_CHOI, DANG_THUC_HIEN, DANG_CHAM, HOAN_THANH |
| 12 | `nop_bao_cao` | 6 | File thật, có lịch sử hai phiên bản N08 |
| 13 | `hoi_dong` | 5 | NHAP, SAN_SANG, DANG_CHAM, DA_KHOA |
| 14 | `hoi_dong_de_tai` | 4 | Gán N04/N08/N09/N10 vào đúng hội đồng/đợt |
| 15 | `thanh_vien_hoi_dong` | 14 | Chủ tịch, thư ký, ủy viên; HĐ đủ điều kiện có 3–4 GV |
| 16 | `phieu_cham_diem` | 14 | 11 DA_NOP, 3 NHAP; đủ ba loại phiếu |
| 17 | `ket_qua` | 2 | N08 CONG_BO; N09 DA_TONG_HOP |

## 3. Ma trận kịch bản

**Toàn bộ hàng dưới đây database-ready cho QA local.** Cột website ghi module còn thiếu ở source hiện tại;
tất cả còn cần authentication/provisioning của TV1. Kỳ vọng là tiêu chí cho lúc module được tích hợp,
không phải thao tác website đã được thực hiện.

| Kịch bản | UC | Bảng liên quan | Mã demo / dữ liệu | Kỳ vọng khi có module | Website hiện tại |
|---|---|---|---|---|---|
| A — Nhóm mới | 11–13 | nhom_sinh_vien, thanh_vien_nhom | N01: SV01 trưởng, SV02 thành viên, SV03 chờ, SV16 từng bị từ chối; tất cả có prefix DEMO_ | Hai người chính thức; yêu cầu chờ chưa chiếm suất; chưa được đăng ký | BLOCKED: TV3 nhóm/thành viên |
| B — Sẵn sàng đăng ký | 10,14 | dot_dang_ky, de_tai, nhom_sinh_vien, thanh_vien_nhom | DEMO_N06, SV18–20, DEMO_DT10, DEMO_DOT_LATE26 | Đúng ba người, một trưởng; trưởng có thể gửi đăng ký trong cửa sổ tới 31/10 | BLOCKED: TV2 tra cứu + TV3 đăng ký |
| C — Chờ duyệt đăng ký | 14–15 | dang_ky_de_tai, nhom_sinh_vien, thanh_vien_nhom, de_tai | DEMO_N03 → DEMO_DT05; DEMO_CB02 là tác nhân trưởng khoa | Chỉ người có quyền duyệt/từ chối; nhóm chưa chốt snapshot | BLOCKED: TV3 + phối hợp TV2 |
| D — Đã duyệt / thực hiện | 15 | dang_ky_de_tai, nhom_sinh_vien, thanh_vien_nhom, de_tai_giang_vien | DEMO_N07 → DT11: DA_DUYET, 5 thành viên chốt; DEMO_N02 → DT01: DANG_THUC_HIEN; DEMO_N04 → DT07 | Danh sách bất biến sau duyệt; có GVHD hợp lệ; chưa tự coi là đã chấm | BLOCKED: TV3 + TV2 |
| E — Đăng ký bị từ chối | 15 | dang_ky_de_tai, nhom_sinh_vien | DEMO_N05 → DEMO_DT02, lý do thiếu giải trình yêu cầu | Hiển thị lý do/mốc quyết định; không tự cho tạo đăng ký thứ hai | BLOCKED: TV3; chính sách sửa lại cần nhóm chốt |
| F — PDF và phiên bản | 16–17 | nop_bao_cao, dang_ky_de_tai, thanh_vien_nhom | DEMO_N02 v1; DEMO_N08 v1/v2; N09/N10/N11 v1 | N02 có thể nộp tiếp trước hạn khi module sẵn sàng; N08 chỉ xem lịch sử đã đóng băng, chọn v2 để chấm | BLOCKED: TV3 upload/download + storage; file thật chỉ có trong kho QA |
| G — Hội đồng | 18–19 | hoi_dong, hoi_dong_de_tai, thanh_vien_hoi_dong, de_tai_giang_vien | HD01 cho N08 (DA_KHOA); HD02 cho N09; HD03 cho N10 (DANG_CHAM); HD04 cho N04 (SAN_SANG); HD05 NHAP | Đúng đợt KLTN; chủ tịch/thư ký riêng; không có GVHD/GVPB trong HĐ | BLOCKED: TV4 + nguồn phân công TV2 |
| H — Nhập/chốt điểm | 20,24 | phieu_cham_diem, de_tai_giang_vien, thanh_vien_hoi_dong | N10: PB 9, HĐ 8 và 10 đã nộp, nháp 0 và NULL; N11 chấm độc lập nháp 8.5 | 0 khác NULL; nháp không tính là DA_NOP; N10 chưa đủ điều kiện tổng hợp | BLOCKED: TV4; không mô phỏng Service bằng SQL validator |
| I — Kết quả công bố | 21–25 | ket_qua, phieu_cham_diem, dang_ky_de_tai, nhom_sinh_vien, thanh_vien_nhom | DEMO_N08; PB GV02=8; HĐ GV03=7.5, GV04=8.5, GV05=9 | 4 phiếu, tổng 33.00, TB 8.25; chủ tịch GV03 tổng hợp, CB02 công bố; nhóm hoàn thành, membership kết thúc và giữ snapshot | BLOCKED: TV4 + TV1 quyền; SMTP chưa có, chưa gửi email |
| I2 — Tổng hợp chưa công bố | 21–24 | ket_qua, phieu_cham_diem, hoi_dong_de_tai | DEMO_N09; 8.33 + 7.11 + 9.02 + 8.87 = 33.33 | 4 phiếu, TB làm tròn 8.33; chưa hiển thị cho SV, nhóm/membership còn hoạt động | BLOCKED: TV4 |
| J — Vai trò và tài khoản | 1–3 | nguoi_dung, vai_tro, nguoi_dung_vai_tro | CB01 ADMIN, CB02 TRUONG_KHOA, GV01 đa vai trò, SV38–39 chưa vào nhóm | Tài khoản khóa bị từ chối; vai trò đối tượng không biến thành role toàn cục | BLOCKED: TV1; không có mật khẩu demo chung |
| K — Danh mục | 4 | bo_mon, nguoi_dung, de_tai | DEMO_CNPM, DEMO_HTTT, DEMO_KHMT; DEMO_OLD inactive | Bộ môn ngừng hoạt động không được chọn cho dữ liệu mới | BLOCKED: TV1 |
| L — Thông báo | 5 | thong_bao, nguoi_dung_vai_tro, dot_dang_ky | TB01/TB02 CONG_BO, TB03 NHAP, TB04 AN | Chỉ hiện nội dung đúng phạm vi/quyền, không lộ nháp/ẩn | BLOCKED: TV1 |
| M — Đợt và đề tài | 6–10 | dot_dang_ky, de_tai, de_tai_giang_vien | DT03 NHAP, DT06 CHO_DUYET, DT12 DA_DUYET, DT13 TU_CHOI; DOT_NEXT27 NHAP TLCN | Không cho SV chọn đề tài chưa công bố; đủ GVHD trước duyệt/công bố; không mở đợt nháp | BLOCKED: TV2 |

Khi dùng mã ngắn trong bảng, tiền tố luôn là `DEMO_`. Các mã này không phải tài khoản hay danh tính thật.

## 4. Kiểm thử đã chạy và cách tái lập

Môi trường: Linux x86_64, Oracle MySQL Community Server **8.4.13**, Python 3.12,
PyMySQL 1.2.3, ReportLab 4.4.9, pypdf 6.10.0. MySQL bind loopback trong sandbox riêng;
không sử dụng database hay credentials Railway.

Lệnh đã thực hiện:

```bash
python3 database/seed/fixture_tools.py generate
python3 database/seed/fixture_tools.py provision --fixture-root /workspace/scratch/69e9c48ec265/private-fixture-store-final
python3 -m py_compile database/seed/fixture_tools.py database/seed/demo_seed.py
python3 /workspace/scratch/69e9c48ec265/tmp/run_local_qa.py
```

Wrapper cuối chỉ khởi động/tắt MySQL cục bộ trong cùng sandbox mạng rồi chạy **lệnh test có thể tái lập**:

```bash
python3 database/seed/demo_seed.py test --port 33060 --user root --fixture-root /workspace/scratch/69e9c48ec265/private-fixture-store-final --report database/seed/test_results.json
```

Trên máy khác thay port/user/root fixture, helper tự hỏi mật khẩu.
Wrapper theo môi trường scratch là công cụ trung gian, không phải dependency của repo.
SQL V1–V5 được thực thi trực tiếp trên schema mới; không ghi lịch sử Flyway giả.

Kết quả thực tế đầy đủ tại [test_results.json](test_results.json): **30 PASS, 0 FAIL, 6 SKIP**.

| Nhóm kiểm thử thực thi | Kết quả |
|---|---|
| V1–V5 tạo 17 bảng, seed mới, count đủ 17 bảng, 89 check dữ liệu/FK/PDF | PASS |
| Không opt-in schema; sai schema; helper chặn remote trước kết nối | PASS |
| Chạy seed lần hai: fingerprint cả 17 bảng không đổi | PASS |
| Dữ liệu ngoài demo và năm role V5 không đổi | PASS |
| Giữ tên/version demo do chủ dữ liệu đã chỉnh khi seed lại | PASS |
| Sai FK (1452), trùng membership PK (1062), hai nhóm hoạt động cho một SV (1062) | PASS |
| Hai trưởng nhóm / hai chủ tịch bị generated UNIQUE chặn (1062) | PASS |
| Điểm -0.01 / 10.01, sai công thức nội bộ kết quả bị CHECK chặn (3819) | PASS |
| Tổng nội bộ đúng nhưng lệch phiếu thật: validator phát hiện, rollback | PASS |
| Thiếu phiếu; hoặc cùng số phiếu/tổng điểm nhưng sai danh tính giảng viên: phát hiện | PASS |
| Sai loại phân công; GVHD xuất hiện trong hội đồng: phát hiện | PASS |
| Thêm người thứ sáu: DB cho phép, SQL validator phát hiện; rollback | PASS ở QA validator, không phải PASS Service |
| Sáu PDF có parser/size/checksum/metadata đúng; tái sinh byte không đổi | PASS |
| PDF bị sửa byte; provision đè object khác nội dung: từ chối | PASS |
| Trùng email ở giữa seed: rollback mọi hàng mới, giữ nguyên dữ liệu cũ | PASS |
| Seed lên schema đã nạp nguyên bản v1: báo sai mốc thời gian, rollback, không ghi đè | PASS |
| Sau mọi negative test: fingerprint khôi phục và toàn bộ invariant còn hợp lệ | PASS |

Các negative test chỉ chạy trong transaction rồi rollback. Chúng không để lại hàng sai trong DB.
Các schema test được giữ lại; tên cụ thể nằm trong JSON. Không có lệnh dọn dữ liệu phá hủy.

**Sáu SKIP được ghi đúng trạng thái:**

| Test chưa thực thi | Lý do |
|---|---|
| Service chặn thành viên thứ sáu | Chưa có Membership Service/Controller; validator không thay thế khóa và transaction nghiệp vụ |
| HTTP tải báo cáo không có quyền | Chưa có authentication, report Controller hoặc private application storage |
| Website đi hết A–I sau đăng nhập | Chưa có các Controller/Service; account demo khóa theo yêu cầu |
| Đồng thời gia nhập/cấp phiên bản/chấm/công bố | Chưa có transaction Services để gửi yêu cầu thật |
| Flyway engine + Spring Boot startup | Runner kiểm thử SQL trực tiếp; JDK môi trường này 17, project yêu cầu 21; không chạy Maven/Spring để giả PASS |
| Gửi email kết quả | Chưa có mail service/SMTP; không gửi thư |

Trong quá trình hoàn thiện PDF có nhúng font, một lượt QA phát hiện metadata cũ còn ở dòng SELECT đầu
và chặn commit dữ liệu. Đã đồng bộ lại số byte/SHA-256/manifest gate; lượt chạy cuối nêu trên đạt 30 PASS, 0 FAIL.
Đã render sáu PDF để kiểm tra bố cục; parser kiểm tra từng file.
`git diff --check` và kiểm tra migration không thay đổi đã thực hiện.

## 5. Sai khác nghiệp vụ/schema và cách xử lý

1. **Nhóm hoàn thành không còn trưởng đang hoạt động.** CSDL 2.0 và FR-22 yêu cầu membership KET_THUC
   để giải phóng SV. Vì vậy N08 có 0 membership hoạt động, đúng một trưởng lịch sử với snapshot;
   nhóm đang hoạt động/đã duyệt vẫn có đúng một trưởng THAM_GIA. Không giữ trưởng hoạt động sau công bố
   chỉ để thỏa câu kiểm thử mô tả chưa khớp vòng đời.
2. **V1 có timestamp chưa đúng thứ tự nghiệp vụ.** Bản nguồn mới sửa mốc phân công GVHD trước duyệt,
   tạo membership trưởng cùng thời điểm tạo nhóm, và đặt yêu cầu bị từ chối của SV16 trước lần vào N05.
   Mã/hàng và hash v1 được giữ. Các hàng đã tồn tại không bị UPDATE. Test với v1 thật chứng minh
   seed từ chối upgrade không nhất quán và rollback phần mới; dùng schema demo mới hoặc xin duyệt sửa hàng cụ thể.
3. **DB không thay thế Service.** UNIQUE bảo vệ tối đa một trưởng, một nhóm hoạt động/SV và chức danh;
   không bảo vệ tối thiểu trưởng, sức chứa 5, đúng đợt, xung đột chấm, tập phiếu và tổng liên bảng.
   Validator kiểm tra các điều này cho fixture; Service tương lai vẫn phải khóa dòng và kiểm tra trong transaction.
4. **Tài khoản khóa có chủ đích.** Các liên kết phân công có đúng hồ sơ/vai trò của snapshot;
   không khẳng định account KHOA có quyền thao tác. Service vẫn phải chặn account khóa.
5. **Đặc tả chưa chốt một số policy.** Không tự thêm đăng ký thứ hai sau từ chối; chỉ tạo hội đồng KLTN;
   kết quả tổng hợp/công bố chỉ dùng KLTN, đúng chủ tịch/trưởng khoa. Chưa áp chính sách tổng hợp khác.

## 6. PDF thật và điều kiện storage

| File | Byte | Nhóm / phiên bản |
|---|---:|---|
| DEMO_N02-v1.pdf | 66001 | N02 / 1 |
| DEMO_N08-v1.pdf | 66014 | N08 / 1 |
| DEMO_N08-v2.pdf | 66027 | N08 / 2 |
| DEMO_N09-v1.pdf | 66016 | N09 / 1 |
| DEMO_N10-v1.pdf | 66029 | N10 / 1 |
| DEMO_N11-v1.pdf | 66017 | N11 / 1 |

Mỗi file dưới 20 MiB, đúng PDF, không mã hóa, một trang; SHA-256 đầy đủ trong manifest và SQL,
được đối chiếu với byte thực mỗi lần seed/validate. Key duy nhất dạng
`demo-fixtures/v1/DEMO_N08/v2.pdf` được giải dưới private fixture root local.

**17/17 là coverage DB QA local sau provision thật.** Hiện chưa thể coi các key này là object tồn tại
trong storage ứng dụng/Railway. Chưa có endpoint tải xuống để khẳng định download hay authorization hoạt động.
Hướng dẫn tích hợp từng bước và gate trước import cloud nằm trong README.

## 7. Thời gian demo và các giới hạn sẽ hết hạn

- `DEMO_DOT_LATE26`: SV đăng ký từ **11/10 đến 31/10/2026 23:59:59** theo giờ VN;
  N06/DT10 sẵn sàng dùng cho demo cuối tháng 10. Hạn PDF 15/11/2026 23:59:59.
- Giữ đợt cũ: MH26 hết cửa sổ SV ngày **28/10**, NC26 ngày **29/10**; KL26 đã hết đăng ký 20/09,
  nhưng công việc/report theo hạn riêng tiếp tục. Hội đồng HD04 của KL26 gắn ngày bảo vệ 20/12;
  không được mở chấm hội đồng đó trong tháng 10.
- KLH26/MHH26 là lịch sử: hết đăng ký 10/08, hạn báo cáo 10/09; KLH26 phản biện trước 15/09,
  bảo vệ ngày 20/09, N08 công bố 21/09. Không mở lại nộp báo cáo cho hồ sơ đã chấm/hoàn thành.
- Dùng ngày cố định để tái lập; không đổi đồng hồ hệ thống. Sau 31/10 cần một đợt/kịch bản mới được duyệt,
  không kéo dài âm thầm các hàng đã được dùng trong demo.

## 8. Website còn thiếu và việc chuẩn bị Railway

Source Java hiện chỉ có lớp bootstrap, chưa có Entity/Repository/Service/Controller nghiệp vụ.
TV1 còn auth/quyền/tài khoản/danh mục/thông báo; TV2 còn đợt/đề tài/phân công;
TV3 còn nhóm/đăng ký/upload/download; TV4 còn hội đồng/phiếu/tổng hợp/công bố/mail.

Trước một task import Railway riêng: hoàn thiện module và private persistent storage;
thống nhất adapter object key; provision sáu file đúng key và đọc lại xác minh checksum;
kiểm thử download có quyền; tạo staging, backup/restore, kiểm tra V1–V5 và xung đột dữ liệu;
thử seed/rollback trên staging; được chủ dự án cho phép cụ thể rồi mới chuẩn bị import.
Helper hiện tại không có đường chạy remote và không được sửa host/cờ để vượt giới hạn này.

Quy trình TV1 kích hoạt một số ít account bằng reset một lần/mật khẩu riêng, đổi mật khẩu bắt buộc,
mở khóa đúng mã và kiểm thử quyền nằm trong README. Task này không kích hoạt account nào.

## 9. Git, quyền ghi và cách chủ repo nhận commit

Commit triển khai đã tạo local: **`11b5cf91cd8682e3a9d462905c250bc97acc14aa`**.
Một commit tài liệu tiếp theo ghi nhận blocker quyền và hướng dẫn này; SHA head cuối được báo kèm bundle.

**Chưa push và chưa sửa được mô tả PR.** `git push origin chore/add-demo-seed-data` thất bại vì
Git CLI không có thông tin đăng nhập. Kết nối GitHub có thể đọc repo/PR nhưng trả
`403 Resource not accessible by integration` cho cả Create Git Blob và Update Pull Request.
Không cố ghi qua nhánh khác hoặc tìm credential. PR #16 vẫn Draft, head cũ
`4897835a58e8b48fa656f4c8bcca14cf8b68c124` tại thời điểm bàn giao.

Git bundle đi kèm chứa commit mới và các object cần thiết; prerequisite là commit v1 đã có trên nhánh remote.
Chủ repo đặt bundle cạnh thư mục clone, kiểm tra working tree sạch, rồi chạy:

```bash
git status
git fetch origin
git switch chore/add-demo-seed-data
git merge --ff-only origin/chore/add-demo-seed-data
git bundle verify ../ql-detai-demo-dataset-v2.bundle
git fetch ../ql-detai-demo-dataset-v2.bundle chore/add-demo-seed-data
git merge --ff-only FETCH_HEAD
git push origin chore/add-demo-seed-data
gh pr edit 16 --title "test(db): hoàn thiện dữ liệu demo 17 bảng CSDL 2.0" --body-file database/seed/PR_DESCRIPTION_VI.md
gh pr view 16 --json isDraft,headRefName,baseRefName
```

Hai lệnh `gh` cần GitHub CLI đã đăng nhập; nếu chưa có thì sửa mô tả PR trên GitHub bằng nội dung
`PR_DESCRIPTION_VI.md` sau khi push. Không chuyển Ready for review và không merge.
Nếu fast-forward bị từ chối do nhánh đã có commit khác, dừng để tích hợp có kiểm tra; không force push/reset dữ liệu của chủ repo.
Không tạo PR khác, không sửa nhánh develop/main hoặc lịch sử đã phát hành.

## 10. Các quyết định còn cần chủ dự án / nhóm

- Chọn private storage/adapter thật trước khi import báo cáo vào DB ứng dụng hoặc Railway.
- Nếu muốn giữ một DB đã nạp seed v1: duyệt thay đổi timestamp từng hàng sau backup, hoặc dùng schema demo mới.
  Không cần quyết định này để kiểm thử snapshot v2 trên schema QA mới.
- Chốt cách xử lý đăng ký bị từ chối, quyền tổng hợp ngoài KLTN và phạm vi hội đồng ngoài KLTN trước khi code các luồng đó.
- Sau khi TV1 hoàn thiện auth, duyệt danh sách account cần kích hoạt và quy trình cấp/reset riêng.

Các điểm trên không bị tự quyết bằng việc mở khóa tài khoản, bỏ phân quyền, sửa schema hoặc ghi vào Railway.
