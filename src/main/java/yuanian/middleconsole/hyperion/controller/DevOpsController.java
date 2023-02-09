package yuanian.middleconsole.hyperion.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import yuanian.middleconsole.hyperion.common.model.enums.CommonEnum;
import yuanian.middleconsole.hyperion.dao.DimObjecctDAO;
import yuanian.middleconsole.hyperion.dao.InterfaceLogDAO;
import yuanian.middleconsole.hyperion.dao.SyncAdjustBudgetDAO;
import yuanian.middleconsole.hyperion.model.vo.AdjustBudgetVO;
import yuanian.middleconsole.hyperion.model.vo.EntityMapingVO;
import yuanian.middleconsole.hyperion.model.vo.InterfaceLogVO;
import yuanian.middleconsole.hyperion.service.HlySyncBudgetService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/12/5
 * @menu: TODO
 */
@RequestMapping("/")
@Controller
public class DevOpsController {

    @Autowired
    private DimObjecctDAO dimObjecctDAO;
    @Autowired
    private SyncAdjustBudgetDAO syncAdjustBudgetDAO;
    @Autowired
    private HlySyncBudgetService hlySyncBudgetService;
    @Autowired
    private InterfaceLogDAO interfaceLogDAO;

    /**
     * 进入系统运维首页
     * @return
     */
    @RequestMapping("/index")
    public String goToIndex(){
        return "index";
    }


    /**
     * 查询已维护的映射信息
     * @return
     */
    @ResponseBody
    @RequestMapping("/getEntities")
    public List<EntityMapingVO> getEntities(@RequestBody EntityMapingVO request){
        return dimObjecctDAO.getEntityList(request);
    }
    /**
     * 查询已维护的映射信息
     * @return
     */
    @ResponseBody
    @RequestMapping("/getAdjustBudgets")
    public List<AdjustBudgetVO> getAdjustBudgets(@RequestBody AdjustBudgetVO request){
        List<AdjustBudgetVO> resultList = new ArrayList<>();
        List<AdjustBudgetVO> budgetVOS = syncAdjustBudgetDAO.selectList(request);
        if(!CollectionUtils.isEmpty(budgetVOS)){
            for (AdjustBudgetVO budgetVO : budgetVOS) {
                AdjustBudgetVO adjustBudgetVO = new AdjustBudgetVO();
                BeanUtils.copyProperties(budgetVO,adjustBudgetVO);
                //状态
                if(adjustBudgetVO.getSyncStatus().equals(CommonEnum.A.getFlag())){
                    adjustBudgetVO.setSyncStatus("待同步");
                }
                if(adjustBudgetVO.getSyncStatus().equals(CommonEnum.B.getFlag())){
                    adjustBudgetVO.setSyncStatus("预算系统同步成功");
                }
                if(adjustBudgetVO.getSyncStatus().equals(CommonEnum.C.getFlag())){
                    adjustBudgetVO.setSyncStatus("预算系统同步失败");
                }
                if(adjustBudgetVO.getSyncStatus().equals(CommonEnum.D.getFlag())){
                    adjustBudgetVO.setSyncStatus("汇联易同步失败");
                }
                if(adjustBudgetVO.getSyncStatus().equals(CommonEnum.E.getFlag())){
                    adjustBudgetVO.setSyncStatus("同步完成");
                }
                //类型
                if(adjustBudgetVO.getAdjustType().equals("F")){
                    adjustBudgetVO.setAdjustType("费用类支出");
                }
                if(adjustBudgetVO.getAdjustType().equals("C")){
                    adjustBudgetVO.setAdjustType("资本性支出");
                }
                resultList.add(adjustBudgetVO);
            }
        }
        return resultList;
    }
    /**
     * 拉取调整预算单行数据
     * @return
     */
    @GetMapping("/syncAdjustBudgetById")
    @ResponseBody
    public Map<String,String> syncAdjustBudgetById(String id){
        AdjustBudgetVO adjustBudgetVO = new AdjustBudgetVO();
        adjustBudgetVO.setId(id);
        return hlySyncBudgetService.syncAdjustBudget(adjustBudgetVO);
    }

    /**
     * 查询日志
     * @param interfaceLogVO
     * @return
     */
    @ResponseBody
    @RequestMapping("/getLogs")
    public List<InterfaceLogVO> getLogs(@RequestBody InterfaceLogVO interfaceLogVO){
        if(StringUtils.isNotBlank(interfaceLogVO.getInterTitle())){
            interfaceLogVO.setInterTitle("%"+interfaceLogVO.getInterTitle()+"%");
        }
        List<InterfaceLogVO> queryList = interfaceLogDAO.queryList(interfaceLogVO);
        return queryList;
    }
}
