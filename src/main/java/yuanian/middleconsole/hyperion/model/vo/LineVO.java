package yuanian.middleconsole.hyperion.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/12/16
 * @menu: TODO
 */
@Data
public class LineVO {

    private String currency;

    private String itemCode;

    private String periodYear;

    private Integer rate = 1;

    private Integer quantity = 1;

    private String companyCode;

    private String unitCode;

    private String remark = "";

    private String dimension1Code = "PROJECT";

    private String dimension1ValueCode;

    private String periodName;

    private BigDecimal amount;

    private BigDecimal functionalAmount;

}
