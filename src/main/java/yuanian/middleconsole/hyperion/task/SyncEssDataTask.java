package yuanian.middleconsole.hyperion.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yuanian.middleconsole.hyperion.common.model.constants.LogConstants;
import yuanian.middleconsole.hyperion.model.vo.AdjustBudgetVO;
import yuanian.middleconsole.hyperion.service.HlySyncBudgetService;
import yuanian.middleconsole.hyperion.service.OaSyncBudgetService;

import java.util.Calendar;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/24
 * @menu: TODO
 */
@Component
public class SyncEssDataTask {

    private static final Logger logger = LoggerFactory.getLogger(SyncEssDataTask.class);

    @Autowired
    private OaSyncBudgetService oaSyncBudgetService;

    @Autowired
    private HlySyncBudgetService hlySyncBudgetService;

    /**
     * 预算系统定时拉取OA系统实际数同步到预算系统
     * 每隔一个小时执行一次
     */
    @Scheduled(cron = "0 0 05,10,14,19 * * ?")
    public void syncActualDataFromOa() {
        logger.info(LogConstants.LOG_ENTER_METHOD,"=====定时任务同步OA实际数到预算系统 开始 ======" );
        oaSyncBudgetService.syncActualData();
        logger.info(LogConstants.LOG_LEAVE_METHOD,"=====定时任务同步OA实际数到预算系统 结束 ======" );
    }
    /**
     * 预算系统定时拉取汇联易实际数同步到预算系统
     * 每隔一个小时二十分钟执行一次
     */
    @Scheduled(cron = "0 0 06,11,13,20 * * ?")
    public void syncActualDataFromHly() {
        logger.info(LogConstants.LOG_ENTER_METHOD,"=====定时任务同步汇联易实际数到预算系统 开始 ======" );
        hlySyncBudgetService.syncActualData();
        logger.info(LogConstants.LOG_LEAVE_METHOD,"=====定时任务同步汇联易实际数到预算系统 结束======" );
    }

    /**
     * 预算系统定时扫描预算系统调整预算中间表
     * 每隔10分钟运行一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void syncAdjustBudget () {
        logger.info(LogConstants.LOG_ENTER_METHOD,"=====定时任务同步调整预算 开始 ======" );
        AdjustBudgetVO adjustBudgetVO = new AdjustBudgetVO();
        adjustBudgetVO.setNotEqualsStatus("E");
        hlySyncBudgetService.syncAdjustBudget(adjustBudgetVO);
        logger.info(LogConstants.LOG_LEAVE_METHOD,"=====定时任务同步调整预算 结束======" );
    }
}
