package yuanian.middleconsole.hyperion.service;

import yuanian.middleconsole.hyperion.model.vo.*;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/19
 * @menu: TODO
 */
public interface IEssBaseUserService {

    /**
     * 钉钉用户信息创建同步
     * @param requestDataVO
     * @return
     */
    EsbInfoVO createUser(RequestDataVO requestDataVO);
    /**
     * 钉钉用户信息更新同步
     * @param requestDataVO
     * @return
     */
    EsbInfoVO updateUser(RequestDataVO requestDataVO);
    /**
     * 钉钉用户信息删除同步
     * @param requestDataVO
     * @return
     */
    EsbInfoVO deleteUser(RequestDataVO requestDataVO);
}
