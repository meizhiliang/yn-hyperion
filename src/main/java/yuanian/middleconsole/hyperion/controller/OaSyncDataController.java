package yuanian.middleconsole.hyperion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import yuanian.middleconsole.hyperion.common.service.CommonService;
import yuanian.middleconsole.hyperion.common.util.CommonUtil;
import yuanian.middleconsole.hyperion.common.util.EssConnectUtil;
import yuanian.middleconsole.hyperion.service.OaSyncBudgetService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/11/14
 * @menu: TODO
 */
@RestController
@RequestMapping("/oaData")
public class OaSyncDataController {

    @Autowired
    private OaSyncBudgetService iBudgetService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private EssConnectUtil essConnectUtil;
    /**
     * 费用科目
     */
    private static final String SUBJECT = "0";

    /**
     * 同步预算可用余额信息 已经调试完
     * @param requestVO
     * @return
     */
    @PostMapping("/syncAvailableBalance")
    public Map<String,Object> syncAvailableBalance(@RequestBody Map<String,Object> requestVO){
        //1 入参对象
        Map<String,Map> jsonObject =(Map<String,Map>) requestVO.get("PROCESS_MAIN_Input");
        Map<String,Map> inputParameters = jsonObject.get("InputParameters");
        Map<String,Map> requestInfo = inputParameters.get("REQUESTINFO");
        Map<String,String> esbInfoVO = inputParameters.get("ESBINFO");
        Map<String,Map> budgetTblInfo = requestInfo.get("P_QUERY_BUDGET_TBL_INFO");
        List<Map<String,String>> itemArray = (List) budgetTblInfo.get("P_QUERY_BUDGET_TBL_INFO_ITEM");
        Map<String,String> requestMap = itemArray.get(0);
        //获取 公司段+部门段+科目段+子目段（没用一般0）+项目段+产品段+公司间（没用）+预留1段（一般为0)顺序，以点做区分“.”）进行拆分成所需要的维度成员。
        String [] strings = requestMap.get("P_CONCATENATED_SEGMENTS").split("\\.");
        String yearDate = requestMap.get("P_PERIOD_DATE").toString().substring(0,4);
        requestMap.put("YEARCODE",essConnectUtil.getYear(yearDate));
        String month = requestMap.get("P_PERIOD_DATE").toString().substring(5,7);
        requestMap.put("PERIODCODE", strings[4].equals(SUBJECT)? CommonUtil.getQuarter(Integer.valueOf(month)):"\"YearTotal\"");
        String curryCode = requestMap.get("P_CURRENY_CODE_1").toString();
        requestMap.put("CURRENYCODE", curryCode);
        requestMap.put("ENTITYCODE", commonService.getEntityValue(strings[0],strings[1],strings[4].equals(SUBJECT),true));
        requestMap.put("PROJECTCODE", strings[4]);
        if(!strings[4].equals(SUBJECT)){
            strings[2] = "KM0615";
        }
        requestMap.put("ACCOUNTCODE", strings[2]);
        Map<String,Object> resultMap = new HashMap<>(3);
        Map<String,Object> outPutMap = new HashMap<>(3);
        outPutMap.put("@xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
        outPutMap.put("@xmlns","http://xmlns.oracle.com/apps/cux/rest/CUX_BUDGET_QUERY/process_main/");
        //1 入参校验
        resultMap.put("OutputParameters",outPutMap);
        return iBudgetService.syncAvailableBalance(requestMap,esbInfoVO,resultMap,requestVO);
    }
    /**
     * 同步可调数额信息
     * @param requestVO
     * @return  已经调试完
     */
    @PostMapping("/syncAdjustable")
    public Map<String,Object> syncAdjustable(@RequestBody Map<String,Object> requestVO){
        return iBudgetService.syncAdjustable(requestVO);
    }
    /**
     * 同步项目投资额及可用余额信息
     * @param requestVO
     * @return
     */
    @PostMapping("/syncProjectInvestment")
    public Map<String,Object> syncProjectInvestment(@RequestBody Map<String,Object> requestVO){
        return iBudgetService.syncProjectInvestment(requestVO);
    }

    /**
     * 同步调整预算数据信息
     * @param requestDataListVO 已经调试完
     * @return
     */
    @PostMapping("/syncAdjustBudget")
    public Map<String,String> syncAdjustBudget(@RequestBody Map<String,Object> requestDataListVO){
        return iBudgetService.syncAdjustBudget(requestDataListVO);
    }

    /**
     * OA同步实际数
     * @return 已经调试完
     */
    @GetMapping("/syncActualData")
    public Map<String,String> syncActualData(){
        Map<String,String> esbInfoVO = iBudgetService.syncActualData();
        return esbInfoVO;
    }
}
