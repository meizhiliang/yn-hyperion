package yuanian.middleconsole.hyperion.model.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/12/30
 * @menu: TODO
 */
@Data
public class InterfaceLogVO implements Serializable {

    private String id;

    private String instId;

    private String interTitle;

    private String interMethod;

    private String requester;

    private String respondent;

    private LocalDateTime requestTime;

    private LocalDateTime responseTime;

    private String result;

    private String requestParameter;

    private String responseParameter;
    /**
     * 查询数据范围
     */
    private Integer dataCount;

    private static final long serialVersionUID = 1L;
}