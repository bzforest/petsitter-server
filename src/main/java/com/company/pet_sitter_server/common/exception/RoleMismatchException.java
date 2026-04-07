package com.company.pet_sitter_server.common.exception;

/**
 * โยนเมื่อ user ที่มี account อยู่แล้วพยายาม login ด้วย Google
 * โดยเลือก role ที่ไม่ตรงกับ role ที่มีอยู่ใน DB
 *
 * ตัวอย่าง: user มี role = SITTER แต่เลือก USER ในหน้า Google Login
 * → โยน RoleMismatchException("SITTER")
 *
 * GlobalExceptionHandler จะจับ exception นี้และคืน HTTP 409 Conflict
 * พร้อม body: { "code": "ROLE_MISMATCH", "accountRole": "SITTER" }
 * ให้ frontend อ่านและแสดง modal แจ้งเตือน user
 */
public class RoleMismatchException extends RuntimeException {

    private final String accountRole;

    public RoleMismatchException(String accountRole) {
        super("Your account already exists with role: " + accountRole +
              ". Please login again and select the correct role.");
        this.accountRole = accountRole;
    }

    public String getAccountRole() {
        return accountRole;
    }
}
