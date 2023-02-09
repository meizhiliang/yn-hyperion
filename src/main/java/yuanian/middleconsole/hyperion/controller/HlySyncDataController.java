package yuanian.middleconsole.hyperion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import yuanian.middleconsole.hyperion.model.vo.AdjustBudgetVO;
import yuanian.middleconsole.hyperion.service.HlySyncBudgetService;

import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/11/14
 * @menu: TODO
 */
@RestController
@RequestMapping("/hlyData")
public class HlySyncDataController {

    @Autowired
    private HlySyncBudgetService hlySyncBudgetService;

    /**
     * 获取预算系统的预算信息
     * 接口提供给汇联易系统调用
     * @param requestVO
     * @return
     */
    @PostMapping("/syncBudget")
    public Map<String,Object> syncAdjustBudget(@RequestBody Map<String,Object> requestVO){
        return hlySyncBudgetService.syncBudget(requestVO);
    }

    /**
     * 预算系统调用汇联易接口拉取实际数更新到预算系统
     * @return
     */
    @GetMapping("/syncActualData")
    public Map<String,String>  syncActualData(){
        return hlySyncBudgetService.syncActualData();
    }

    /**
     * 拉取调整预算
     * @return
     */
    @GetMapping("/syncAdjustBudget")
    public Map<String,String>  syncAdjustBudget(){
        AdjustBudgetVO adjustBudgetVO = new AdjustBudgetVO();
        adjustBudgetVO.setNotEqualsStatus("E");
        return hlySyncBudgetService.syncAdjustBudget(adjustBudgetVO);
    }
}
