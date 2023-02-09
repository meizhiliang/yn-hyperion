package yuanian.middleconsole.hyperion.common.service;

import java.time.LocalDateTime;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/12/4
 * @menu: TODO
 */
public interface CommonService {
    /**
     * 获取组织数据
     * @param orgCode
     * @param deptCode
     * @param isExpenses
     * @param isSuffix
     * @return
     */
    String getEntityValue(String orgCode,String deptCode,boolean isExpenses,boolean isSuffix);
    /**
     * 特殊公司的币种映射
     * @param orgCode
     * @param curryCode
     * @return
     */
    String getCurryMapping(String orgCode,String curryCode);

    /**
     * 记录接口调用日志
     * @param instId ESB标识
     * @param interTitle 接口描述
     * @param interMethod 接口方法
     * @param requester 调用方
     * @param respondent 服务方
     * @param requestParameter 请求报文
     * @param responseParameter 响应报文
     * @param result 结果
     * @param requestTime 请求时间
     * @return
     */
    void saveInterLogs(String instId,String interTitle,String interMethod,String requester,String respondent,
                       String requestParameter,String responseParameter,String result,String requestTime);
}
