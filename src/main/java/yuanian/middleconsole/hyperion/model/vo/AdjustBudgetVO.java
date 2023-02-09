package yuanian.middleconsole.hyperion.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/12/8
 * @menu: TODO
 */
@Data
public class AdjustBudgetVO implements Serializable {

    private String id;

    private String batchNo;

    private String accountCode;

    private String entityCode;

    private String deptCode;

    private String yearCode;

    private String periodCode;

    private BigDecimal q1Amount;

    private BigDecimal q2Amount;

    private BigDecimal q3Amount;

    private BigDecimal q4Amount;

    private BigDecimal adjustAmount;

    private BigDecimal investAdjustAmount;
    /**
     * A 待同步; B 预算系统同步成功 ; C 预算系统同步失败; D 汇联易同步失败; E 同步完成
     */
    private String syncStatus;
    /**
     * 状态取反
     */
    private String notEqualsStatus;

    private String syncMsg;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String adjustType;

    private String currenyCode;

    private String projectCode;

    private Integer dataCount;

    private Integer pushCount;

    private static final long serialVersionUID = 1L;
}
