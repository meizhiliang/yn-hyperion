package yuanian.middleconsole.hyperion.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import yuanian.middleconsole.hyperion.model.vo.EntityMapingVO;

import java.util.List;
import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/11/30
 * @menu: TODO 查询关系库数据
 */
@Repository
public interface DimObjecctDAO {
    /**
     * 查询推送汇联编制预算的项目信息
     * @return
     */
    List<Map<String,Object>> getProjectsFromDb();
    /**
     * 查询推送ESB组织映射
     * @param orgCode
     * @param deptCode
     * @return
     */
    String getEntityFromDb(@Param("orgCode")String orgCode,@Param("deptCode")String deptCode);


    /**
     * 根据海波龙的组织查询部门和对象
     * @param orgCode
     * @return
     */
    List<Map<String,String>> getEntityByCode(@Param("orgCode")String orgCode);

    /**
     * 查询推送ESB组织映射
     * @param entityMapingVO
     * @return
     */
    List<EntityMapingVO> getEntityList(EntityMapingVO entityMapingVO);

    /**
     * 查询子级科目
     * @return
     */
    List<String> getChildrenAccount();

    /**
     * 查询子级项目
     * @return
     */
    List<String> getChildrenProject();
    /**
     * 查询账套信息
     * @return
     */
    List<Map<String,String>> getBooksAndOrgs();
}
