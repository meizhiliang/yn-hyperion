package yuanian.middleconsole.hyperion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import yuanian.middleconsole.hyperion.model.vo.*;
import yuanian.middleconsole.hyperion.service.IEssBaseUserService;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/19
 * @menu: TODO
 */
@RestController
@RequestMapping("/dingdingData")
public class EssBaseDataController {

    @Autowired
    private IEssBaseUserService iEssBaseUserService;

    /**
     * 钉钉创建用户同步
     * @param requestDataVO
     * @return
     */
    @PostMapping("/createUser")
    public EsbInfoVO createUser(@RequestBody RequestDataVO requestDataVO){
        return iEssBaseUserService.createUser(requestDataVO);
    }
    /**
     * 钉钉修改用户同步
     * @param requestDataVO
     * @return
     */
    @PostMapping("/updateUser")
    public EsbInfoVO updateUser(@RequestBody RequestDataVO requestDataVO){
        return iEssBaseUserService.updateUser(requestDataVO);
    }
    /**
     * 钉钉删除用户同步
     * @param requestDataVO
     * @return
     */
    @PostMapping("/deleteUser")
    public EsbInfoVO deleteUser(@RequestBody RequestDataVO requestDataVO){
        return iEssBaseUserService.deleteUser(requestDataVO);
    }
}
