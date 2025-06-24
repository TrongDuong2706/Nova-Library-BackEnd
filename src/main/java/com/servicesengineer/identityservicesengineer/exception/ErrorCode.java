package com.servicesengineer.identityservicesengineer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(1001, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_CREDENTIAL(1002, "Sai mật khẩu, vui lòng thử lại", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(1003, "Không tìm thấy username", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1004, "You do not have permission", HttpStatus.FORBIDDEN),
    USER_NOT_EXISTED(1005, "User không tồn tại", HttpStatus.BAD_REQUEST),
    WRONG_INPUT_TYPE(1006, "Nhập sai kiểu dữ liệu", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1007, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    AUTHOR_NOT_EXISTED(1008, "Author not existed", HttpStatus.NOT_FOUND),
    GENRE_NOT_EXISTED(1009, "Genre not existed", HttpStatus.NOT_FOUND),
    BOOK_NOT_EXISTED(1010, "Book not existed", HttpStatus.NOT_FOUND),
    BORROW_NOT_EXISTED(1011, "Borrow Id not existed", HttpStatus.NOT_FOUND),
    BORROWING_ALREADY_RETURNED(1012, "Borrowing already returned", HttpStatus.BAD_REQUEST),
    USERNAME_HAS_EXISTED(1013, "Username already exists", HttpStatus.BAD_REQUEST),
    BOOK_OUT_OF_STOCK(1014, "Số lượng sách trong kho không đủ", HttpStatus.BAD_REQUEST),
    ALREADY_FAVORITED(1015, "Sách đã nằm trong mục yêu thích", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1016, "Token không hợp lệ", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1017, "Token đã quá hạn", HttpStatus.BAD_REQUEST),
    USER_HAS_UNRETURNED_BORROWING(1018, "Bạn có đơn mượn chưa trả", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(1019, "Email không đúng định dạng", HttpStatus.BAD_REQUEST),
    DISABLE_ACCOUNT(1020, "Tài khoản đã bị vô hiệu hóa", HttpStatus.BAD_REQUEST),

    ;






    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
