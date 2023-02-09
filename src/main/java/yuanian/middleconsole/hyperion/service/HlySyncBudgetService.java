package yuanian.middleconsole.hyperion.service;

import yuanian.middleconsole.hyperion.model.vo.AdjustBudgetVO;

import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/11/14
 * @menu: TODO
 */
public interface HlySyncBudgetService {

    /**
     * 获取预算系统的编制预算信息
     * @param requestDataVO
     * @return
     */
    Map<String,Object> syncBudget(Map<String,Object> requestDataVO);

    /**
     * 预算系统调用汇联易接口拉取实际数更新到预算系统
     * @return
     */
    Map<String,String> syncActualData();


    /**
     * 同步调整预算信息
     * @param adjustBudgetVO
     * @return
     */
    Map<String,String> syncAdjustBudget(AdjustBudgetVO adjustBudgetVO);
}
