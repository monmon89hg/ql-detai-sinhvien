# 1 Phạm vi và quy ước

Tài liệu đặc tả 15 Use Case cốt lõi của Hệ thống quản lý đề tài sinh viên Khoa CNTT, từ chuẩn bị đợt đăng ký, hình thành nhóm và nộp báo cáo đến chấm điểm, công bố và tra cứu kết quả.

Nhóm trưởng, giảng viên hướng dẫn, người phản biện và chủ tịch hội đồng là quyền gắn với nhóm, đề tài hoặc hội đồng cụ thể. Trưởng bộ môn thực hiện đề xuất theo quyền giảng viên được cấp.

Main Flow mô tả luồng chính; AF là luồng thay thế hợp lệ; EF là luồng ngoại lệ. Mã UC theo danh mục nền tảng; mã FR và NFR tham chiếu tài liệu FR_NFR_Nhom12.

# 2 Danh mục Use Case

| **Mã UC** | **Tên Use Case** | **Actor chính** | **FR liên quan** |
| --- | --- | --- | --- |
| UC01 | Đăng nhập và đăng xuất | Người dùng | FR-01 |
| UC06 | Tạo và công bố đợt đăng ký | Trưởng khoa | FR-06 |
| UC07 | Đề xuất, sửa và gửi duyệt đề tài | Giảng viên | FR-07 |
| UC08 | Duyệt, từ chối và công bố đề tài | Trưởng khoa | FR-09; FR-08 |
| UC11 | Tạo nhóm, gửi và duyệt yêu cầu gia nhập | Sinh viên, Nhóm trưởng | FR-11; FR-12; FR-13 |
| UC12 | Đăng ký đề tài | Nhóm trưởng | FR-14; FR-10 |
| UC13 | Duyệt đăng ký và bắt đầu thực hiện | Trưởng khoa, Nhóm trưởng | FR-15 |
| UC14 | Nộp phiên bản báo cáo | Nhóm trưởng | FR-16 |
| UC15 | Xem và tải báo cáo | Thành viên chốt, GV được giao, Trưởng khoa | FR-17 |
| UC16 | Lập hội đồng và phân công chức danh | Trưởng khoa | FR-18 |
| UC17 | Giao đăng ký cho hội đồng | Trưởng khoa | FR-18 |
| UC19 | Lưu nháp và nộp điểm | Giảng viên được giao chấm | FR-20 |
| UC20 | Tổng hợp kết quả | Chủ tịch hoặc người có quyền tổng hợp | FR-21; FR-24 |
| UC21 | Công bố và kết thúc nhóm | Trưởng khoa | FR-22; FR-24; FR-25 |
| UC22 | Xem điểm và kết quả | Sinh viên | FR-23 |

# 3 Đặc tả chi tiết

## UC01 Đăng nhập và đăng xuất

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC01 |
| **2. Tên Use Case** | Đăng nhập và đăng xuất |
| **3. Mô tả ngắn** | Xác thực người dùng để sử dụng các chức năng được cấp và kết thúc phiên làm việc khi đăng xuất. |
| **4. Actor chính** | Người dùng có tài khoản: Quản trị viên, Trưởng khoa, Trưởng bộ môn, Giảng viên hoặc Sinh viên. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Người dùng đã được cấp tài khoản đang hoạt động. |
| **7. Trigger** | Người dùng chọn chức năng Đăng nhập. |
| **8. Main Flow** | 1. Người dùng mở trang đăng nhập.<br>2. Hệ thống hiển thị biểu mẫu đăng nhập.<br>3. Người dùng nhập email, mật khẩu và xác nhận đăng nhập.<br>4. Hệ thống kiểm tra thông tin xác thực, trạng thái tài khoản và các quyền được cấp.<br>5. Hệ thống tạo phiên hợp lệ và hiển thị trang làm việc phù hợp với quyền của người dùng.<br>6. Người dùng sử dụng các chức năng trong phạm vi được cấp.<br>7. Người dùng chọn Đăng xuất khi kết thúc công việc.<br>8. Hệ thống kết thúc phiên và hiển thị trang đăng nhập. |
| **9. Alternative Flow** | **AF-1  Đã có phiên đăng nhập hợp lệ**<br>Tại bước 1, hệ thống xác định người dùng đã đăng nhập và hiển thị trang làm việc theo quyền hiện tại. Người dùng tiếp tục từ bước 6. |
| **10. Exception Flow** | **EF-1  Không xác thực được tài khoản**<br>Tại bước 4, thông tin đăng nhập sai hoặc tài khoản bị khóa. Hệ thống thông báo không thể đăng nhập, không tạo phiên và cho người dùng nhập lại từ bước 3.<br>**EF-2  Phiên hết hiệu lực**<br>Tại bước 6, hệ thống phát hiện phiên không còn hợp lệ, dừng việc truy cập chức năng cần xác thực và yêu cầu người dùng đăng nhập lại. |
| **11. Post-condition** | Đăng nhập thành công tạo phiên theo đúng quyền. Sau đăng xuất, phiên cũ không còn giá trị truy cập. |
| **12. NFR liên quan** | NFR-01; NFR-02; NFR-03; NFR-09. |

## UC06 Tạo và công bố đợt đăng ký

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC06 |
| **2. Tên Use Case** | Tạo và công bố đợt đăng ký |
| **3. Mô tả ngắn** | Thiết lập đợt đăng ký theo loại đề tài, lịch giảng viên đề xuất và lịch sinh viên đăng ký để tổ chức thực hiện đúng thời gian. |
| **4. Actor chính** | Trưởng khoa. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Trưởng khoa đã đăng nhập và có quyền quản lý đợt đăng ký. |
| **7. Trigger** | Trưởng khoa chọn chức năng quản lý đợt đăng ký. |
| **8. Main Flow** | 1. Trưởng khoa mở danh sách đợt đăng ký và chọn Tạo đợt.<br>2. Hệ thống hiển thị biểu mẫu thông tin đợt và các mốc thời gian theo loại đề tài.<br>3. Trưởng khoa nhập mã, tên, loại đề tài, lịch đề xuất của giảng viên, lịch đăng ký của sinh viên và các hạn áp dụng.<br>4. Trưởng khoa xác nhận lưu đợt đăng ký.<br>5. Hệ thống kiểm tra thông tin bắt buộc, mã đợt và thứ tự các mốc thời gian; sau đó lưu đợt ở trạng thái nháp.<br>6. Trưởng khoa kiểm tra thông tin đã lưu và chọn Công bố.<br>7. Hệ thống kiểm tra điều kiện công bố, cập nhật trạng thái và hiển thị lịch để người dùng thực hiện theo từng giai đoạn. |
| **9. Alternative Flow** | **AF-1  Chỉnh sửa đợt hiện có**<br>Tại bước 1, Trưởng khoa chọn đợt còn được phép chỉnh sửa. Hệ thống hiển thị thông tin hiện tại; Trưởng khoa sửa và lưu theo bước 4. Hệ thống giữ nguyên loại đề tài khi đợt đã có đăng ký và không làm sai hiệu lực của đăng ký trước đó.<br>**AF-2  Đóng đợt đăng ký**<br>Tại bước 1, Trưởng khoa chọn đóng một đợt đã công bố và xác nhận. Hệ thống chuyển đợt sang đóng, ngừng nhận đăng ký mới; các công việc báo cáo, chấm điểm đang tiếp diễn vẫn tuân theo hạn và trạng thái riêng. |
| **10. Exception Flow** | **EF-1  Thông tin hoặc lịch không hợp lệ**<br>Tại bước 5 hoặc 7, hệ thống phát hiện mã đợt trùng, thiếu mốc bắt buộc hoặc các giai đoạn sai thứ tự. Hệ thống chỉ rõ thông tin cần sửa, không lưu thay đổi và cho Trưởng khoa quay lại bước 3.<br>**EF-2  Không được phép thay đổi đợt**<br>Khi quyền hoặc trạng thái đợt không cho phép thao tác, hệ thống từ chối và giữ nguyên thông tin hiện có. |
| **11. Post-condition** | Đợt được lưu hoặc chuyển trạng thái hợp lệ; lịch và các đăng ký đã tồn tại được bảo toàn. |
| **12. NFR liên quan** | NFR-02; NFR-04; NFR-05; NFR-07; NFR-18. |

## UC07 Đề xuất, sửa và gửi duyệt đề tài

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC07 |
| **2. Tên Use Case** | Đề xuất, sửa và gửi duyệt đề tài |
| **3. Mô tả ngắn** | Cho phép giảng viên chuẩn bị và gửi đề xuất đề tài để Trưởng khoa xét duyệt trong giai đoạn cho phép. |
| **4. Actor chính** | Giảng viên; Trưởng bộ môn thực hiện theo quyền giảng viên được cấp. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Người đề xuất đã đăng nhập, có quyền đề xuất; đợt đang trong giai đoạn giảng viên đề xuất. |
| **7. Trigger** | Giảng viên chọn chức năng Đề xuất đề tài. |
| **8. Main Flow** | 1. Giảng viên mở chức năng Đề xuất đề tài.<br>2. Hệ thống hiển thị biểu mẫu và các đợt, bộ môn hợp lệ.<br>3. Giảng viên chọn đợt, bộ môn, nhập tên, nội dung và yêu cầu của đề tài.<br>4. Giảng viên kiểm tra thông tin và chọn Gửi duyệt.<br>5. Hệ thống kiểm tra nội dung bắt buộc, quyền đề xuất và thời gian của đợt.<br>6. Hệ thống ghi nhận đề tài chờ duyệt, người đề xuất và thời điểm gửi.<br>7. Hệ thống hiển thị đề xuất cùng trạng thái chờ duyệt. |
| **9. Alternative Flow** | **AF-1  Lưu nháp đề xuất**<br>Tại bước 4, Giảng viên chọn Lưu nháp. Hệ thống kiểm tra thông tin cần thiết và lưu bản nháp của chính người đề xuất; đề tài chưa được chuyển cho người duyệt.<br>**AF-2  Chỉnh sửa đề xuất**<br>Tại bước 1, Giảng viên chọn bản nháp hoặc đề xuất bị từ chối còn được phép sửa trong thời gian đề xuất. Hệ thống hiển thị thông tin và lý do từ chối nếu có; Giảng viên chỉnh sửa rồi tiếp tục từ bước 4. |
| **10. Exception Flow** | **EF-1  Đề xuất không đủ thông tin**<br>Tại bước 5, hệ thống phát hiện thiếu tên, nội dung hoặc lựa chọn đợt, bộ môn không hợp lệ. Hệ thống không gửi duyệt và yêu cầu Giảng viên bổ sung từ bước 3.<br>**EF-2  Sai quyền, thời gian hoặc trạng thái**<br>Tại bước 5, người thao tác không có quyền, không sở hữu đề xuất, đã hết giai đoạn đề xuất hoặc đề tài đã khóa chỉnh sửa. Hệ thống từ chối lưu hay gửi duyệt. |
| **11. Post-condition** | Đề tài được lưu nháp hoặc gửi chờ duyệt, gắn đúng người đề xuất, đợt và bộ môn. |
| **12. NFR liên quan** | NFR-02; NFR-03; NFR-04; NFR-07; NFR-09. |

## UC08 Duyệt, từ chối và công bố đề tài

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC08 |
| **2. Tên Use Case** | Duyệt, từ chối và công bố đề tài |
| **3. Mô tả ngắn** | Xét duyệt đề xuất và công bố các đề tài đủ điều kiện để sinh viên lựa chọn đăng ký. |
| **4. Actor chính** | Trưởng khoa. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Trưởng khoa đã đăng nhập; có đề tài chờ duyệt hoặc đã được duyệt và chưa công bố. |
| **7. Trigger** | Trưởng khoa mở danh sách đề tài cần xét duyệt hoặc công bố. |
| **8. Main Flow** | 1. Trưởng khoa chọn đề tài chờ duyệt trong phạm vi quản lý.<br>2. Hệ thống hiển thị nội dung đề xuất, người đề xuất, đợt, bộ môn và phân công hướng dẫn.<br>3. Trưởng khoa xem xét nội dung và xác nhận duyệt đề tài.<br>4. Hệ thống kiểm tra quyền xét duyệt, trạng thái và thông tin bắt buộc của đề tài.<br>5. Hệ thống lưu quyết định duyệt cùng người và thời điểm xét duyệt.<br>6. Trưởng khoa chọn Công bố đối với đề tài đã được duyệt.<br>7. Hệ thống kiểm tra đề tài đã duyệt và có từ một đến hai giảng viên hướng dẫn hợp lệ, lưu trạng thái và thời điểm công bố; sinh viên đủ điều kiện có thể chọn đề tài trong lịch đăng ký. |
| **9. Alternative Flow** | **AF-1  Từ chối đề xuất**<br>Tại bước 3, Trưởng khoa chọn Từ chối, nhập lý do và xác nhận. Hệ thống lưu quyết định từ chối, người và thời điểm xử lý; người đề xuất được xem lý do. Đề tài không được công bố.<br>**AF-2  Công bố đề tài đã duyệt**<br>Tại bước 1, Trưởng khoa chọn một đề tài đã duyệt từ trước. Hệ thống hiển thị thông tin và tiếp tục từ bước 6; không tạo lại quyết định duyệt. |
| **10. Exception Flow** | **EF-1  Quyết định không hợp lệ**<br>Tại bước 4 hoặc khi từ chối, hệ thống phát hiện đề tài không còn ở trạng thái cho phép, người thao tác thiếu quyền hoặc chưa nhập lý do từ chối. Hệ thống không lưu quyết định và nêu thông tin cần kiểm tra.<br>**EF-2  Phân công hướng dẫn chưa hợp lệ**<br>Tại bước 7, hệ thống phát hiện thiếu giảng viên hướng dẫn, vượt quá hai người hoặc có phân công không hợp lệ. Hệ thống không công bố; Trưởng khoa hoàn thiện phân công trước khi thực hiện lại. |
| **11. Post-condition** | Quyết định được lưu đúng; chỉ đề tài đã công bố và còn đủ điều kiện mới được dùng để đăng ký. |
| **12. NFR liên quan** | NFR-02; NFR-04; NFR-05; NFR-12; NFR-18. |

## UC11 Tạo nhóm, gửi và duyệt yêu cầu gia nhập

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC11 |
| **2. Tên Use Case** | Tạo nhóm, gửi và duyệt yêu cầu gia nhập |
| **3. Mô tả ngắn** | Cho sinh viên tạo nhóm, xin tham gia và được nhóm trưởng xét duyệt. |
| **4. Actor chính** | Sinh viên; Nhóm trưởng xét duyệt yêu cầu của nhóm mình. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Sinh viên đã đăng nhập, chưa thuộc nhóm hoạt động khác. Nhóm trưởng chỉ duyệt cho nhóm mình đang lập. |
| **7. Trigger** | Sinh viên chọn Tạo nhóm hoặc tìm nhóm để gửi yêu cầu gia nhập. |
| **8. Main Flow** | 1. Sinh viên chọn Tạo nhóm; hệ thống hiển thị biểu mẫu.<br>2. Sinh viên nhập tên, chọn đợt và xác nhận tạo nhóm.<br>3. Hệ thống kiểm tra điều kiện, tạo nhóm đang lập và ghi người tạo làm nhóm trưởng chính thức.<br>4. Sinh viên khác tìm, lọc và xem chi tiết nhóm theo tên, đợt hoặc trạng thái.<br>5. Sinh viên đó chọn Gửi yêu cầu tham gia nhóm đang nhận thành viên.<br>6. Hệ thống lưu yêu cầu chờ duyệt sau khi kiểm tra điều kiện; sinh viên chưa là thành viên chính thức.<br>7. Nhóm trưởng xem yêu cầu, chọn sinh viên và xác nhận chấp thuận.<br>8. Hệ thống kiểm tra lại quyền, số chỗ và tư cách sinh viên rồi ghi thành viên chính thức, bảo đảm tối đa năm người và đúng một trưởng. |
| **9. Alternative Flow** | **AF-1  Xin tham gia nhóm đã có**<br>Sinh viên chọn nhóm đang lập còn nhận thành viên thay vì tạo mới; hệ thống hiển thị chi tiết nhóm và tiếp tục từ bước 5.<br>**AF-2  Từ chối yêu cầu gia nhập**<br>Tại bước 7, Nhóm trưởng chọn Từ chối; hệ thống lưu quyết định, không tăng số thành viên chính thức. |
| **10. Exception Flow** | **EF-1  Không đủ điều kiện tạo hoặc xin vào nhóm**<br>Tại bước 3 hoặc 6, sinh viên đã có nhóm hoạt động, đợt hoặc nhóm không hợp lệ hay yêu cầu trùng. Hệ thống từ chối, nêu lý do.<br>**EF-2  Không thể chấp thuận yêu cầu**<br>Tại bước 8, người duyệt sai quyền, nhóm đã chốt, đủ năm người hoặc sinh viên vừa vào nhóm khác. Hệ thống không duyệt yêu cầu.<br>**EF-3  Không hoàn tất việc tạo nhóm**<br>Tại bước 3, hệ thống không lưu được đầy đủ nhóm và tư cách nhóm trưởng; hệ thống báo thất bại, không giữ nhóm hoạt động thiếu trưởng. |
| **11. Post-condition** | Nhóm có đúng một trưởng, tối đa năm thành viên; sinh viên không thuộc hai nhóm hoạt động. Nhóm đủ 3–5 thành viên được đăng ký tại UC12, chốt danh sách tại UC13. |
| **12. NFR liên quan** | NFR-02; NFR-04; NFR-05; NFR-08; NFR-18. |

## UC12 Đăng ký đề tài

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC12 |
| **2. Tên Use Case** | Đăng ký đề tài |
| **3. Mô tả ngắn** | Cho phép nhóm trưởng chọn một đề tài đã công bố và gửi đăng ký cho nhóm đủ điều kiện trong thời gian sinh viên đăng ký. |
| **4. Actor chính** | Nhóm trưởng chính thức của nhóm. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Nhóm trưởng đã đăng nhập. Với đăng ký mới, nhóm có 3–5 thành viên chính thức, đúng một trưởng, chưa có đăng ký; đợt còn mở đăng ký sinh viên. |
| **7. Trigger** | Nhóm trưởng chọn chức năng Đăng ký đề tài. |
| **8. Main Flow** | 1. Nhóm trưởng mở chức năng Đăng ký đề tài.<br>2. Hệ thống xác định nhóm của người đang đăng nhập và hiển thị các đề tài đã công bố đủ điều kiện trong cùng đợt.<br>3. Nhóm trưởng tìm, lọc theo từ khóa, đợt hoặc bộ môn và xem chi tiết đề tài.<br>4. Nhóm trưởng chọn đề tài phù hợp và xác nhận đăng ký cho nhóm mình.<br>5. Hệ thống kiểm tra lại quyền nhóm trưởng, thành viên, thời gian, trạng thái đợt, đề tài và đăng ký hiện có.<br>6. Hệ thống ghi một đăng ký chờ duyệt, người đăng ký và thời điểm tiếp nhận.<br>7. Hệ thống hiển thị đề tài đã đăng ký và trạng thái chờ duyệt cho nhóm. |
| **9. Alternative Flow** | **AF-1  Xem đăng ký hiện có**<br>Tại bước 2, nếu nhóm đã có đăng ký, hệ thống hiển thị đăng ký đó cùng trạng thái và lý do từ chối nếu có. Nhóm trưởng thực hiện tra cứu; hệ thống không tạo đăng ký thứ hai, kể cả khi đăng ký cũ bị từ chối. |
| **10. Exception Flow** | **EF-1  Nhóm hoặc người đăng ký không hợp lệ**<br>Tại bước 5, người thao tác không phải nhóm trưởng, nhóm không có 3–5 thành viên chính thức hoặc không có đúng một trưởng. Hệ thống từ chối đăng ký và nêu điều kiện chưa đáp ứng.<br>**EF-2  Đề tài hoặc thời gian không hợp lệ**<br>Tại bước 5, đợt chưa mở, đã đóng hoặc hết hạn; đề tài chưa công bố, ngừng nhận hay thuộc đợt khác. Hệ thống không nhận đăng ký.<br>**EF-3  Đăng ký đã được ghi nhận**<br>Tại bước 6, nhóm đã có đăng ký do một yêu cầu trước vừa hoàn tất. Hệ thống không tạo bản trùng và hiển thị đăng ký hiện có cho người có quyền. |
| **11. Post-condition** | Đăng ký mới hợp lệ ở trạng thái chờ duyệt; đăng ký đã có được giữ nguyên. Mỗi nhóm có tối đa một đăng ký. |
| **12. NFR liên quan** | NFR-02; NFR-04; NFR-05; NFR-07; NFR-08. |

## UC13 Duyệt đăng ký và bắt đầu thực hiện

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC13 |
| **2. Tên Use Case** | Duyệt đăng ký và bắt đầu thực hiện |
| **3. Mô tả ngắn** | Xét duyệt đăng ký, chốt thành viên và cho nhóm trưởng bắt đầu thực hiện sau khi được duyệt. |
| **4. Actor chính** | Trưởng khoa xét duyệt; Nhóm trưởng bắt đầu thực hiện. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Actor đã đăng nhập và có quyền trên đăng ký. Xét duyệt: đăng ký chờ duyệt; bắt đầu thực hiện: đăng ký đã duyệt. |
| **7. Trigger** | Trưởng khoa mở danh sách đăng ký chờ duyệt. |
| **8. Main Flow** | 1. Trưởng khoa chọn đăng ký cần xét duyệt trong phạm vi quản lý.<br>2. Hệ thống hiển thị đề tài, đợt, nhóm, nhóm trưởng và danh sách thành viên chính thức.<br>3. Trưởng khoa xem xét thông tin và xác nhận chấp thuận đăng ký.<br>4. Hệ thống kiểm tra lại trạng thái, sự phù hợp của đề tài, đợt và nhóm có 3–5 thành viên chính thức, đúng một trưởng.<br>5. Hệ thống lưu người, thời điểm và quyết định duyệt; đồng thời chốt danh sách thành viên và nhóm trưởng.<br>6. Hệ thống hiển thị đăng ký đã duyệt, nhóm đã chốt; danh sách không được tự ý thay đổi.<br>7. Nhóm trưởng mở đăng ký đã duyệt và chọn Bắt đầu thực hiện.<br>8. Hệ thống kiểm tra quyền, trạng thái rồi chuyển cả đăng ký và nhóm sang đang thực hiện. |
| **9. Alternative Flow** | **AF-1  Từ chối đăng ký**<br>Tại bước 3, Trưởng khoa chọn Từ chối và nhập lý do. Hệ thống lưu quyết định trên đăng ký hiện có, hiển thị cho nhóm và chưa chốt danh sách.<br>**AF-2  Bắt đầu thực hiện vào thời điểm sau**<br>Sau bước 6, Nhóm trưởng chưa bắt đầu; hệ thống giữ đăng ký đã duyệt, nhóm đã chốt. Khi Nhóm trưởng bắt đầu, hệ thống tiếp tục từ bước 7. |
| **10. Exception Flow** | **EF-1  Không thể xét duyệt**<br>Tại bước 4 hoặc khi từ chối, người dùng thiếu quyền, đăng ký đã xử lý, nhóm không đủ điều kiện hoặc thiếu lý do từ chối. Hệ thống không lưu quyết định.<br>**EF-2  Không thể bắt đầu thực hiện**<br>Tại bước 8, người thao tác không phải nhóm trưởng hoặc đăng ký chưa ở trạng thái đã duyệt. Hệ thống từ chối chuyển trạng thái.<br>**EF-3  Không lưu được đầy đủ quyết định**<br>Tại bước 5 hoặc 8, cập nhật không hoàn tất. Hệ thống báo thất bại, không để đăng ký, nhóm và danh sách chốt mâu thuẫn. |
| **11. Post-condition** | Đăng ký bị từ chối có lý do hoặc được duyệt và chốt thành viên. Sau thao tác bắt đầu, nhóm và đăng ký cùng đang thực hiện. |
| **12. NFR liên quan** | NFR-02; NFR-04; NFR-05; NFR-12; NFR-18. |

## UC14 Nộp phiên bản báo cáo

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC14 |
| **2. Tên Use Case** | Nộp phiên bản báo cáo |
| **3. Mô tả ngắn** | Tiếp nhận báo cáo PDF của nhóm theo từng phiên bản, bảo toàn các bản đã nộp để làm căn cứ xem xét và chấm điểm. |
| **4. Actor chính** | Nhóm trưởng chính thức trong danh sách đã chốt. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Nhóm trưởng đã đăng nhập; đăng ký đang thực hiện, chưa khóa báo cáo để chấm. Hạn nộp đã được thiết lập và còn hiệu lực. |
| **7. Trigger** | Nhóm trưởng chọn chức năng Nộp báo cáo. |
| **8. Main Flow** | 1. Nhóm trưởng mở trang nộp báo cáo của nhóm mình.<br>2. Hệ thống hiển thị nhóm, đề tài, hạn nộp và lịch sử các phiên bản hiện có.<br>3. Nhóm trưởng chọn tệp PDF và xác nhận nộp.<br>4. Hệ thống kiểm tra quyền nộp, trạng thái, thời hạn, nội dung PDF và kích thước thực không vượt quá 20 MiB.<br>5. Hệ thống xác định số phiên bản mới, tính mã kiểm tra SHA-256 từ tệp nhận được và lưu báo cáo riêng tư.<br>6. Hệ thống ghi thông tin phiên bản, người nộp và thời điểm khi tệp được lưu thành công, bảo đảm không trùng phiên bản.<br>7. Hệ thống xác nhận nộp thành công và hiển thị lịch sử đã cập nhật; người có quyền xem hoặc tải tại UC15. |
| **9. Alternative Flow** | **AF-1  Nộp phiên bản bổ sung**<br>Tại bước 2, nhóm đã có báo cáo. Nhóm trưởng chọn tệp mới và tiếp tục từ bước 3 nếu còn hạn, chưa khóa chấm; hệ thống tạo phiên bản tiếp theo và giữ nguyên các phiên bản trước. |
| **10. Exception Flow** | **EF-1  Tệp không hợp lệ**<br>Tại bước 4, tệp rỗng, nội dung không phải PDF hợp lệ hoặc vượt 20 MiB. Hệ thống từ chối tệp, nêu lỗi và cho Nhóm trưởng chọn lại từ bước 3; không chỉ căn cứ tên hay loại tệp do người dùng cung cấp.<br>**EF-2  Không còn được phép nộp**<br>Tại bước 4 hoặc 6, hệ thống phát hiện người nộp thiếu quyền, chưa có hạn nộp, đã quá hạn hoặc báo cáo đã khóa để chấm. Hệ thống không ghi nhận phiên bản mới.<br>**EF-3  Không hoàn tất lưu báo cáo**<br>Tại bước 5 hoặc 6, hệ thống không lưu được tệp hoặc thông tin phiên bản. Hệ thống thông báo nộp thất bại, không xác nhận bản nộp chưa hoàn tất, giữ các phiên bản trước và bảo đảm thông tin báo cáo khớp với tệp được lưu. |
| **11. Post-condition** | Một phiên bản PDF hợp lệ được lưu bền vững, có người nộp, thời điểm và mã kiểm tra tương ứng. Các phiên bản cũ được bảo toàn. |
| **12. NFR liên quan** | NFR-02; NFR-05; NFR-06; NFR-07; NFR-15; NFR-18. |

## UC15 Xem và tải báo cáo

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC15 |
| **2. Tên Use Case** | Xem và tải báo cáo |
| **3. Mô tả ngắn** | Cho người có quyền tra cứu lịch sử và tải đúng phiên bản báo cáo của nhóm thuộc phạm vi được phép. |
| **4. Actor chính** | Thành viên trong danh sách nhóm đã chốt; giảng viên hướng dẫn, phản biện, chấm độc lập hoặc thành viên hội đồng được giao; Trưởng khoa theo phạm vi quản lý. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Người xem đã đăng nhập và có quyền đối với báo cáo của nhóm; nhóm đã có báo cáo được ghi nhận. |
| **7. Trigger** | Người xem mở danh sách báo cáo của nhóm được phép truy cập. |
| **8. Main Flow** | 1. Người xem chọn nhóm hoặc đăng ký đề tài cần tra cứu báo cáo.<br>2. Hệ thống kiểm tra tư cách thành viên đã chốt, phân công hoặc quyền quản lý trên nhóm được chọn.<br>3. Hệ thống hiển thị lịch sử phiên bản, tên tệp, người nộp, thời điểm và trạng thái báo cáo.<br>4. Người xem chọn phiên bản báo cáo cần tải.<br>5. Hệ thống kiểm tra lại quyền và sự tồn tại của tệp ứng với phiên bản đã chọn.<br>6. Hệ thống cung cấp đúng tệp PDF để người xem tải xuống. |
| **9. Alternative Flow** | **AF-1  Chỉ xem lịch sử phiên bản**<br>Tại bước 3, người xem tra cứu thông tin các lần nộp mà không tải tệp. Hệ thống giữ danh sách hiển thị và kết thúc tra cứu.<br>**AF-2  Tra cứu báo cáo lịch sử**<br>Tại bước 1, thành viên chọn nhóm đã hoàn thành mà mình có tên trong danh sách đã chốt. Hệ thống xác định quyền theo danh sách đó và tiếp tục từ bước 3, kể cả khi sinh viên đã tham gia nhóm mới. |
| **10. Exception Flow** | **EF-1  Không có quyền đối với báo cáo**<br>Tại bước 2 hoặc 5, người dùng không thuộc phạm vi được phép, kể cả khi tự thay mã nhóm hoặc phiên bản. Hệ thống từ chối xem hay tải và không cung cấp tệp.<br>**EF-2  Không lấy được tệp báo cáo**<br>Tại bước 5 hoặc 6, tệp không còn truy xuất được hoặc chức năng lưu trữ tạm thời không đáp ứng. Hệ thống thông báo chưa thể tải, không trả tệp thay thế và không xóa thông tin lịch sử đã ghi nhận. |
| **11. Post-condition** | Người có quyền xem được lịch sử hoặc nhận đúng phiên bản PDF đã chọn; dữ liệu và phiên bản báo cáo không thay đổi. |
| **12. NFR liên quan** | NFR-02; NFR-06; NFR-08; NFR-15; NFR-18. |

## UC16 Lập hội đồng và phân công chức danh

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC16 |
| **2. Tên Use Case** | Lập hội đồng và phân công chức danh |
| **3. Mô tả ngắn** | Thành lập hội đồng, bố trí giảng viên và chức danh để hội đồng đủ điều kiện nhận phân công chấm. |
| **4. Actor chính** | Trưởng khoa. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Trưởng khoa đã đăng nhập; có đợt áp dụng chấm hội đồng và danh sách giảng viên đủ điều kiện. |
| **7. Trigger** | Trưởng khoa chọn chức năng lập hội đồng. |
| **8. Main Flow** | 1. Trưởng khoa mở danh sách hội đồng và chọn tạo mới.<br>2. Hệ thống hiển thị biểu mẫu thông tin hội đồng, đợt và danh sách giảng viên.<br>3. Trưởng khoa nhập thông tin, chọn đợt và thêm từ ba đến năm giảng viên.<br>4. Trưởng khoa phân công đúng một chủ tịch, một thư ký là hai người khác nhau; các người còn lại là ủy viên.<br>5. Trưởng khoa xác nhận hội đồng sẵn sàng.<br>6. Hệ thống kiểm tra số người, chức danh, tư cách giảng viên và các xung đột với đăng ký đã giao nếu có.<br>7. Hệ thống lưu hội đồng sẵn sàng và hiển thị danh sách thành viên, chức danh; Trưởng khoa có thể giao đăng ký theo UC17. |
| **9. Alternative Flow** | **AF-1  Lưu hội đồng nháp**<br>Tại bước 5, Trưởng khoa chọn Lưu nháp khi chưa đủ thành viên hoặc chức danh. Hệ thống lưu thông tin nháp hợp lệ; hội đồng chưa được xác nhận sẵn sàng.<br>**AF-2  Điều chỉnh trước khi khóa chấm**<br>Tại bước 1, Trưởng khoa chọn hội đồng còn được phép chỉnh sửa, cập nhật thành viên hoặc chức danh rồi tiếp tục từ bước 5. Hệ thống kiểm tra xung đột đối với toàn bộ đăng ký đã giao cho hội đồng. |
| **10. Exception Flow** | **EF-1  Chưa đủ điều kiện sẵn sàng**<br>Tại bước 6, hội đồng có ít hơn ba hoặc nhiều hơn năm người, trùng giảng viên, thiếu hay trùng chức danh hoặc cùng một người làm chủ tịch và thư ký. Hệ thống không chuyển sang sẵn sàng và yêu cầu điều chỉnh.<br>**EF-2  Thành viên hoặc trạng thái không hợp lệ**<br>Tại bước 6, giảng viên không đủ điều kiện, có xung đột chấm đề tài mình hướng dẫn hoặc hội đồng đã khóa thay đổi. Hệ thống từ chối lưu thay đổi không hợp lệ. |
| **11. Post-condition** | Hội đồng được lưu nháp hoặc sẵn sàng. Hội đồng sẵn sàng có 3–5 giảng viên, đúng một chủ tịch và một thư ký khác người. |
| **12. NFR liên quan** | NFR-02; NFR-04; NFR-05; NFR-12; NFR-18. |

## UC17 Giao đăng ký cho hội đồng

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC17 |
| **2. Tên Use Case** | Giao đăng ký cho hội đồng |
| **3. Mô tả ngắn** | Phân công hội đồng phù hợp để chấm một đăng ký của nhóm, bảo đảm cùng đợt và không có xung đột hướng dẫn, chấm điểm. |
| **4. Actor chính** | Trưởng khoa. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Trưởng khoa đã đăng nhập; hội đồng đã sẵn sàng, đăng ký đã được duyệt và phân công còn được phép thay đổi. |
| **7. Trigger** | Trưởng khoa chọn chức năng giao đăng ký cho hội đồng. |
| **8. Main Flow** | 1. Trưởng khoa chọn hội đồng cần phân công.<br>2. Hệ thống hiển thị đợt, danh sách thành viên và các đăng ký có thể giao.<br>3. Trưởng khoa chọn đăng ký của nhóm cần được hội đồng chấm.<br>4. Hệ thống hiển thị đề tài, nhóm và các giảng viên hướng dẫn để Trưởng khoa đối chiếu.<br>5. Trưởng khoa xác nhận phân công.<br>6. Hệ thống kiểm tra đăng ký, hội đồng cùng đợt, trạng thái cho phép và không để giảng viên hướng dẫn tham gia chấm chính đề tài đó.<br>7. Hệ thống lưu phân công cùng người, thời điểm thực hiện và hiển thị đăng ký trong danh sách hội đồng được giao. |
| **9. Alternative Flow** | **AF-1  Thay đổi hội đồng trước khi khóa**<br>Tại bước 1, Trưởng khoa chọn đăng ký có phân công còn được phép thay đổi, chọn hội đồng thay thế và tiếp tục kiểm tra từ bước 4. Hệ thống chỉ thay phân công khi hội đồng mới hợp lệ; nếu không, phân công cũ được giữ nguyên. |
| **10. Exception Flow** | **EF-1  Đăng ký hoặc hội đồng không phù hợp**<br>Tại bước 6, đăng ký chưa được duyệt, khác đợt, hội đồng chưa sẵn sàng hoặc phân công đã bị khóa. Hệ thống từ chối và không thay đổi phân công.<br>**EF-2  Xung đột người hướng dẫn và người chấm**<br>Tại bước 6, hội đồng có giảng viên hướng dẫn đề tài của đăng ký được chọn. Hệ thống nêu xung đột và yêu cầu Trưởng khoa chọn hội đồng hoặc bố trí thành viên hợp lệ trước khi xác nhận lại.<br>**EF-3  Phân công vừa thay đổi**<br>Tại bước 7, hệ thống phát hiện phân công đã được xử lý bởi thao tác khác. Hệ thống không ghi trùng hay ghi đè phân công đã khóa, thông báo để Trưởng khoa tải lại danh sách. |
| **11. Post-condition** | Đăng ký được giao đúng hội đồng, đúng đợt và không vi phạm xung đột hướng dẫn, chấm điểm. |
| **12. NFR liên quan** | NFR-02; NFR-04; NFR-05; NFR-12; NFR-18. |

## UC19 Lưu nháp và nộp điểm

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC19 |
| **2. Tên Use Case** | Lưu nháp và nộp điểm |
| **3. Mô tả ngắn** | Cho giảng viên được phân công nhập điểm, nhận xét và nộp phiếu của chính mình để làm căn cứ tổng hợp kết quả. |
| **4. Actor chính** | Giảng viên được giao phản biện, chấm độc lập hoặc là thành viên hội đồng chấm đăng ký. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Giảng viên đã đăng nhập, có phân công chấm hợp lệ và không là người hướng dẫn đề tài đó. Đăng ký đã được mở chấm theo FR-19; phiếu chưa nộp, còn trong thời gian cho phép. |
| **7. Trigger** | Giảng viên chọn một đăng ký trong danh sách công việc chấm điểm của mình. |
| **8. Main Flow** | 1. Giảng viên mở danh sách đăng ký được giao chấm và chọn một nhóm.<br>2. Hệ thống kiểm tra phân công, hiển thị báo cáo đã khóa làm căn cứ chấm và biểu mẫu phiếu của chính giảng viên.<br>3. Giảng viên nhập điểm từ 0 đến 10, tối đa hai chữ số thập phân, cùng nhận xét.<br>4. Giảng viên kiểm tra và xác nhận Nộp phiếu.<br>5. Hệ thống kiểm tra lại người chấm, loại phiếu, phân công, thời hạn, điểm, nhận xét và trạng thái phiếu.<br>6. Hệ thống lưu phiếu đã nộp cùng người chấm và thời điểm nộp; không cho tự ý sửa phiếu đã nộp.<br>7. Hệ thống thông báo nộp thành công và hiển thị trạng thái phiếu. |
| **9. Alternative Flow** | **AF-1  Lưu nháp phiếu điểm**<br>Tại bước 4, Giảng viên chọn Lưu nháp. Hệ thống kiểm tra dữ liệu đã nhập, lưu phiếu nháp và chưa dùng phiếu đó để tổng hợp. Giảng viên được mở lại để hoàn thiện trong thời hạn, trạng thái cho phép rồi tiếp tục từ bước 3. |
| **10. Exception Flow** | **EF-1  Không đúng quyền chấm**<br>Tại bước 2 hoặc 5, người dùng không được giao chấm đăng ký, dùng sai loại phiếu, là giảng viên hướng dẫn hoặc cố nhập thay người khác. Hệ thống từ chối xem hay ghi phiếu ngoài quyền.<br>**EF-2  Điểm hoặc nhận xét không hợp lệ**<br>Tại bước 5, điểm ngoài khoảng 0–10, vượt hai chữ số thập phân, thiếu điểm hay nhận xét khi nộp. Hệ thống chỉ rõ lỗi, giữ phiếu chưa nộp và cho Giảng viên sửa từ bước 3.<br>**EF-3  Không còn được sửa hoặc nộp phiếu**<br>Tại bước 5 hoặc 6, đã quá hạn, đợt chấm không còn mở hoặc phiếu đã nộp. Hệ thống không ghi đè phiếu đã chốt và hiển thị trạng thái hiện tại. |
| **11. Post-condition** | Phiếu được lưu nháp hoặc nộp đúng người, đúng phân công. Chỉ phiếu đã nộp hợp lệ được xét tổng hợp. |
| **12. NFR liên quan** | NFR-02; NFR-04; NFR-05; NFR-07; NFR-12; NFR-18. |

## UC20 Tổng hợp kết quả

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC20 |
| **2. Tên Use Case** | Tổng hợp kết quả |
| **3. Mô tả ngắn** | Tổng hợp đúng tập phiếu chấm hợp lệ, tính trung bình cộng và lưu kết quả để Trưởng khoa xem xét công bố. |
| **4. Actor chính** | Chủ tịch hội đồng được giao; với loại không có hội đồng, người được giao quyền tổng hợp theo chính sách đã xác nhận. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Người tổng hợp đã đăng nhập và có quyền trên đăng ký đang chấm. Quyền tổng hợp và tập phiếu bắt buộc theo loại đề tài đã được xác nhận; kết quả chưa công bố. |
| **7. Trigger** | Người có quyền chọn chức năng Tổng hợp kết quả. |
| **8. Main Flow** | 1. Chủ tịch mở danh sách đăng ký thuộc hội đồng mình được giao và chọn nhóm cần tổng hợp.<br>2. Hệ thống xác định những người chấm, loại phiếu bắt buộc theo loại đề tài và phân công của đăng ký.<br>3. Hệ thống đối chiếu đủ từng người chấm, đúng loại phiếu, trạng thái đã nộp và dữ liệu điểm hợp lệ.<br>4. Hệ thống tính trung bình cộng của tập điểm hợp lệ và làm tròn kết quả đến hai chữ số thập phân.<br>5. Hệ thống hiển thị các phiếu làm căn cứ, số phiếu và điểm trung bình để Chủ tịch kiểm tra.<br>6. Chủ tịch xác nhận tổng hợp và nhập nhận xét tổng hợp nếu có.<br>7. Hệ thống kiểm tra lại căn cứ, lưu kết quả đã tổng hợp cùng người, thời điểm và hiển thị trạng thái chờ công bố. |
| **9. Alternative Flow** | **AF-1  Tổng hợp loại không có hội đồng**<br>Tại bước 1, chỉ khi quyền tổng hợp và tập phiếu của loại đề tài này đã được xác nhận, người có quyền chọn đăng ký trong phạm vi được giao. Hệ thống tiếp tục từ bước 2 theo tập phiếu đó; không tự cấp quyền này cho một vai trò khác. |
| **10. Exception Flow** | **EF-1  Chưa đủ phiếu hợp lệ**<br>Tại bước 3, hệ thống phát hiện thiếu phiếu, còn phiếu nháp, sai người chấm hoặc sai loại phiếu. Hệ thống hiển thị phần cần hoàn thiện cho người có quyền và không tính kết quả từ tập phiếu thiếu.<br>**EF-2  Không được tổng hợp**<br>Tại bước 1 hoặc 7, người thao tác không đúng quyền, chính sách áp dụng chưa được xác nhận, kết quả đã tổng hợp hoặc đã công bố. Hệ thống từ chối tạo kết quả mới và giữ nguyên kết quả hiện có. |
| **11. Post-condition** | Kết quả trung bình được lưu từ đủ phiếu hợp lệ, sẵn sàng xét công bố; sinh viên chưa được xem kết quả chưa công bố. |
| **12. NFR liên quan** | NFR-02; NFR-04; NFR-05; NFR-12; NFR-18. |

## UC21 Công bố và kết thúc nhóm

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC21 |
| **2. Tên Use Case** | Công bố và kết thúc nhóm |
| **3. Mô tả ngắn** | Công bố kết quả hợp lệ cho sinh viên, hoàn tất nhóm và thông báo kết quả qua email sau khi công bố thành công. |
| **4. Actor chính** | Trưởng khoa. |
| **5. Actor phụ** | Dịch vụ email hỗ trợ gửi thông báo; không phải vai trò người dùng nghiệp vụ. |
| **6. Pre-condition** | Trưởng khoa đã đăng nhập và có quyền công bố; kết quả đã tổng hợp hợp lệ, chưa công bố. |
| **7. Trigger** | Trưởng khoa chọn Công bố kết quả đối với đăng ký của một nhóm. |
| **8. Main Flow** | 1. Trưởng khoa mở danh sách kết quả đã tổng hợp trong phạm vi quản lý.<br>2. Hệ thống hiển thị nhóm, đề tài, điểm và các phiếu làm căn cứ tổng hợp.<br>3. Trưởng khoa kiểm tra thông tin và xác nhận công bố.<br>4. Hệ thống kiểm tra lại quyền, trạng thái, tập phiếu hợp lệ và sự phù hợp của kết quả tổng hợp.<br>5. Hệ thống lưu người, thời điểm công bố; chuyển kết quả sang đã công bố, hoàn tất đăng ký và nhóm, kết thúc tư cách tham gia hoạt động nhưng giữ danh sách thành viên đã chốt.<br>6. Hệ thống xác nhận công bố thành công và cho thành viên đã chốt tra cứu kết quả theo UC22.<br>7. Sau khi công bố hoàn tất, hệ thống gửi email thông báo kết quả cho sinh viên trong danh sách đã chốt thông qua dịch vụ email. |
| **9. Alternative Flow** | Không có. |
| **10. Exception Flow** | **EF-1  Chưa đủ điều kiện công bố**<br>Tại bước 4, người thao tác thiếu quyền, thiếu phiếu hợp lệ, kết quả không khớp căn cứ hoặc đã công bố. Hệ thống từ chối thao tác, giữ nguyên trạng thái và nêu lý do cho Trưởng khoa.<br>**EF-2  Không hoàn tất việc công bố**<br>Tại bước 5, hệ thống không lưu được đầy đủ thay đổi. Hệ thống thông báo thất bại, không công bố hay kết thúc một phần nhóm và không gửi email thông báo kết quả.<br>**EF-3  Không gửi được email sau công bố**<br>Tại bước 7, dịch vụ email không gửi thành công. Hệ thống ghi nhận và thông báo việc gửi chưa hoàn tất, giữ nguyên kết quả đã công bố; sinh viên vẫn tra cứu được kết quả tại UC22. |
| **11. Post-condition** | Kết quả được công bố; nhóm, đăng ký hoàn tất nhất quán và giữ lịch sử thành viên. Lỗi email không hủy kết quả đã công bố. |
| **12. NFR liên quan** | NFR-02; NFR-04; NFR-05; NFR-12; NFR-13; NFR-18. |

## UC22 Xem điểm và kết quả

| **Trường** | **Nội dung** |
| --- | --- |
| **1. Use Case ID** | UC22 |
| **2. Tên Use Case** | Xem điểm và kết quả |
| **3. Mô tả ngắn** | Cho sinh viên tra cứu điểm và nhận xét đã công bố của những nhóm mà mình có tên trong danh sách thành viên đã chốt. |
| **4. Actor chính** | Sinh viên thuộc danh sách thành viên đã chốt của nhóm. |
| **5. Actor phụ** | Không có. |
| **6. Pre-condition** | Sinh viên đã đăng nhập; quyền tra cứu được xác định theo danh sách thành viên đã chốt. |
| **7. Trigger** | Sinh viên chọn chức năng Kết quả của tôi. |
| **8. Main Flow** | 1. Sinh viên mở chức năng Kết quả của tôi.<br>2. Hệ thống xác định các nhóm mà sinh viên có tên trong danh sách đã chốt, kể cả nhóm đã hoàn thành.<br>3. Hệ thống hiển thị các kết quả đã công bố của những nhóm đó theo đợt đăng ký.<br>4. Sinh viên chọn đợt và kết quả cần xem.<br>5. Hệ thống kiểm tra quyền đối với kết quả được chọn và hiển thị đề tài, nhóm, điểm, nhận xét cùng thời điểm công bố.<br>6. Sinh viên xem thông tin chi tiết; hệ thống giữ nguyên dữ liệu kết quả. |
| **9. Alternative Flow** | **AF-1  Xem kết quả của nhóm đã hoàn thành**<br>Tại bước 4, Sinh viên chọn một kết quả thuộc đợt trước. Hệ thống tiếp tục từ bước 5 dựa trên danh sách đã chốt, không phụ thuộc sinh viên hiện thuộc nhóm mới hay không.<br>**AF-2  Chưa có kết quả được công bố**<br>Tại bước 3, hệ thống không tìm thấy kết quả đã công bố trong phạm vi được phép. Hệ thống hiển thị thông báo chưa có kết quả; điểm và nhận xét chưa công bố không được hiển thị. |
| **10. Exception Flow** | **EF-1  Truy cập kết quả ngoài phạm vi**<br>Tại bước 5, kết quả không thuộc nhóm có tên sinh viên trong danh sách đã chốt hoặc chưa được công bố. Hệ thống từ chối hiển thị, kể cả khi sinh viên tự thay mã kết quả. |
| **11. Post-condition** | Chỉ kết quả đã công bố đúng phạm vi được hiển thị, kể cả lịch sử sau khi nhóm kết thúc; dữ liệu không thay đổi. |
| **12. NFR liên quan** | NFR-02; NFR-03; NFR-08; NFR-09. |
