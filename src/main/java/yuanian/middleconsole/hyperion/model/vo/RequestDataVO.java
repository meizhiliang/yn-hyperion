package yuanian.middleconsole.hyperion.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/28
 * @menu: TODO
 */
@Data
public class RequestDataVO {

    private EsbInfoVO ESBINFO;

    private Map<String,String> REQUESTINFO;
}
