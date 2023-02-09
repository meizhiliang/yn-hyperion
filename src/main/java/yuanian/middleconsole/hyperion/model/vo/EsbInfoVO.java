package yuanian.middleconsole.hyperion.model.vo;

import lombok.Data;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/19
 * @menu: TODO
 */
@Data
public class EsbInfoVO {

    private String INSTID = "";

    private String REQUESTTIME;

    private String RETURNSTATUS;

    private String RETURNCODE;

    private String RETURNMSG;

    private String RESPONSETIME;
}
