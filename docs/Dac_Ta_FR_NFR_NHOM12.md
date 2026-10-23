## 1. Tổng quan và phạm vi hệ thống

**Hệ thống phục vụ năm vai trò**: ADMIN, TRUONG_KHOA, TRUONG_BO_MON, GIANG_VIEN và SINH_VIEN. Bốn phân hệ gồm: (TV1) tài khoản/bộ môn/thông báo; (TV2) đợt đăng ký/đề tài; (TV3) nhóm sinh viên/đăng ký/báo cáo; (TV4) hội đồng/chấm điểm/kết quả.

| **Vai trò chính** | **Trách nhiệm và giới hạn** |
| --- | --- |
| ADMIN | Quản lý tài khoản, vai trò, bộ môn và thông báo; không tự có quyền nhập hoặc sửa điểm. |
| TRUONG_KHOA | Quản lý đợt, duyệt/công bố, phân công, hội đồng và kết quả theo chính sách đã chốt. |
| TRUONG_BO_MON | Vai trò giảng viên phụ trách bộ môn. Quyền cụ thể cần khớp cấu hình và Use Case, không tự cấp quyền quản trị toàn khoa. |
| GIANG_VIEN | Đề xuất và thực hiện hướng dẫn, phản biện hoặc chấm theo phân công hợp lệ. |
| SINH_VIEN | Tra cứu đề tài, tham gia nhóm và xem kết quả đúng nhóm/đợt; chỉ nhóm trưởng được đăng ký và nộp báo cáo. |

Nhóm trưởng, GVHD, GVPB, người chấm độc lập, thành viên/chủ tịch/thư ký hội đồng là vai trò trên một đối tượng cụ thể. Chúng không trở thành mã vai trò toàn cục mới trong bảng vai_tro. SMTP là hệ thống tích hợp gửi email, không phải actor nghiệp vụ thứ sáu; FR-24 được hệ thống thực hiện trong thao tác của người có quyền tổng hợp/công bố.

**Phân loại MoSCoW**: Must Have (bắt buộc), Should Have (quan trọng), Could Have (có thể hoãn).

| **Ưu tiên FR** | **Số lượng** | **Tỷ lệ thực tế** |
| --- | --- | --- |
| Must Have | 23 | 76,67% |
| Should Have | 5 | 16,67% |
| Could Have | 2 | 6,67% |
| Tổng | 30 | 100% |

# 1. YÊU CẦU CHỨC NĂNG (FR)

| **STT** | **Mã** | **Chức năng** | **Ưu tiên** | **Mô tả** |
| --- | --- | --- | --- | --- |
| 1 | **FR-01** | **Đăng nhập và đăng xuất**<br>**Tác nhân:** Năm vai trò có tài khoản hợp lệ | Must | Cho phép người dùng có tài khoản hợp lệ đăng nhập, sử dụng các chức năng theo vai trò và đăng xuất an toàn. Tài khoản bị khóa không được đăng nhập. |
| 2 | **FR-02** | **Xem hồ sơ và đổi mật khẩu**<br>**Tác nhân:** Người dùng đã đăng nhập | Should | Cho phép người dùng xem thông tin hồ sơ và đổi mật khẩu bằng cách nhập mật khẩu hiện tại, mật khẩu mới và xác nhận mật khẩu mới. |
| 3 | **FR-03** | **Quản lý tài khoản và vai trò**<br>**Tác nhân:** ADMIN | Must | Cho phép quản trị viên tạo, cập nhật, khóa/mở khóa tài khoản và quản lý vai trò của sinh viên, giảng viên, cán bộ. Mã người dùng và email không được trùng; không xóa dữ liệu đang được tham chiếu. |
| 4 | **FR-04** | **Quản lý bộ môn**<br>**Tác nhân:** ADMIN | Must | Cho phép quản trị viên thêm, xem, cập nhật và ngừng sử dụng bộ môn. Bộ môn ngừng hoạt động không còn được chọn cho hồ sơ mới nhưng dữ liệu liên quan trước đó được giữ lại. |
| 5 | **FR-05** | **Quản lý và đọc thông báo**<br>**Tác nhân:** ADMIN, TRUONG_KHOA và người đọc thuộc phạm vi | Must | Cho phép người có thẩm quyền soạn, chỉnh sửa, công bố hoặc ẩn thông báo. Người dùng chỉ xem được thông báo đã công bố cho vai trò hoặc đợt đăng ký của mình. |
| 6 | **FR-06** | **Quản lý đợt đăng ký hai giai đoạn**<br>**Tác nhân:** TRUONG_KHOA | Must | Cho phép trưởng khoa tạo, chỉnh sửa, công bố và đóng đợt đăng ký theo loại đề tài. Mỗi đợt có thời gian giảng viên đề xuất và thời gian sinh viên đăng ký; các mốc thời gian phải theo đúng thứ tự. |
| 7 | **FR-07** | **Đề xuất và chỉnh sửa đề tài**<br>**Tác nhân:** GIANG_VIEN; TRUONG_BO_MON theo quyền giảng viên | Must | Cho phép giảng viên đề xuất đề tài, cập nhật đề xuất khi được phép và gửi đề xuất để xét duyệt trong thời gian quy định. Đề tài phải có tên, nội dung và thuộc đợt, bộ môn hợp lệ. |
| 8 | **FR-08** | **Phân công giảng viên theo chức trách**<br>**Tác nhân:** TRUONG_KHOA | Must | Cho phép trưởng khoa phân công giảng viên hướng dẫn, phản biện hoặc chấm độc lập cho đề tài. Phân công phải phù hợp loại đề tài, không trùng vai trò và không vượt quá hai giảng viên hướng dẫn. |
| 9 | **FR-09** | **Duyệt từ chối và công bố đề tài**<br>**Tác nhân:** TRUONG_KHOA | Must | Cho phép trưởng khoa duyệt hoặc từ chối đề tài kèm lý do; công bố đề tài đã duyệt khi có giảng viên hướng dẫn hợp lệ. Đề tài được công bố mới có thể được sinh viên đăng ký. |
| 10 | **FR-10** | **Xem và tìm kiếm đề tài**<br>**Tác nhân:** SINH_VIEN, GIANG_VIEN, TRUONG_BO_MON, TRUONG_KHOA theo phạm vi | Should | Cho phép người dùng có quyền xem danh sách, thông tin chi tiết và tìm kiếm/lọc đề tài theo từ khóa, đợt, bộ môn hoặc trạng thái. Sinh viên chỉ được đăng ký những đề tài đã công bố đủ điều kiện. |
| 11 | **FR-11** | **Tạo nhóm sinh viên**<br>**Tác nhân:** SINH_VIEN | Must | Cho phép sinh viên tạo nhóm thuộc một đợt đăng ký và trở thành nhóm trưởng. Một sinh viên không được đồng thời thuộc hai nhóm đang hoạt động; nhóm đang lập có tối đa năm thành viên. |
| 12 | **FR-12** | **Tìm nhóm và xin tham gia**<br>**Tác nhân:** SINH_VIEN | Must | Cho phép sinh viên tìm kiếm, xem thông tin nhóm và gửi yêu cầu tham gia nhóm đang nhận thành viên. Yêu cầu phải được nhóm trưởng chấp thuận trước khi sinh viên trở thành thành viên chính thức. |
| 13 | **FR-13** | **Duyệt hoặc từ chối yêu cầu tham gia**<br>**Tác nhân:** SINH_VIEN là nhóm trưởng của nhóm | Must | Cho phép nhóm trưởng chấp thuận hoặc từ chối yêu cầu tham gia nhóm. Số thành viên chính thức không được vượt quá năm và một sinh viên không được đồng thời là thành viên của hai nhóm đang hoạt động. |
| 14 | **FR-14** | **Đăng ký đề tài cho nhóm**<br>**Tác nhân:** SINH_VIEN là nhóm trưởng | Must | Cho phép nhóm trưởng đăng ký một đề tài đã công bố trong đợt đăng ký hợp lệ. Nhóm phải có từ ba đến năm thành viên chính thức, đúng một nhóm trưởng và chỉ có tối đa một bản ghi đăng ký đề tài. |
| 15 | **FR-15** | **Duyệt đăng ký và bắt đầu thực hiện**<br>**Tác nhân:** TRUONG_KHOA; nhóm trưởng khi bắt đầu thực hiện | Must | Cho phép trưởng khoa duyệt hoặc từ chối đăng ký đề tài của nhóm, ghi nhận lý do từ chối và chốt danh sách thành viên khi duyệt. Nhóm được chuyển sang trạng thái thực hiện theo quy trình đăng ký đã quy định. |
| 16 | **FR-16** | **Nộp báo cáo PDF theo phiên bản**<br>**Tác nhân:** SINH_VIEN là nhóm trưởng chính thức | Must | Cho phép nhóm trưởng nộp báo cáo định dạng PDF trong thời hạn quy định, dung lượng tối đa 20 MiB. Mỗi lần nộp hợp lệ tạo một phiên bản mới và giữ lại các phiên bản trước. |
| 17 | **FR-17** | **Xem phiên bản và tải báo cáo có quyền**<br>**Tác nhân:** Thành viên nhóm hợp lệ; GV được phân công; trưởng khoa theo phạm vi | Must | Cho phép thành viên nhóm và giảng viên/người quản lý có quyền xem lịch sử phiên bản báo cáo và tải đúng tệp PDF theo phạm vi được phân quyền. |
| 18 | **FR-18** | **Lập và phân công hội đồng**<br>**Tác nhân:** TRUONG_KHOA | Must | Cho phép trưởng khoa tạo hội đồng, quản lý thành viên và chức danh, xác nhận hội đồng sẵn sàng và phân công nhóm đủ điều kiện. Hội đồng gồm ba đến năm giảng viên, có một chủ tịch và một thư ký khác nhau. |
| 19 | **FR-19** | **Mở chấm và khóa đầu vào**<br>**Tác nhân:** TRUONG_KHOA | Must | Cho phép người có thẩm quyền mở chấm đối với đăng ký đủ điều kiện và khóa những dữ liệu làm căn cứ chấm điểm, gồm báo cáo, danh sách thành viên và phân công người chấm. |
| 20 | **FR-20** | **Lưu nháp và nộp phiếu điểm**<br>**Tác nhân:** GIANG_VIEN được giao phản biện, chấm độc lập hoặc thành viên hội đồng | Must | Cho phép giảng viên được phân công nhập điểm và nhận xét, lưu nháp hoặc nộp phiếu chấm. Điểm từ 0 đến 10, tối đa hai chữ số thập phân; phiếu đã nộp không được tự ý chỉnh sửa. |
| 21 | **FR-21** | **Tổng hợp điểm trung bình**<br>**Tác nhân:** GIANG_VIEN là chủ tịch được giao; quyền loại không hội đồng cần chốt | Must | Cho phép người có quyền tổng hợp tính và lưu điểm trung bình từ các phiếu chấm hợp lệ, đủ số lượng và đúng người chấm. Điểm trung bình được làm tròn đến hai chữ số thập phân. |
| 22 | **FR-22** | **Công bố kết quả và hoàn tất nhóm**<br>**Tác nhân:** TRUONG_KHOA | Must | Cho phép trưởng khoa công bố kết quả đã được tổng hợp hợp lệ, cập nhật trạng thái hoàn thành của đăng ký và nhóm, đồng thời giữ thông tin cần thiết để tra cứu kết quả về sau. |
| 23 | **FR-23** | **Tra cứu kết quả theo đợt của sinh viên**<br>**Tác nhân:** SINH_VIEN thuộc nhóm đã chốt; người có thẩm quyền theo phạm vi | Must | Cho phép sinh viên xem điểm, nhận xét và thông tin kết quả đã công bố của các nhóm mà mình là thành viên trong danh sách đã chốt, kể cả sau khi nhóm kết thúc. |
| 24 | **FR-24** | **Kiểm tra đủ phiếu hợp lệ**<br>**Tác nhân:** Người có quyền tổng hợp hoặc công bố; kiểm tra tự động trong thao tác | Must | Hệ thống kiểm tra đầy đủ các phiếu chấm bắt buộc theo loại đề tài, đối chiếu đúng người chấm, loại phiếu và trạng thái đã nộp trước khi cho phép tổng hợp hoặc công bố kết quả. |
| 25 | **FR-25** | **Gửi email kết quả sau công bố**<br>**Tác nhân:** TRUONG_KHOA kích hoạt qua công bố; dịch vụ email là tích hợp ngoài | Must | Hệ thống gửi email thông báo kết quả cho các sinh viên thuộc danh sách thành viên đã chốt sau khi kết quả được công bố thành công. |
| 26 | **FR-26** | **Chỉnh sửa thông tin hồ sơ cá nhân**<br>**Tác nhân:** Người dùng đã đăng nhập | Should | Cho phép người dùng cập nhật các thông tin cá nhân được phép, như họ tên và số điện thoại. Mã người dùng, email và vai trò không được tự ý thay đổi tại chức năng này. |
| 27 | **FR-27** | **Quản lý thành viên và nhóm trưởng trước chốt**<br>**Tác nhân:** SINH_VIEN và nhóm trưởng theo quyền trên nhóm | Should | Cho phép quản lý thành viên và thay đổi nhóm trưởng trước khi chốt nhóm theo quyền được giao; sau khi chốt không được tự ý thay đổi danh sách thành viên và nhóm trưởng. |
| 28 | **FR-28** | **Ngừng nhận đăng ký mới cho đề tài**<br>**Tác nhân:** TRUONG_KHOA | Should | Cho phép trưởng khoa ngừng nhận đăng ký mới đối với đề tài đã công bố mà không hủy những đăng ký hoặc kết quả đã tồn tại. |
| 29 | **FR-29** | **Xuất báo cáo kết quả**<br>**Tác nhân:** TRUONG_KHOA; chủ tịch trong phạm vi được giao nếu nhóm duyệt | Could | Cho phép người có quyền xuất danh sách kết quả theo đợt ra tệp Excel hoặc PDF, với dữ liệu thuộc phạm vi được phép xem. |
| 30 | **FR-30** | **Xem thống kê tổng quan theo phạm vi**<br>**Tác nhân:** Năm vai trò theo phạm vi dữ liệu của mình | Could | Cho phép người dùng xem số liệu tổng quan phù hợp với vai trò, chẳng hạn số đề tài theo trạng thái, nhóm sinh viên hoặc công việc chấm điểm đang chờ xử lý. |

# 2. YÊU CẦU PHI CHỨC NĂNG (NFR)

| **STT** | **Mã** | **Yêu cầu** | **Ưu tiên** | **Mô tả** |
| --- | --- | --- | --- | --- |
| 1 | **NFR-01** | **Bảo mật xác thực và mật khẩu** | Must | Mật khẩu phải được lưu dưới dạng mã băm an toàn; không hiển thị hay ghi nhật ký mật khẩu. Hệ thống xác thực người dùng trước khi cho phép đổi mật khẩu. |
| 2 | **NFR-02** | **Phân quyền tại server và trên từng bản ghi** | Must | Các chức năng và dữ liệu phải được bảo vệ theo vai trò và quyền trên từng nhóm, đề tài, hội đồng hoặc kết quả. Không cho phép xem hay chỉnh sửa thông tin ngoài phạm vi được cấp. |
| 3 | **NFR-03** | **Bảo vệ phiên biểu mẫu và dữ liệu hiển thị** | Must | Hệ thống phải bảo vệ phiên đăng nhập, chống yêu cầu giả mạo đối với thao tác thay đổi dữ liệu và không để nội dung người dùng nhập gây thực thi mã không mong muốn. |
| 4 | **NFR-04** | **Toàn vẹn dữ liệu và tương thích mô hình khóa** | Must | Dữ liệu phải tuân thủ khóa chính, khóa ngoại và các quy tắc duy nhất, hợp lệ của cơ sở dữ liệu; không phát sinh bản ghi sai quan hệ giữa các phân hệ. |
| 5 | **NFR-05** | **Nhất quán giao dịch và xử lý đồng thời** | Must | Những thao tác cập nhật nhiều dữ liệu liên quan phải bảo đảm tính nhất quán. Khi có lỗi hoặc nhiều yêu cầu cùng lúc, hệ thống không được tạo trạng thái hoặc bản ghi mâu thuẫn. |
| 6 | **NFR-06** | **An toàn tệp PDF** | Must | Chỉ nhận báo cáo PDF hợp lệ có kích thước không vượt quá 20 MiB, lưu riêng tư và chỉ cho người có quyền truy cập hoặc tải xuống. |
| 7 | **NFR-07** | **Thời gian nghiệp vụ và chuyển trạng thái** | Must | Các mốc mở, đóng và hạn nộp phải được xác định bằng thời gian máy chủ, hiển thị nhất quán theo múi giờ Việt Nam; thao tác quá hạn không được chấp nhận. |
| 8 | **NFR-08** | **Hiệu năng trang nghiệp vụ** | Should | Các thao tác tìm kiếm, xem danh sách và cập nhật thông thường cần có thời gian phản hồi phù hợp khi nhiều người sử dụng; hệ thống tránh truy vấn và tải dữ liệu không cần thiết. |
| 9 | **NFR-09** | **Giao diện và thông báo dễ hiểu** | Should | Giao diện sử dụng tiếng Việt, bố cục thống nhất, nhãn và hướng dẫn rõ ràng; các thông báo lỗi giúp người dùng nhận biết và sửa dữ liệu không hợp lệ. |
| 10 | **NFR-10** | **Phân tầng và quản lý thay đổi CSDL** | Should | Hệ thống cần phân tách trách nhiệm giữa giao diện, xử lý nghiệp vụ và truy cập dữ liệu, thuận tiện cho bảo trì và mở rộng mà không ảnh hưởng chức năng khác. |
| 11 | **NFR-11** | **Cấu hình và khả năng triển khai** | Must | Ứng dụng phải triển khai được cùng các trang và tài nguyên giao diện, kết nối đúng cơ sở dữ liệu theo môi trường, và không đưa mật khẩu hoặc khóa truy cập vào mã nguồn công khai. |
| 12 | **NFR-12** | **Ghi log để truy vết** | Should | Các thao tác quan trọng như duyệt, phân công, nộp điểm và công bố cần được ghi nhận thông tin cần thiết để tra cứu sự kiện và xác định nguyên nhân lỗi. |
| 13 | **NFR-13** | **Độ tin cậy khi gửi email** | Must | Email thông báo chỉ được gửi sau khi kết quả công bố thành công. Lỗi gửi email không được làm mất hoặc hủy kết quả đã công bố. |
| 14 | **NFR-14** | **Khả năng kiểm thử và truy vết yêu cầu** | Should | Các yêu cầu cần có mã định danh nhất quán để thuận tiện đối chiếu với chức năng và việc kiểm thử trong quá trình phát triển. |
| 15 | **NFR-15** | **Lưu báo cáo bền vững** | Must | Báo cáo đã nộp và thông tin phiên bản phải được bảo toàn sau khi khởi động lại hoặc triển khai lại hệ thống; dữ liệu báo cáo và tệp lưu trữ phải nhất quán. |
| 16 | **NFR-16** | **Sao lưu và khôi phục nhất quán** | Should | Dữ liệu và tệp báo cáo cần có khả năng sao lưu, phục hồi khi gặp sự cố để hạn chế mất mát thông tin. |
| 17 | **NFR-17** | **Tương thích trình duyệt và màn hình** | Should | Các chức năng chính phải hoạt động trên trình duyệt Chrome, Edge và hiển thị phù hợp trên màn hình máy tính, điện thoại; không bị che khuất nút hoặc nội dung quan trọng. |
| 18 | **NFR-18** | **Phản hồi đúng khi thao tác thất bại** | Must | Thông báo thành công chỉ xuất hiện khi thao tác thực sự hoàn tất. Khi xảy ra lỗi, hệ thống phải giải thích dễ hiểu mà không làm lộ chi tiết kỹ thuật hoặc thông tin nhạy cảm. |
| 19 | **NFR-19** | **Khả năng truy cập bản triển khai khi nghiệm thu** | Should | Phiên bản triển khai phục vụ nghiệm thu phải truy cập được từ bên ngoài máy phát triển và có khả năng thực hiện những chức năng cần trình diễn. |
