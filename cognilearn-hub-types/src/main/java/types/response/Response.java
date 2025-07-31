package types.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import types.enums.ResponseCode;

import java.io.Serializable;

/**
 * @author KJH
 * @description
 * @create 2025/6/8 8:55
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private String code;
    private String info;
    private T data;
    // types/response/Response.java


    //静态工厂方法
    public static <T> Response<T> success(T data) {
            return Response.<T>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
    }

    public static <T> Response<T> error(ResponseCode code) {
        return Response.<T>builder()
                .code(code.getCode())
                .info(code.getInfo())
                .data(null)
                .build();
    }
    public static <T> Response<T> error(ResponseCode code,String s) {
        return Response.<T>builder()
                .code(code.getCode())
                .info(s)
                .data(null)
                .build();
    }

}
