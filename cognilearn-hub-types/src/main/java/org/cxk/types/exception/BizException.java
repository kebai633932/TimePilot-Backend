package org.cxk.types.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cxk.types.enums.ResponseCode;

/**
 * @author KJH
 * @description
 * @create 2025/6/8 9:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BizException  extends RuntimeException {

    private static final long serialVersionUID = 5317680961212299217L;

    /** 异常码 */
    private String code;

    /** 异常信息 */
    private String info;

    public BizException (String code) {
        this.code = code;
    }
    public BizException (ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.info = responseCode.getInfo();
    }

    public BizException (String code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    public BizException (String code, String message) {
        this.code = code;
        this.info = message;
        super.initCause(new Throwable(message));
    }

    public BizException (String code, String message, Throwable cause) {
        this.code = code;
        this.info = message;
        super.initCause(cause);
    }

    @Override
    public String toString() {
        return "org.cxk.types.exception.AppException{" +
                "code='" + code + '\'' +
                ", info='" + info + '\'' +
                '}';
    }

}
