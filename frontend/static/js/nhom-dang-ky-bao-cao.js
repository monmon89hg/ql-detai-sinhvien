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

    // Modal xác nhận dùng chung cho các thao tác TV3 có data-sv-confirm.
    // Chỉ thay lớp giao diện; POST, ràng buộc form và CSRF vẫn do browser + backend xử lý.
    const actionForms = [...document.querySelectorAll('form[data-sv-confirm]')];
    if (actionForms.length) {
        const root = document.querySelector('.sv-module');
        if (root) {
            const overlay = document.createElement('div');
            overlay.className = 'sv-reject-overlay';
            overlay.hidden = true;
            overlay.setAttribute('data-sv-action-dialog', '');
            // HTML tĩnh do ứng dụng kiểm soát. Dữ liệu nhóm/sinh viên được gán qua textContent.
            overlay.innerHTML = `
                <section class="sv-reject-modal" role="dialog" aria-modal="true"
                         aria-labelledby="sv-action-title" aria-describedby="sv-action-detail sv-action-note"
                         tabindex="-1" data-sv-action-panel>
                    <div class="sv-reject-heading">
                        <h2 id="sv-action-title" data-sv-action-title>Xác nhận thao tác</h2>
                        <button class="sv-reject-close" type="button" data-sv-action-close
                                aria-label="Đóng hộp thoại">&times;</button>
                    </div>
                    <p id="sv-action-detail" class="sv-help" data-sv-action-detail></p>
                    <p id="sv-action-note" class="sv-info sv-info-soft" data-sv-action-note hidden></p>
                    <div class="sv-reject-footer">
                        <button class="btn btn-outline" type="button" data-sv-action-close>Hủy</button>
                        <button class="btn btn-primary" type="button" data-sv-action-submit>Xác nhận</button>
                    </div>
                </section>`;
            root.appendChild(overlay);
            const panel = overlay.querySelector('[data-sv-action-panel]');
            const title = overlay.querySelector('[data-sv-action-title]');
            const detail = overlay.querySelector('[data-sv-action-detail]');
            const note = overlay.querySelector('[data-sv-action-note]');
            const confirmButton = overlay.querySelector('[data-sv-action-submit]');
            const approved = new WeakSet();
            const submitted = new WeakSet();
            let currentForm = null;
            let previousFocus = null;
            let previousSubmitter = null;

            function closeActionDialog() {
                overlay.hidden = true;
                document.body.classList.remove('sv-reject-modal-open');
                currentForm = null;
                previousSubmitter = null;
                if (previousFocus && previousFocus.isConnected) previousFocus.focus();
                previousFocus = null;
            }

            actionForms.forEach(form => {
                form.addEventListener('submit', event => {
                    if (submitted.has(form)) {
                        event.preventDefault();
                        return;
                    }
                    if (approved.has(form)) {
                        approved.delete(form);
                        submitted.add(form);
                        return;
                    }
                    event.preventDefault();
                    currentForm = form;
                    previousSubmitter = event.submitter || form.querySelector('button[type="submit"], input[type="submit"]');
                    previousFocus = previousSubmitter;
                    title.textContent = form.getAttribute('data-sv-confirm') || 'Xác nhận thao tác';
                    detail.textContent = form.getAttribute('data-sv-confirm-detail') || '';
                    note.textContent = form.getAttribute('data-sv-confirm-note') || '';
                    note.hidden = !note.textContent;
                    note.classList.toggle('sv-alert-danger', form.dataset.svConfirmLevel === 'danger');
                    note.classList.toggle('sv-alert-warning', form.dataset.svConfirmLevel !== 'danger');
                    confirmButton.textContent = form.getAttribute('data-sv-confirm-button') || 'Xác nhận';
                    overlay.hidden = false;
                    document.body.classList.add('sv-reject-modal-open');
                    confirmButton.focus();
                });
            });

            overlay.querySelectorAll('[data-sv-action-close]').forEach(button => {
                button.addEventListener('click', closeActionDialog);
            });
            overlay.addEventListener('click', event => {
                if (event.target === overlay) closeActionDialog();
            });
            overlay.addEventListener('keydown', event => {
                if (event.key === 'Escape') {
                    event.preventDefault();
                    closeActionDialog();
                    return;
                }
                if (event.key !== 'Tab') return;
                const focusable = [...panel.querySelectorAll('button:not([disabled])')];
                if (!focusable.length) return;
                const first = focusable[0];
                const last = focusable[focusable.length - 1];
                if (event.shiftKey && document.activeElement === first) {
                    event.preventDefault();
                    last.focus();
                } else if (!event.shiftKey && document.activeElement === last) {
                    event.preventDefault();
                    first.focus();
                }
            });
            confirmButton.addEventListener('click', () => {
                const form = currentForm;
                if (!form) return;
                const submitter = previousSubmitter;
                closeActionDialog();
                // Kiểm tra trước để không giữ cờ 'approved' nếu form chưa hợp lệ.
                if (!form.reportValidity()) return;
                approved.add(form);
                if (submitter && submitter.form === form && !submitter.disabled) {
                    form.requestSubmit(submitter);
                } else {
                    form.requestSubmit();
                }
            });
        }
    }

    // Chỉ riêng nút đăng ký đề tài dùng modal thay cho window.confirm mặc định.
    // Form POST gốc, CSRF và mã đề tài hidden vẫn do Thymeleaf gửi lên backend.
    const registerOverlay = document.querySelector('[data-sv-register-dialog]');
    if (registerOverlay) {
        const panel = registerOverlay.querySelector('[data-sv-register-panel]');
        const code = registerOverlay.querySelector('[data-sv-register-code]');
        const title = registerOverlay.querySelector('[data-sv-register-title]');
        const confirmButton = registerOverlay.querySelector('[data-sv-register-submit]');
        const submittedForms = new WeakSet();
        const approvedForms = new WeakSet();
        let currentForm = null;
        let previousFocus = null;

        function closeRegistrationModal() {
            registerOverlay.hidden = true;
            document.body.classList.remove('sv-reject-modal-open');
            currentForm = null;
            if (previousFocus && previousFocus.isConnected) previousFocus.focus();
            previousFocus = null;
        }

        document.querySelectorAll('form[data-sv-register-confirm]').forEach(form => {
            form.addEventListener('submit', event => {
                if (submittedForms.has(form)) {
                    event.preventDefault();
                    return;
                }
                if (approvedForms.has(form)) {
                    approvedForms.delete(form);
                    submittedForms.add(form);
                    const submitButton = form.querySelector('button[type="submit"]');
                    if (submitButton) submitButton.disabled = true;
                    return;
                }
                event.preventDefault();
                currentForm = form;
                previousFocus = event.submitter || form.querySelector('button[type="submit"]');
                // textContent chống chèn HTML khi tên đề tài chứa ký tự đặc biệt.
                code.textContent = form.dataset.svRegisterCode || '—';
                title.textContent = form.dataset.svRegisterTitle || '—';
                registerOverlay.hidden = false;
                document.body.classList.add('sv-reject-modal-open');
                confirmButton.focus();
            });
        });

        registerOverlay.querySelectorAll('[data-sv-register-close]').forEach(button => {
            button.addEventListener('click', closeRegistrationModal);
        });
        registerOverlay.addEventListener('click', event => {
            if (event.target === registerOverlay) closeRegistrationModal();
        });
        registerOverlay.addEventListener('keydown', event => {
            if (event.key === 'Escape') {
                event.preventDefault();
                closeRegistrationModal();
                return;
            }
            if (event.key !== 'Tab') return;
            const focusable = [...panel.querySelectorAll('button:not([disabled])')];
            if (!focusable.length) return;
            const first = focusable[0];
            const last = focusable[focusable.length - 1];
            if (event.shiftKey && document.activeElement === first) {
                event.preventDefault();
                last.focus();
            } else if (!event.shiftKey && document.activeElement === last) {
                event.preventDefault();
                first.focus();
            }
        });
        confirmButton.addEventListener('click', () => {
            if (!currentForm) return;
            const form = currentForm;
            closeRegistrationModal();
            // requestSubmit chạy validation + sự kiện submit; form được duyệt đúng một lần.
            approvedForms.add(form);
            form.requestSubmit();
        });
    }

    // Modal từ chối đăng ký: URL theo đúng nhóm, CSRF có sẵn trong form Thymeleaf.
    const rejectOverlay = document.querySelector('[data-sv-reject-dialog]');
    if (rejectOverlay) {
        const panel = rejectOverlay.querySelector('[data-sv-reject-panel]');
        const rejectForm = rejectOverlay.querySelector('[data-sv-reject-form]');
        const reason = rejectOverlay.querySelector('[data-sv-reject-reason]');
        const errorEl = rejectOverlay.querySelector('[data-sv-reject-error]');
        const groupLabel = rejectOverlay.querySelector('[data-sv-reject-group]');
        const submitBtn = rejectOverlay.querySelector('[data-sv-reject-submit]');
        let lastTrigger = null;

        function resetReasonError() {
            reason.setCustomValidity('');
            reason.removeAttribute('aria-invalid');
            errorEl.textContent = '';
            errorEl.hidden = true;
        }

        function closeRejectModal() {
            rejectOverlay.hidden = true;
            document.body.classList.remove('sv-reject-modal-open');
            rejectForm.reset();
            rejectForm.removeAttribute('action');
            resetReasonError();
            submitBtn.disabled = false;
            if (lastTrigger) lastTrigger.focus();
        }

        document.querySelectorAll('[data-sv-reject-open]').forEach(button => {
            button.addEventListener('click', () => {
                const url = button.getAttribute('data-sv-reject-url');
                if (!url) return;
                lastTrigger = button;
                rejectForm.reset();
                resetReasonError();
                submitBtn.disabled = false;
                rejectForm.action = url;
                groupLabel.textContent = button.getAttribute('data-sv-reject-group') || '';
                rejectOverlay.hidden = false;
                document.body.classList.add('sv-reject-modal-open');
                reason.focus();
            });
        });

        rejectOverlay.querySelectorAll('[data-sv-reject-close]').forEach(button => {
            button.addEventListener('click', closeRejectModal);
        });
        rejectOverlay.addEventListener('click', event => {
            if (event.target === rejectOverlay) closeRejectModal();
        });
        rejectOverlay.addEventListener('keydown', event => {
            if (event.key === 'Escape') {
                event.preventDefault();
                closeRejectModal();
            }
            if (event.key !== 'Tab') return;
            const focusable = [...panel.querySelectorAll('button:not([disabled]), textarea:not([disabled]), input:not([type="hidden"]):not([disabled])')];
            if (!focusable.length) return;
            const first = focusable[0];
            const last = focusable[focusable.length - 1];
            if (event.shiftKey && document.activeElement === first) {
                event.preventDefault();
                last.focus();
            } else if (!event.shiftKey && document.activeElement === last) {
                event.preventDefault();
                first.focus();
            }
        });
        reason.addEventListener('input', resetReasonError);

        rejectForm.addEventListener('submit', event => {
            const trimmed = reason.value.trim();
            if (!trimmed || trimmed.length > 1000) {
                event.preventDefault();
                const message = 'Vui lòng nhập lý do từ chối từ 1 đến 1.000 ký tự.';
                reason.setCustomValidity(message);
                reason.setAttribute('aria-invalid', 'true');
                errorEl.textContent = message;
                errorEl.hidden = false;
                reason.reportValidity();
                return;
            }
            // Xác nhận bằng nút trong modal, sau đó gửi form POST thật đến backend.
            reason.value = trimmed;
            submitBtn.disabled = true;
        });
    }

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
