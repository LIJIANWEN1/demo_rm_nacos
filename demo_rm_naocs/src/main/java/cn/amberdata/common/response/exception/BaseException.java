//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cn.amberdata.common.response.exception;

import cn.amberdata.common.response.enums.ExceptionCode;

public class BaseException extends RuntimeException {

    private String code;
    private String message;
    private Object[] args;

    public BaseException() {
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(Throwable throwable) {
        super(throwable);
    }

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BaseException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.message = message;
        this.args = args;
    }

    public BaseException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.code = exceptionCode.getCode();
        this.message = exceptionCode.getMessage();
    }

    public BaseException(ExceptionCode exceptionCode, Object... args) {
        super(exceptionCode.getMessage());
        this.code = exceptionCode.getCode();
        this.message = exceptionCode.getMessage();
        this.args = args;
    }

    public BaseException(ExceptionCode exceptionCode, String format) {
        super(String.format(exceptionCode.getMessage(), format));
        this.code = exceptionCode.getCode();
        this.message = String.format(exceptionCode.getMessage(), format);
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public Object[] getArgs() {
        return this.args;
    }
}
