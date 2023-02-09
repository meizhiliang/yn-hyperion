package yuanian.middleconsole.hyperion.dao;

import yuanian.middleconsole.hyperion.model.vo.InterfaceLogVO;

import java.util.List;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/12/30
 * @menu: TODO 接口平台日志
 */
public interface InterfaceLogDAO {

    /**
     * 记录接口日志
     * @param record
     * @return
     */
    int insertSelective(InterfaceLogVO record);

    /**
     * 查询接口日志集合
     * @param record
     * @return
     */
    List<InterfaceLogVO> queryList(InterfaceLogVO record);
}