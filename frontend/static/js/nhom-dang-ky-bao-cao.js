// Chỉ xử lý trải nghiệm phía trình duyệt. Không ghi dữ liệu hay giả lập API thành công.
(function () {
    'use strict';
    const maxPdfSize = 20 * 1024 * 1024;
    const statusLabels = {
        DANG_LAP: 'Đang lập', DA_CHOT: 'Đã chốt', DANG_THUC_HIEN: 'Đang thực hiện',
        HOAN_THANH: 'Hoàn thành', GIAI_TAN: 'Đã giải tán', CHO_DUYET: 'Chờ duyệt',
        DA_DUYET: 'Đã duyệt', TU_CHOI: 'Từ chối', DANG_CHAM: 'Đang chấm',
        HOP_LE: 'Hợp lệ', THU_HOI: 'Đã thu hồi'
    };
    document.querySelectorAll('.sv-state[data-state]').forEach(item => {
        const state = item.dataset.state;
        if (statusLabels[state]) item.textContent = statusLabels[state];
    });

    document.querySelectorAll('[data-sv-confirm]').forEach(form => {
        form.addEventListener('submit', event => {
            const question = form.getAttribute('data-sv-confirm');
            if (question && !window.confirm(question)) event.preventDefault();
        });
    });

    document.querySelectorAll('[data-sv-upload]').forEach(form => {
        const fileInput = form.querySelector('[data-sv-pdf]');
        const errorEl = form.querySelector('[data-sv-file-error]');
        const detailEl = form.querySelector('[data-sv-file-details]');
        if (!fileInput) return;

        function setError(message) {
            if (errorEl) {
                errorEl.textContent = message;
                errorEl.hidden = !message;
            }
            fileInput.setCustomValidity(message);
        }

        async function validateFile() {
            const file = fileInput.files && fileInput.files[0];
            if (!file) {
                setError('Vui lòng chọn tệp PDF.');
                if (detailEl) detailEl.textContent = '';
                return false;
            }
            if (!/\.pdf$/i.test(file.name)) {
                setError('Tên tệp cần có đuôi .pdf.');
                return false;
            }
            if (file.size === 0 || file.size > maxPdfSize) {
                setError('PDF phải lớn hơn 0 byte và không quá 20 MiB.');
                return false;
            }
            try {
                const signature = await file.slice(0, 5).text();
                if (signature !== '%PDF-') {
                    setError('Tệp không có định dạng PDF hợp lệ.');
                    return false;
                }
            } catch (error) {
                setError('Không đọc được tệp đã chọn.');
                return false;
            }
            setError('');
            if (detailEl) detailEl.textContent = `${file.name} · ${(file.size / 1024 / 1024).toFixed(2)} MiB`;
            return true;
        }

        fileInput.addEventListener('change', () => { void validateFile(); });

        // Không chặn/can thiệp submit của Thymeleaf. Chỉ gửi đi khi đã kiểm tra file.
        form.addEventListener('submit', event => {
            if (form.dataset.svFileValidated === 'true') {
                delete form.dataset.svFileValidated;
                return;
            }
            event.preventDefault();
            void validateFile().then(valid => {
                if (!valid) {
                    fileInput.reportValidity();
                    return;
                }
                form.dataset.svFileValidated = 'true';
                form.requestSubmit();
            });
        });
    });
})();
