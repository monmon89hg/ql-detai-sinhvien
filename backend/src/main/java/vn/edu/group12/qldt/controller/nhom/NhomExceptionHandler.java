package vn.edu.group12.qldt.controller.nhom;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;

import java.security.Principal;

@ControllerAdvice(basePackageClasses = NhomController.class)
public class NhomExceptionHandler {
    @ExceptionHandler(NghiepVuNhomException.class)
    ModelAndView nghiepVu(NghiepVuNhomException ex, Principal principal) {
        HttpStatus status =
                switch (ex.getLoai()) {
                    case KHONG_DANG_NHAP -> HttpStatus.UNAUTHORIZED;
                    case CAM_TRUY_CAP -> HttpStatus.FORBIDDEN;
                    case KHONG_TIM_THAY -> HttpStatus.NOT_FOUND;
                    case XUNG_DOT -> HttpStatus.CONFLICT;
                    case DU_LIEU -> HttpStatus.BAD_REQUEST;
                    case LUU_TRU -> HttpStatus.SERVICE_UNAVAILABLE;
                };
        return loi(status, ex.getMessage(), principal);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConcurrencyFailureException.class})
    ModelAndView xungDot(Exception ex, Principal principal) {
        return loi(
                HttpStatus.CONFLICT,
                "Dữ liệu vừa được thay đổi bởi yêu cầu khác. Vui lòng tải lại trang để kiểm tra kết"
                        + " quả trước khi gửi lại.",
                principal);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    ModelAndView matKetNoi(Exception ex, Principal principal) {
        return loi(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Cơ sở dữ liệu đang không sẵn sàng. Vui lòng thử lại sau.",
                principal);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ModelAndView quaLon(Exception ex, Principal principal) {
        return loi(HttpStatus.PAYLOAD_TOO_LARGE, "Tệp PDF không được vượt quá 20 MiB.", principal);
    }

    @ExceptionHandler({
        MissingServletRequestPartException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    ModelAndView dauVao(Exception ex, Principal principal) {
        return loi(
                HttpStatus.BAD_REQUEST,
                "Thiếu hoặc sai định dạng dữ liệu gửi lên. Vui lòng kiểm tra lại biểu mẫu.",
                principal);
    }

    private ModelAndView loi(HttpStatus status, String message, Principal principal) {
        var view = new ModelAndView("shared/operation-error");
        view.setStatus(status);
        view.addObject("errorMessage", message);
        view.addObject("currentUserName", principal == null ? "Khách" : principal.getName());
        return view;
    }
}
