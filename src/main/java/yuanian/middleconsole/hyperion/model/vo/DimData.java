package yuanian.middleconsole.hyperion.model.vo;

import lombok.Data;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/25
 * @menu: TODO
 */
@Data
public class DimData {

    public static String SUCCESSFUL = "SUCCESSFUL";

    public static String FAILING    = "FAILING";

    private String status;

    private String data;

    public DimData(String status, String data) {
        this.status = status;
        this.data = data;
    }
}
