package yuanian.middleconsole.hyperion.common.exception;

import lombok.Data;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/17
 * @menu: TODO
 */
@Data
public class BussinessException extends RuntimeException {

    private String methodCode;

    private Throwable cause;


    private static final long serialVersionUID = 1L;

    public BussinessException(String message) {
        super(message);
    }

    public BussinessException(String methodCode,String message,Throwable cause) {
        super(message);
        this.methodCode = methodCode;
        this.cause = cause;
    }
}
