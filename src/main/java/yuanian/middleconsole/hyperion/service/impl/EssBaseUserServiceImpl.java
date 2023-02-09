package yuanian.middleconsole.hyperion.service.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import yuanian.middleconsole.hyperion.common.model.constants.LogConstants;
import yuanian.middleconsole.hyperion.common.model.enums.CommonEnum;
import yuanian.middleconsole.hyperion.model.vo.*;
import yuanian.middleconsole.hyperion.service.IEssBaseUserService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/19
 * @menu: TODO
 */
@Service
public class EssBaseUserServiceImpl implements IEssBaseUserService {

    private static final Logger logger = LoggerFactory.getLogger(EssBaseUserServiceImpl.class);
    /**
     * date formatt
     */
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public EsbInfoVO createUser(RequestDataVO requestDataVO) {
        logger.info(LogConstants.LOG_ENTER_METHOD,"create new user start:" + JSON.toJSONString(requestDataVO));
        EsbInfoVO esbInfoVO = requestDataVO.getESBINFO();
        Map<String,String> requestinfo = requestDataVO.getREQUESTINFO();
        //参数校验
        esbInfoVO.setRETURNCODE(CommonEnum.WARN.getCode());
        if(null == esbInfoVO || null == requestinfo){
            esbInfoVO.setRETURNMSG(CommonEnum.WARN.getTitle());
            return esbInfoVO;
        }
        if(null == requestinfo.get("userName")){
            esbInfoVO.setRETURNMSG("请求入参对象主键不能为空！");
            return esbInfoVO;
        }
        try{
            //创建用户
            UserFunctionsSample sample = new UserFunctionsSample("","");
            sample.createUser(requestinfo);

            esbInfoVO.setRETURNSTATUS(CommonEnum.SUSS.getFlag());
            esbInfoVO.setRETURNMSG(CommonEnum.SUSS.getTitle());
            esbInfoVO.setRETURNCODE("A001");
            esbInfoVO.setRESPONSETIME(FORMATTER.format(new Date()));
        }catch (Exception ce){
            esbInfoVO.setRETURNSTATUS(CommonEnum.FAIL.getCode());
            esbInfoVO.setRETURNMSG("create new user fail"+ce.getMessage());
            logger.error(LogConstants.LOG_LEAVE_METHOD,JSON.toJSONString(esbInfoVO));
        }
        logger.info(LogConstants.LOG_LEAVE_METHOD,"create new user end:" + JSON.toJSONString(esbInfoVO));
        return esbInfoVO;
    }

    @Override
    public EsbInfoVO updateUser(RequestDataVO requestDataVO) {
        logger.info(LogConstants.LOG_ENTER_METHOD,"update new user start:" + JSON.toJSONString(requestDataVO));
        EsbInfoVO esbInfoVO = requestDataVO.getESBINFO();
        Map<String,String> requestinfo = requestDataVO.getREQUESTINFO();
        //参数校验
        esbInfoVO.setRETURNCODE(CommonEnum.WARN.getCode());
        if(null == esbInfoVO || null == requestinfo){
            esbInfoVO.setRETURNMSG(CommonEnum.WARN.getTitle());
            return esbInfoVO;
        }
        if(null == requestinfo.get("userName")){
            esbInfoVO.setRETURNMSG("请求入参对象主键不能为空！");
            return esbInfoVO;
        }
        try{
            //更新用户
            UserFunctionsSample sample = new UserFunctionsSample("","");
            sample.modifyUser(requestinfo);

            esbInfoVO.setRETURNSTATUS(CommonEnum.SUSS.getFlag());
            esbInfoVO.setRETURNMSG(CommonEnum.SUSS.getTitle());
            esbInfoVO.setRETURNCODE("A001");
            esbInfoVO.setRESPONSETIME(FORMATTER.format(new Date()));
        }catch (Exception ce){
            esbInfoVO.setRETURNSTATUS(CommonEnum.FAIL.getCode());
            esbInfoVO.setRETURNMSG("update new user fail"+ce.getMessage());
            logger.error(LogConstants.LOG_LEAVE_METHOD,JSON.toJSONString(esbInfoVO));
        }
        logger.info(LogConstants.LOG_LEAVE_METHOD,"update new user end:" + JSON.toJSONString(esbInfoVO));
        return esbInfoVO;
    }

    @Override
    public EsbInfoVO deleteUser(RequestDataVO requestDataVO) {
        logger.info(LogConstants.LOG_ENTER_METHOD,"delete new user start:" + JSON.toJSONString(requestDataVO));
        EsbInfoVO esbInfoVO = requestDataVO.getESBINFO();
        Map<String,String> requestinfo = requestDataVO.getREQUESTINFO();
        //参数校验
        esbInfoVO.setRETURNCODE(CommonEnum.WARN.getCode());
        if(null == esbInfoVO || null == requestinfo){
            esbInfoVO.setRETURNMSG(CommonEnum.WARN.getTitle());
            return esbInfoVO;
        }
        if(null == requestinfo.get("userName")){
            esbInfoVO.setRETURNMSG("请求入参对象主键不能为空！");
            return esbInfoVO;
        }
        try{
            String identity = requestinfo.get("externalId");
            //删除用户
            UserFunctionsSample sample = new UserFunctionsSample("","");
            sample.deleteUser(identity);


            esbInfoVO.setRETURNSTATUS(CommonEnum.SUSS.getFlag());
            esbInfoVO.setRETURNMSG(CommonEnum.SUSS.getTitle());
            esbInfoVO.setRETURNCODE("A001");
            esbInfoVO.setRESPONSETIME(FORMATTER.format(new Date()));
        }catch (Exception ce){
            esbInfoVO.setRETURNSTATUS(CommonEnum.FAIL.getCode());
            esbInfoVO.setRETURNMSG("delete new user fail"+ce.getMessage());
            logger.error(LogConstants.LOG_LEAVE_METHOD,JSON.toJSONString(esbInfoVO));
        }
        logger.info(LogConstants.LOG_LEAVE_METHOD,"delete new user end:" + JSON.toJSONString(esbInfoVO));
        return esbInfoVO;
    }
}
