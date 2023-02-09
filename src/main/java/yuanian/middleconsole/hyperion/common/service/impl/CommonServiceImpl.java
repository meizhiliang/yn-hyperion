package yuanian.middleconsole.hyperion.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yuanian.middleconsole.hyperion.common.model.constants.LogConstants;
import yuanian.middleconsole.hyperion.common.model.enums.CommonEnum;
import yuanian.middleconsole.hyperion.common.service.CommonService;
import yuanian.middleconsole.hyperion.dao.DimObjecctDAO;
import yuanian.middleconsole.hyperion.dao.InterfaceLogDAO;
import yuanian.middleconsole.hyperion.model.vo.InterfaceLogVO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/12/4
 * @menu: TODO
 */

@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private DimObjecctDAO dimObjecctDAO;
    @Autowired
    private InterfaceLogDAO interfaceLogDAO;
    /**
     * 组织拼接
     */
    private static final String SUFFIX = "01";
    /**
     * 美元币种公司
     */
    private static final List<String>  USD_ORG = new ArrayList<>(Arrays.asList("220","210","072",
            "226","174","263","281","047","082","274","335","298"));
    /**
     * 日元币种公司
     */
    private static final List<String>  JPY_ORG = new ArrayList<>(Arrays.asList("217","211",
            "095","172","171"));
    /**
     * 欧元币种公司
     */
    private static final List<String>  EUR_ORG = new ArrayList<>(Arrays.asList("085 ","087"));

    @Override
    public String getEntityValue(String orgCode, String deptCode, boolean flag, boolean isSuffix) {
        String code = isSuffix ? "E"+ orgCode : "E"+ orgCode + SUFFIX;
        String entity = flag ? dimObjecctDAO.getEntityFromDb(orgCode,deptCode): code;
        return entity;
    }

    @Override
    public String getCurryMapping(String orgCode,String curryCode) {
        if(orgCode.equals(CommonEnum.AED.name())){
            curryCode = CommonEnum.AED.getCode();
        }
        if (orgCode.equals(CommonEnum.AUD.name())){
            curryCode = CommonEnum.AUD.getCode();
        }
        if (orgCode.equals(CommonEnum.BRL.name())){
            curryCode = CommonEnum.BRL.getCode();
        }
        if (orgCode.equals(CommonEnum.KRW.name())){
            curryCode = CommonEnum.KRW.getCode();
        }
        if (orgCode.equals(CommonEnum.MYR.name())){
            curryCode = CommonEnum.MYR.getCode();
        }
        if (orgCode.equals(CommonEnum.TRY.name())){
            curryCode = CommonEnum.TRY.getCode();
        }
        if (orgCode.equals(CommonEnum.ZAR.name())){
            curryCode = CommonEnum.ZAR.getCode();
        }
        if (USD_ORG.contains(orgCode)){
            curryCode = CommonEnum.USD.getCode();
        }
        if (JPY_ORG.contains(orgCode)){
            curryCode = CommonEnum.JPY.getCode();
        }
        if (EUR_ORG.contains(orgCode)){
            curryCode = CommonEnum.EUR.getCode();
        }
        return curryCode;
    }

    @Override
    public void saveInterLogs(String instId,String interTitle,String interMethod,String requester,String respondent,String requestParameter,
                              String responseParameter,String result,String requestTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(LogConstants.LACALDATE_TIME_FORMAT_PATTERN);
        InterfaceLogVO interfaceLogVO = new InterfaceLogVO();
        interfaceLogVO.setId(UUID.randomUUID().toString().replace("-","").toUpperCase());
        interfaceLogVO.setInstId(instId);
        interfaceLogVO.setInterTitle(interTitle);
        interfaceLogVO.setInterMethod(interMethod);
        interfaceLogVO.setRequester(requester);
        interfaceLogVO.setRespondent(respondent);
        interfaceLogVO.setRequestParameter(requestParameter);
        interfaceLogVO.setResponseParameter(responseParameter);
        interfaceLogVO.setResult(result);
        interfaceLogVO.setRequestTime(LocalDateTime.parse(requestTime.substring(0,19),df));
        interfaceLogVO.setResponseTime(LocalDateTime.now());
        interfaceLogDAO.insertSelective(interfaceLogVO);
    }
}
