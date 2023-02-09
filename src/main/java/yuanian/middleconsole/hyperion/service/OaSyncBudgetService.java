package yuanian.middleconsole.hyperion.service;

import yuanian.middleconsole.hyperion.model.vo.*;

import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/20
 * @menu: TODO
 */
public interface OaSyncBudgetService {
    /**
     * 同步预算可用余额信息
     * @param requestMap
     * @param esbInfoVO
     * @param resultMap
     * @return
     */
    Map<String,Object> syncAvailableBalance(Map<String,String> requestMap,Map<String,String> esbInfoVO,
                                            Map<String,Object> resultMap,Map<String,Object> requestVO);
    /**
     * 同步可调数额信息
     * @param requestVO
     * @return
     */
    Map<String,Object> syncAdjustable(Map<String,Object> requestVO);
    /**
     * 同步项目投资额及可用余额信息
     * @param requestVO
     * @return
     */
    Map<String,Object> syncProjectInvestment(Map<String,Object> requestVO);
    /**
     * 同步可调预算信息
     * @param requestDataListVO
     * @return
     */
    Map<String,String> syncAdjustBudget(Map<String,Object> requestDataListVO);

    /**
     * 预算系统定时拉取OA系统实际数同步到预算系统
     * @return
     */
    Map<String,String>  syncActualData();

}
