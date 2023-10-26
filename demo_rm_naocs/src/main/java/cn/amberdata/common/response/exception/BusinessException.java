//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cn.amberdata.common.response.exception;

import cn.amberdata.common.response.enums.ExceptionCode;

public class BusinessException extends BaseException {
    public BusinessException(String code, String message) {
        super(code, message);
    }

    public BusinessException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    public BusinessException(Throwable throwable) {
        super(throwable);
    }

    public BusinessException(ExceptionCode exceptionCode, String format) {
        super(exceptionCode, format);
    }

    public BusinessException(ExceptionCode exceptionCode, Object[] args) {
        super(exceptionCode, args);
    }
}
