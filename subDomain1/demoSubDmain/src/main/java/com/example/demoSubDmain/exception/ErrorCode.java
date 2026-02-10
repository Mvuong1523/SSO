package com.example.demoSubDmain.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    SUCCESS(1000, "Success"),
    STUDENT_NOT_EXISTED(1001, "Student not existed"),
    STUDENT_EXISTED(1002, "Student existed"),
    INVALID_KEY(1003, "Invalid message key"),
    INVALID_REQUEST(1004, "Invalid request"),
    ;

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
