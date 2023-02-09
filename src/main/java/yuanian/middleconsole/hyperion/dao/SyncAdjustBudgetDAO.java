package yuanian.middleconsole.hyperion.dao;
import org.springframework.stereotype.Repository;
import yuanian.middleconsole.hyperion.model.vo.AdjustBudgetVO;

import java.util.List;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/11/30
 * @menu: TODO 调整预算中间表
 */
@Repository
public interface SyncAdjustBudgetDAO {

    /**
     * 查询调整预算中间表信息
     * @param adjustBudgetVO
     * @return
     */
    List<AdjustBudgetVO> selectList(AdjustBudgetVO adjustBudgetVO);
    /**
     *写入调整预算表
     * @param record
     * @return
     */
    int insertSelective(AdjustBudgetVO record);

    /**
     *调整预算表更新状态
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(AdjustBudgetVO record);

}