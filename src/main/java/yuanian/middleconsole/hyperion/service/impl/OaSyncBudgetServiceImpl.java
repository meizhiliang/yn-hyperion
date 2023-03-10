package yuanian.middleconsole.hyperion.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.essbase.api.datasource.IEssCube;
import com.essbase.api.datasource.IEssOlapServer;
import com.essbase.api.session.IEssbase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import yuanian.middleconsole.hyperion.common.model.constants.LogConstants;
import yuanian.middleconsole.hyperion.common.model.enums.CommonEnum;
import yuanian.middleconsole.hyperion.common.service.CommonService;
import yuanian.middleconsole.hyperion.common.util.CommonUtil;
import yuanian.middleconsole.hyperion.common.util.EssConnectUtil;
import yuanian.middleconsole.hyperion.common.util.HttpClientUtils;
import yuanian.middleconsole.hyperion.dao.DimObjecctDAO;
import yuanian.middleconsole.hyperion.dao.SyncAdjustBudgetDAO;
import yuanian.middleconsole.hyperion.model.enums.ScennarioEnum;
import yuanian.middleconsole.hyperion.model.enums.VersionsEnum;
import yuanian.middleconsole.hyperion.model.vo.AdjustBudgetVO;
import yuanian.middleconsole.hyperion.model.vo.DimData;
import yuanian.middleconsole.hyperion.model.vo.EntityMapingVO;
import yuanian.middleconsole.hyperion.service.IEssBaseData;
import yuanian.middleconsole.hyperion.service.OaSyncBudgetService;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/20
 * @menu: TODO
 */
@Service
public class OaSyncBudgetServiceImpl implements OaSyncBudgetService {

    private static final Logger logger = LoggerFactory.getLogger(OaSyncBudgetServiceImpl.class);

    @Autowired
    private IEssBaseData iEssBaseData;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DimObjecctDAO dimObjecctDAO;
    @Autowired
    private SyncAdjustBudgetDAO syncAdjustBudgetDAO;
    @Autowired
    private EssConnectUtil essConnectUtil;


    @Value("${OA_URL}")
    private String OA_URL;

    @Value("${OA_USERNAME}")
    private String OA_USERNAME;

    @Value("${OA_PASSWORD}")
    private String OA_PASSWORD;

    /**
     * ????????????
     */
    private static final String SUBJECT = "0";

    /**
     * ??????????????????
     */
    private static final List<String> ACCOUNTS = new ArrayList<>(Arrays.asList("218102A","113398A","660301","160199","130101A","113301A","113304A","22210101","22210111","640308","640309"));

    @Override
    public Map<String,Object> syncAvailableBalance(Map<String,String> requestMap,Map<String,String> esbInfoVO,
                                                   Map<String,Object> resultMap,Map<String,Object> requestVO) {
        logger.info(LogConstants.LOG_ENTER_METHOD,"update budget balance start:" + JSON.toJSONString(requestMap));
        //????????????
        Map<String,Object> outPutMap = (Map<String,Object>)resultMap.get("OutputParameters");
        //2 ??????????????????
        Map<String,Object> responseMap = new HashMap<>(3);
        Map<String,Object> tempMap = new HashMap<>(3);
        List<Map<String,Object>> tempMapList = new ArrayList<>();
        Map<String,Object> valueMap = new HashMap<>(2);
        //3 ???????????????
        Map map = essConnectUtil.connectEssbase();
        IEssbase ess = (IEssbase) map.get("ess");
        IEssOlapServer olapSvr = (IEssOlapServer) map.get("olapSvr");
        try {
            String account = requestMap.get("ACCOUNTCODE").toString();
            if(ACCOUNTS.contains(account) || account.startsWith("9999") || account.endsWith("9999")||
                    account.startsWith("oa9999") || account.endsWith("oa9999")){
                CommonUtil.packageObj(esbInfoVO,CommonEnum.SUSS.getFlag(),"OA??????????????????",null);
                outPutMap.put("ESBINFO",esbInfoVO);
                valueMap.put("X_ERROR_CODE","F");
                valueMap.put("X_ERROR_MESSAGE","??????");
                tempMapList.add(valueMap);
                tempMap.put("X_QUERY_BUDGET_TBL_INFO_ITEM",tempMapList);
                responseMap.put("X_QUERY_BUDGET_TBL_INFO",tempMap);
                outPutMap.put("RESPONSEINFO",responseMap);
                resultMap.put("OutputParameters",outPutMap);
                return resultMap;
            }
            IEssCube cube = olapSvr.getApplication(essConnectUtil.getAppName()).getCube(essConnectUtil.getAppCube());
            //4 ??????OA????????????????????????
            BigDecimal oaActual = null; BigDecimal oaBudget = null;
            boolean isExpenses = SUBJECT.equals(requestMap.get("PROJECTCODE"));
            if(isExpenses){
                oaActual = new BigDecimal(requestMap.get("P_Q1")).add(new BigDecimal(requestMap.get("P_Q2")))
                        .add(new BigDecimal(requestMap.get("P_Q3"))).add(new BigDecimal(requestMap.get("P_Q4")));
                oaBudget = new BigDecimal(requestMap.get("P_Q1_U")).add(new BigDecimal(requestMap.get("P_Q2_U")))
                        .add(new BigDecimal(requestMap.get("P_Q3_U"))).add(new BigDecimal(requestMap.get("P_Q4_U")));
            }else{
                oaActual = new BigDecimal(requestMap.get("P_YTD")).add(new BigDecimal(requestMap.get("P_PTD")));
                oaBudget = new BigDecimal(requestMap.get("P_YTD_U")).add(new BigDecimal(requestMap.get("P_PTD_U")));
            }
            //5 ?????????????????????????????????????????????
            BigDecimal addOaTotal = this.getAmoutData(requestMap,VersionsEnum.OA_ver.getCode(),cube,CommonEnum.QUERY_A.getFlag());
            if(addOaTotal.compareTo(BigDecimal.ZERO)==0){
                valueMap.put("X_OA_T",oaActual.add(oaBudget).toString());
                valueMap.put("X_BUDGET_T",oaActual.add(oaBudget).toString());
                valueMap.put("X_ERROR_CODE","N");
                valueMap.put("X_ERROR_MESSAGE","??????????????????");
            }else {
                //OA????????? OA?????? = OA?????????+OA?????????+??????????????????+??????????????????
                BigDecimal decimalActual = oaActual.add(oaBudget);
                //?????????????????? ???????????????=?????????+?????????-OA?????????-OA?????????-??????????????????-??????????????????
                BigDecimal decimalBlance = addOaTotal.subtract(decimalActual);
                valueMap.put("X_OA_T",decimalActual.toString());
                valueMap.put("X_BUDGET_T",decimalBlance.toString());
                if(decimalBlance.compareTo(BigDecimal.ZERO)>0){
                    valueMap.put("X_ERROR_CODE","");
                    valueMap.put("X_ERROR_MESSAGE","????????????");
                }else{
                    valueMap.put("X_ERROR_CODE","");
                    valueMap.put("X_ERROR_MESSAGE","????????????");
                }
            }
            CommonUtil.packageObj(esbInfoVO,CommonEnum.SUSS.getFlag(),"OA??????????????????",null);
            tempMapList.add(valueMap);
            tempMap.put("X_QUERY_BUDGET_TBL_INFO_ITEM",tempMapList);
            responseMap.put("X_QUERY_BUDGET_TBL_INFO",tempMap);
            outPutMap.put("ESBINFO",esbInfoVO);
            outPutMap.put("RESPONSEINFO",responseMap);
            resultMap.put("OutputParameters",outPutMap);
        } catch (Exception e) {
            CommonUtil.packageObj(esbInfoVO,CommonEnum.FAIL.getFlag(),"OA??????????????????",e.getMessage());
            outPutMap.put("ESBINFO",esbInfoVO);
            valueMap.put("X_ERROR_CODE","E");
            valueMap.put("X_ERROR_MESSAGE",CommonEnum.FAIL.getTitle()+e.getMessage());
            tempMapList.add(valueMap);
            tempMap.put("X_QUERY_BUDGET_TBL_INFO_ITEM",tempMapList);
            responseMap.put("X_QUERY_BUDGET_TBL_INFO",tempMap);
            outPutMap.put("RESPONSEINFO",responseMap);
            resultMap.put("OutputParameters",outPutMap);
            logger.error("update budget balance to essbase Error: " + e);
        } finally {
            essConnectUtil.closeEssConnect(ess,olapSvr);
            String result = valueMap.get("X_ERROR_CODE").equals(CommonEnum.FAIL.getFlag())? CommonEnum.FAIL.getFlag(): CommonEnum.SUSS.getFlag();
            commonService.saveInterLogs(esbInfoVO.get("INSTID"),"????????????????????????","syncAvailableBalance","OA??????",
                    "????????????", JSON.toJSONString(requestVO),JSON.toJSONString(resultMap),result,esbInfoVO.get("REQUESTTIME"));
        }
        logger.info(LogConstants.LOG_LEAVE_METHOD,"update budget balance end:" + JSON.toJSONString(resultMap));
        return resultMap;
    }
    @Override
    public Map<String,Object> syncAdjustable(Map<String,Object> requestVO) {
        logger.info(LogConstants.LOG_ENTER_METHOD,"sync adjustable balance start:" + JSON.toJSONString(requestVO));
        //????????????
        Map<String,Object> resultMap  = new HashMap<>(2);
        //????????????
        Map<String,String> esbInfoVO = (Map) requestVO.get("ESBINFO");
        Map<String,String> adjustEsbVO =  (Map) requestVO.get("REQUESTINFO");
        //1 ???????????????????????????
        boolean checkParameter = null == adjustEsbVO.get("PROJECTCODE") || null == adjustEsbVO.get("ACCOUNTCODE") ||
                null == adjustEsbVO.get("ENTITYCODE") || null == adjustEsbVO.get("DEPTCODE") ||
                null == adjustEsbVO.get("YEARCODE") || null == adjustEsbVO.get("CURRENYCODE");
        if(null == esbInfoVO || null == adjustEsbVO || checkParameter){
            CommonUtil.packageObj(esbInfoVO,CommonEnum.WARN.getFlag(),"OA????????????", null);
            resultMap.put("ESBINFO", esbInfoVO);
            return resultMap;
        }
        //2 ???????????????
        Map map = essConnectUtil.connectEssbase();
        IEssbase ess = (IEssbase) map.get("ess");
        IEssOlapServer olapSvr = (IEssOlapServer) map.get("olapSvr");
        try{
            //??????OA??????????????????
            String total1 = StringUtils.isBlank(adjustEsbVO.get("P_BUD").toString())?"0":adjustEsbVO.get("P_BUD").toString();
            String total2 = StringUtils.isBlank(adjustEsbVO.get("P_BUD_U").toString())?"0":adjustEsbVO.get("P_BUD_U").toString();
            String total3 = StringUtils.isBlank(adjustEsbVO.get("P_ACT").toString())?"0":adjustEsbVO.get("P_ACT").toString();
            String total4 = StringUtils.isBlank(adjustEsbVO.get("P_ACT_U").toString())?"0":adjustEsbVO.get("P_ACT_U").toString();
            BigDecimal oaAdjust = new BigDecimal(total1).add(new BigDecimal(total2)).subtract(new BigDecimal(total3)).subtract(new BigDecimal(total4));
            IEssCube cube = olapSvr.getApplication(essConnectUtil.getAppName()).getCube(essConnectUtil.getAppCube());
            boolean isExpenses = SUBJECT.equals(adjustEsbVO.get("PROJECTCODE"));
            String code = commonService.getEntityValue(adjustEsbVO.get("ENTITYCODE").toString(),adjustEsbVO.get("DEPTCODE").toString(),isExpenses,true);
            adjustEsbVO.put("YEARCODE",essConnectUtil.getYear(adjustEsbVO.get("YEARCODE").toString()));
            adjustEsbVO.put("ENTITYCODE", code);
            if(!isExpenses){
                adjustEsbVO.put("ACCOUNTCODE", "KM0615");
            }
            //3 ?????????????????????????????????????????????
            String period = adjustEsbVO.get("PERIODCODE");
            if(StringUtils.isNotBlank(period) && period.startsWith("0")){
                period = period.replace("0","");
            }
            adjustEsbVO.put("PERIODCODE","\""+period+"\"");
            BigDecimal addHlyTotal = this.getAmoutData(adjustEsbVO,VersionsEnum.HLY_ver.getCode(),cube,CommonEnum.QUERY_B.getFlag());
            //4 ??????????????????????????????
            adjustEsbVO.put("PERIODCODE","\"YearTotal\"");
            BigDecimal addTotal = this.getAmoutData(adjustEsbVO,VersionsEnum.OA_ver.getCode(),cube,CommonEnum.QUERY_C.getFlag());
            //????????????????????? ???????????? =?????????+??????????????????-?????????????????????-?????????????????????-??????????????????-??????????????????
            BigDecimal adjustTotal = addTotal.add(oaAdjust).subtract(addHlyTotal);
            //??????????????????
            adjustEsbVO.put("P_RET_BUD",adjustTotal.toString());
            adjustEsbVO.put("P_RET_BUD_CON","0");
            //5 ?????????????????????????????????????????????????????????????????????
            boolean flag = !adjustEsbVO.get("P_ACT_CON").toString().equals("0") || !adjustEsbVO.get("P_ACT_CON_U").toString().equals("0")
                    || !adjustEsbVO.get("P_BUD_CON").toString().equals("0") || !adjustEsbVO.get("P_BUD_CON_U").toString().equals("0");
            if(flag){
                adjustEsbVO.put("ACCOUNTCODE", "KM0630");
                //??????OA???????????????
                BigDecimal oaInvest = new BigDecimal(adjustEsbVO.get("P_BUD_CON")).add(new BigDecimal(adjustEsbVO.get("P_BUD_CON_U")))
                        .subtract(new BigDecimal(adjustEsbVO.get("P_ACT_CON"))).subtract(new BigDecimal(adjustEsbVO.get("P_ACT_CON_U")));
                adjustEsbVO.put("PERIODCODE","\"QJ99\"");
                BigDecimal invest = this.getAmoutData(adjustEsbVO,VersionsEnum.OA_ver.getCode(),cube,CommonEnum.QUERY_C.getFlag());
                adjustEsbVO.put("P_RET_BUD_CON",oaInvest.add(invest).toString());
            }
            //??????????????????????????????????????????
            if(isExpenses){
                Map<String,BigDecimal> quartarMap  = this.getAmoutQuarterData(adjustEsbVO,cube);
                BigDecimal q1 = new BigDecimal(adjustEsbVO.get("P_BUD_Q1")).add(new BigDecimal(adjustEsbVO.get("P_BUD_U_Q1")));
                BigDecimal q2 = new BigDecimal(adjustEsbVO.get("P_BUD_Q2")).add(new BigDecimal(adjustEsbVO.get("P_BUD_U_Q2")));
                BigDecimal q3 = new BigDecimal(adjustEsbVO.get("P_BUD_Q3")).add(new BigDecimal(adjustEsbVO.get("P_BUD_U_Q3")));
                BigDecimal q4 = new BigDecimal(adjustEsbVO.get("P_BUD_Q4")).add(new BigDecimal(adjustEsbVO.get("P_BUD_U_Q4")));
                adjustEsbVO.put("P_RET_BUD_Q1",quartarMap.get("Q1").add(q1).toString());
                adjustEsbVO.put("P_RET_BUD_Q2",quartarMap.get("Q2").add(q2).toString());
                adjustEsbVO.put("P_RET_BUD_Q3",quartarMap.get("Q3").add(q3).toString());
                adjustEsbVO.put("P_RET_BUD_Q4",quartarMap.get("Q4").add(q4).toString());
            }
            CommonUtil.packageObj(esbInfoVO,CommonEnum.SUSS.getFlag(),"OA????????????", null);
            resultMap.put("ESBINFO", esbInfoVO);
            resultMap.put("RESULTINFO", adjustEsbVO);
        }catch (Exception e) {
            CommonUtil.packageObj(esbInfoVO,CommonEnum.FAIL.getFlag(),"OA????????????", e.getMessage());
            resultMap.put("ESBINFO", esbInfoVO);
            logger.error("sync adjustable balance to essbase Error: " + e);
        } finally {
            essConnectUtil.closeEssConnect(ess,olapSvr);
            String result = esbInfoVO.get("RETURNSTATUS").equals(CommonEnum.FAIL.getFlag())? CommonEnum.FAIL.getFlag(): CommonEnum.SUSS.getFlag();
            commonService.saveInterLogs(esbInfoVO.get("INSTID"),"??????????????????","syncAdjustable","OA??????",
                    "????????????", JSON.toJSONString(requestVO),JSON.toJSONString(resultMap),result,esbInfoVO.get("REQUESTTIME"));
        }
        logger.info(LogConstants.LOG_ENTER_METHOD,"sync adjustable balance end:" + JSON.toJSONString(resultMap));
        return resultMap;
    }

    @Override
    public Map<String,Object> syncProjectInvestment(Map<String,Object> requestVO) {
        logger.info(LogConstants.LOG_ENTER_METHOD,"sync projectInvestment balance start:" + JSON.toJSONString(requestVO));
        //????????????
        Map<String,Object> resultMap  = new HashMap<>(2);
        //????????????
        Map<String,String> esbInfoVO = (Map) requestVO.get("ESBINFO");
        Map<String,String> adjustEsbVO =  (Map) requestVO.get("REQUESTINFO");
        //1 ???????????????????????????
        boolean checkParameter = null == adjustEsbVO.get("PROJECTCODE") || null == adjustEsbVO.get("ACCOUNTCODE") ||
                                 null == adjustEsbVO.get("ENTITYCODE") || null == adjustEsbVO.get("DEPTCODE") ||
                                 null == adjustEsbVO.get("YEARCODE") || null == adjustEsbVO.get("CURRENYCODE");
        if(null == requestVO || null == adjustEsbVO ||checkParameter){
            CommonUtil.packageObj(esbInfoVO,CommonEnum.WARN.getFlag(),"OA???????????????", null);
            resultMap.put("ESBINFO", esbInfoVO);
            return resultMap;
        }
        //2 ???????????????
        Map map = essConnectUtil.connectEssbase();
        IEssbase ess = (IEssbase) map.get("ess");
        IEssOlapServer olapSvr = (IEssOlapServer) map.get("olapSvr");
        try{
            IEssCube cube = olapSvr.getApplication(essConnectUtil.getAppName()).getCube(essConnectUtil.getAppCube());
            //??????OA?????????????????????
            BigDecimal oaAdjust = new BigDecimal(adjustEsbVO.get("P_ACT")).add(new BigDecimal(adjustEsbVO.get("P_ACT_U")));
            adjustEsbVO.put("PERIODCODE","\"QJ99\"");
            boolean isExpenses = SUBJECT.equals(adjustEsbVO.get("PROJECTCODE"));
            String code = commonService.getEntityValue(adjustEsbVO.get("ENTITYCODE").toString(),adjustEsbVO.get("DEPTCODE").toString(),isExpenses,true);
            adjustEsbVO.put("YEARCODE", essConnectUtil.getYear(adjustEsbVO.get("YEARCODE")));
            adjustEsbVO.put("ENTITYCODE", code);
            if(!isExpenses){
                adjustEsbVO.put("ACCOUNTCODE", "KM0630");
            }
            //3 ??????????????????????????????????????????
            BigDecimal addTotal = this.getAmoutData(adjustEsbVO,VersionsEnum.OA_ver.getCode(),cube,CommonEnum.QUERY_A.getFlag());
            //????????????????????? ???????????? =  ?????????????????????+?????????????????????-OA??????????????????????????????-???OA???????????????????????????
            BigDecimal adjustTotal = addTotal.subtract(oaAdjust);
            adjustEsbVO.put("P_RET_BUD",adjustTotal.toString());
            CommonUtil.packageObj(esbInfoVO,CommonEnum.SUSS.getFlag(),"OA???????????????", null);
            resultMap.put("ESBINFO", esbInfoVO);
            resultMap.put("RESULTINFO", adjustEsbVO);
        }catch (Exception e) {
            CommonUtil.packageObj(esbInfoVO,CommonEnum.FAIL.getFlag(),"OA???????????????", e.getMessage());
            resultMap.put("ESBINFO", esbInfoVO);
            logger.error("sync projectInvestment balance to essbase Error: " + e);
        } finally {
            essConnectUtil.closeEssConnect(ess,olapSvr);
            String result = esbInfoVO.get("RETURNSTATUS").equals(CommonEnum.FAIL.getFlag())? CommonEnum.FAIL.getFlag(): CommonEnum.SUSS.getFlag();
            commonService.saveInterLogs(esbInfoVO.get("INSTID"),"???????????????????????????","syncProjectInvestment","OA??????",
                    "????????????", JSON.toJSONString(requestVO),JSON.toJSONString(resultMap),result,esbInfoVO.get("REQUESTTIME"));
        }
        logger.info(LogConstants.LOG_ENTER_METHOD,"sync projectInvestment balance end:" + JSON.toJSONString(resultMap));
        return resultMap;
    }

    @Override
    public Map<String,String> syncAdjustBudget(Map<String,Object> requestDataListVO) {
        logger.info(LogConstants.LOG_ENTER_METHOD,"sync adjust budget start:" + JSON.toJSONString(requestDataListVO));
        Map<String,String> esbInfoVO = (Map) requestDataListVO.get("ESBINFO");
        List<Map<String,String>> mapList = (List<Map<String,String>>) requestDataListVO.get("REQUESTINFO");
        if(null == esbInfoVO || CollectionUtils.isEmpty(mapList)){
            CommonUtil.packageObj(esbInfoVO,CommonEnum.WARN.getFlag(),"OA???????????????",null);
            return esbInfoVO;
        }
        //??????????????????
        SimpleDateFormat dateFormat = new SimpleDateFormat(LogConstants.DATE_FORMAT_PATTERN);
        String batchNo = dateFormat.format(new Date());
        for (Map<String, String> map : mapList) {
            AdjustBudgetVO adjustBudgetVO = new AdjustBudgetVO();
            adjustBudgetVO.setId(UUID.randomUUID().toString().replace("-","").toUpperCase());
            adjustBudgetVO.setBatchNo(batchNo);
            adjustBudgetVO.setAdjustType(map.get("ADJUSTTYPE"));
            adjustBudgetVO.setAccountCode(map.get("ACCOUNTCODE"));
            adjustBudgetVO.setEntityCode(map.get("ENTITYCODE"));
            adjustBudgetVO.setDeptCode(map.get("DEPTCODE"));
            adjustBudgetVO.setYearCode(map.get("YEARCODE"));
            adjustBudgetVO.setPeriodCode(map.get("PERIODCODE"));
            adjustBudgetVO.setProjectCode(map.get("PROJECTCODE"));
            adjustBudgetVO.setCurrenyCode(map.get("CURRENYCODE"));
            if(null!=map.get("P_BUD_Q1") && StringUtils.isNotBlank(map.get("P_BUD_Q1").toString())){
                adjustBudgetVO.setQ1Amount(new BigDecimal(map.get("P_BUD_Q1").toString()));
            }
            if(null!=map.get("P_BUD_Q2") && StringUtils.isNotBlank(map.get("P_BUD_Q2").toString())){
                adjustBudgetVO.setQ2Amount(new BigDecimal(map.get("P_BUD_Q2").toString()));
            }
            if(null!=map.get("P_BUD_Q3") && StringUtils.isNotBlank(map.get("P_BUD_Q3").toString())){
                adjustBudgetVO.setQ3Amount(new BigDecimal(map.get("P_BUD_Q3").toString()));
            }
            if(null!=map.get("P_BUD_Q4") && StringUtils.isNotBlank(map.get("P_BUD_Q4").toString())){
                adjustBudgetVO.setQ4Amount(new BigDecimal(map.get("P_BUD_Q4").toString()));
            }
            if(null!=map.get("P_BUD_CAP") && StringUtils.isNotBlank(map.get("P_BUD_CAP").toString())){
                adjustBudgetVO.setAdjustAmount(new BigDecimal(map.get("P_BUD_CAP").toString()));
            }
            if(null!=map.get("P_BUD_CO") && StringUtils.isNotBlank(map.get("P_BUD_CO").toString())){
                adjustBudgetVO.setInvestAdjustAmount(new BigDecimal(map.get("P_BUD_CO").toString()));
            }
            adjustBudgetVO.setCreateTime(LocalDateTime.now());
            adjustBudgetVO.setUpdateTime(LocalDateTime.now());
            adjustBudgetVO.setSyncStatus(CommonEnum.A.getFlag());
            adjustBudgetVO.setSyncMsg("OA????????????");
            syncAdjustBudgetDAO.insertSelective(adjustBudgetVO);
        }
        CommonUtil.packageObj(esbInfoVO,CommonEnum.SUSS.getFlag(),"OA???????????????",null);
        String result =  CommonEnum.SUSS.getFlag();
        commonService.saveInterLogs(esbInfoVO.get("INSTID"),"???????????????????????????","syncAdjustBudget","OA??????",
                "????????????", JSON.toJSONString(requestDataListVO),JSON.toJSONString(esbInfoVO),result,esbInfoVO.get("REQUESTTIME"));
        return esbInfoVO;
    }

    @Override
    public Map<String,String> syncActualData() {
        Map<String,String> esbInfoVO = new HashMap<>(2);
        Map<String,Object> requestInfo = new HashMap<>(3);
        CommonUtil.packageObj(esbInfoVO,CommonEnum.REQUEST_UPPER.getFlag(),"OA?????????",null);
        requestInfo.put("ESBINFO",esbInfoVO);
        requestInfo.put("REQUESTINFO",this.setRequestMap());
        //2 ???????????????
        Map map = essConnectUtil.connectEssbase();
        IEssbase ess = (IEssbase) map.get("ess");
        IEssOlapServer olapSvr = (IEssOlapServer) map.get("olapSvr");
        String result = "" ;
        //????????????
        try {
            logger.info(" ??????OA?????????????????? ???" + JSONObject.toJSONString(requestInfo));
            //3 ????????????
            result = HttpClientUtils.doPostJson(OA_URL, JSONObject.toJSONString(requestInfo),OA_USERNAME,OA_PASSWORD);
            logger.info(" ??????OA?????????????????? ???" + result);
            JSONObject resultJson = JSON.parseObject(result);
            JSONObject data = JSON.parseObject(resultJson.getString("data"));
            JSONObject esbinfo = JSON.parseObject(data.getString("ESBINFO"));
            if(CommonEnum.OA_RESPONSE.getCode().equals(resultJson.getString("code"))){
                JSONArray resultInfo = JSON.parseArray(data.getString("RESULTINFO"));
                int noMatchCount = this.loadActualData(resultInfo,olapSvr);
                int succCount = resultInfo.size()-noMatchCount;
                String massge = "OA?????????????????????"+succCount+"???????????????????????????"+noMatchCount+"????????????????????????????????????????????????????????????";
                CommonUtil.packageObj(esbInfoVO,CommonEnum.SUSS.getFlag(),massge,null);
            }else{
                CommonUtil.packageObj(esbInfoVO,CommonEnum.MISS.getFlag(),"OA?????????",null);
            }
            esbInfoVO.put("INSTID",esbinfo.getString("INSTID"));
        } catch (Exception e) {
            CommonUtil.packageObj(esbInfoVO,CommonEnum.FAIL.getFlag(),"OA?????????",e.getMessage());
            logger.error("sync actual data  to essbase Error: " + e);
        }finally {
            essConnectUtil.closeEssConnect(ess,olapSvr);
            String logFlag = esbInfoVO.get("RETURNSTATUS").equals(CommonEnum.FAIL.getFlag())? CommonEnum.FAIL.getFlag(): CommonEnum.SUSS.getFlag();
            commonService.saveInterLogs(esbInfoVO.get("INSTID"),"???????????????","syncActualData","????????????",
                    "OA??????", JSON.toJSONString(requestInfo),JSON.toJSONString(result),logFlag,esbInfoVO.get("REQUESTTIME"));
        }
        logger.info(LogConstants.LOG_ENTER_METHOD,"sync actual data end:" + JSON.toJSONString(esbInfoVO));
        return esbInfoVO;
    }

    /**
     * ??????OA???????????????
     * @return
     */
    private  Map<String,String> setRequestMap(){
        Map<String,String> requestMap = new HashMap<>(3);
        Calendar calendar = Calendar.getInstance();
        // ???????????????
        int year = calendar.get(Calendar.YEAR);
        // ???????????????
        int month = calendar.get(Calendar.MONTH) + 1;
        requestMap.put("YEARCODE",String.valueOf(year));
        requestMap.put("PERIODCODE",String.valueOf(month));
        //????????????
        List<String> childrenAccount = dimObjecctDAO.getChildrenAccount();
        //????????????
        List<String> childrenProject = dimObjecctDAO.getChildrenProject();
        //????????????
        List<EntityMapingVO> entityList = dimObjecctDAO.getEntityList(new EntityMapingVO());
        Set<String> entitys = entityList.stream().map(EntityMapingVO::getEbsCode).collect(Collectors.toSet());
        //??????????????????-????????????
        entitys.add("671197");
        requestMap.put("ACCOUNTCODE",StringUtils.join(childrenAccount.toArray(), ","));
        requestMap.put("PROJECTCODE",StringUtils.join(childrenProject.toArray(), ","));
        requestMap.put("ENTITYCODE",StringUtils.join(entitys.toArray(), ","));
        return requestMap;
    }

    /**
     * ??????????????????????????????
     * @param requestVO
     * @param cube
     * @return
     * @throws Exception
     */
    private Map<String,BigDecimal> getAmoutQuarterData(Map<String,String> requestVO,IEssCube cube) throws Exception{
        //??????????????????
        Map<String,BigDecimal> quartarMap = new HashMap<>(2);
        StringBuffer sb = new StringBuffer();
        sb.append("//ESS_LOCALE SimplifiedChinese_China.MS936@Binary\n");
        sb.append("<Sym\n").append("{SUPALL}{TABDELIMIT} {NAMESON}{ROWREPEAT}{NOINDENTGEN}{SUPMISSINGROWS}\n");
        sb.append("{MISSINGTEXT \"0\"}\n");
        sb.append("<COLUMN(\"Periods\")\n");
        sb.append("<Row(").append("Account").append(",Periods").append(",Years");
        sb.append(",Scenario").append(",Versions").append(",Entity").append(",Product");
        sb.append(",Material").append(",Auxiliary").append(",Synthesis").append(",Currency");
        sb.append(",Spare").append(",Area").append(",Project)").append("{ ROWREPEAT }\n").append("{decimal 2}\n");
        String massge = "??????????????????????????????";
        //??????????????? ???????????? ??????????????????
        //String accountCode = requestVO.get("ACCOUNTCODE").toString().equals(SPS_ACCOUNT)?"66029701":requestVO.get("ACCOUNTCODE").toString();
        String accountCode = requestVO.get("ACCOUNTCODE").toString();
        sb.append("\""+accountCode+"\"\n");
        //???????????????
        sb.append(CommonUtil.getQuarter(12)+"\n").append("\""+requestVO.get("YEARCODE")+"\"\n");
        //??????
        sb.append("\""+ScennarioEnum.S02.getCode()+"\"\n");
        //??????
        sb.append("\""+VersionsEnum.OA_ver.getCode()+"\"\n");
        //??????
        sb.append("\""+requestVO.get("ENTITYCODE")+"\"\n");
        //????????????????????????????????????????????????????????????
        sb.append("\"CP99\"\n").append("\"WL99\"\n").append("\"FZ99\"\n").append("\"ZH99\"\n");
        sb.append("\""+requestVO.get("CURRENYCODE")+"\"\n").append("\"BY99\"\n").append("\"QY99\"\n");
        sb.append("\"XM99\"\n!");
        logger.info(massge +"?????????" + sb.toString());
        DimData dataBudget = iEssBaseData.executeReport(cube, IEssBaseData.ReportType.scriptString,sb.toString());
        logger.info(massge +"?????????" + JSON.toJSONString(dataBudget.getData()));
        String [] strings = null;
        if(StringUtils.isNotBlank(dataBudget.getData())){
            String []data = dataBudget.getData().split("\n");
            strings = data[0].split("\t");
        }
        quartarMap.put("Q1",null == strings ? BigDecimal.ZERO : new BigDecimal(strings[strings.length-4]));
        quartarMap.put("Q2",null == strings ? BigDecimal.ZERO : new BigDecimal(strings[strings.length-3]));
        quartarMap.put("Q3",null == strings ? BigDecimal.ZERO : new BigDecimal(strings[strings.length-2]));
        quartarMap.put("Q4",null == strings ? BigDecimal.ZERO : new BigDecimal(strings[strings.length-1]));
        return quartarMap;
    }


    /**
     * ??????????????????????????????
     * @param requestVO
     * @param versionCode
     * @param cube
     * @param flag ????????????
     * @return
     * @throws Exception
     */
    private BigDecimal getAmoutData(Map<String,String> requestVO,String versionCode,IEssCube cube,
                                    String flag) throws Exception{
        //??????????????????
        BigDecimal total = BigDecimal.ZERO;
        StringBuffer sb = new StringBuffer();
        sb.append("//ESS_LOCALE SimplifiedChinese_China.MS936@Binary\n");
        sb.append("<Sym\n").append("{SUPALL}{TABDELIMIT} {NAMESON}{ROWREPEAT}{NOINDENTGEN}{SUPMISSINGROWS}\n");
        sb.append("{MISSINGTEXT \"0\"}\n");
        sb.append("<COLUMN(\"Scenario\")\n");
        sb.append("<Row(").append("Account").append(",Periods").append(",Years");
        sb.append(",Scenario").append(",Versions").append(",Entity").append(",Product");
        sb.append(",Material").append(",Auxiliary").append(",Synthesis").append(",Currency");
        sb.append(",Spare").append(",Area").append(",Project)").append("{ ROWREPEAT }\n").append("{decimal 2}\n");
        String massge = "";
        //??????????????? ???????????? ??????????????????
        String accountCode = requestVO.get("ACCOUNTCODE").toString();
        sb.append("\""+accountCode+"\"\n");
        //???????????????
        sb.append(requestVO.get("PERIODCODE")+"\n").append("\""+requestVO.get("YEARCODE")+"\"\n");
        //??????
        if(flag.equals(CommonEnum.QUERY_A.getFlag())){
            sb.append("\""+ScennarioEnum.S02.getCode()+"\" ").append("\""+ScennarioEnum.S03.getCode()+"\"\n");
            massge = "??????????????????????????????????????????";
        }
        if(flag.equals(CommonEnum.QUERY_B.getFlag())){
            sb.append("\""+ScennarioEnum.S14.getCode()+"\" ").append("\""+ScennarioEnum.S15.getCode()+"\"\n");
            massge = "?????????????????????????????????????????????";
        }
        if(flag.equals(CommonEnum.QUERY_C.getFlag())){
            sb.append("\""+ScennarioEnum.S02.getCode()+"\"\n");
            massge = "??????????????????????????????";
        }
        //??????
        sb.append("\""+versionCode+"\"\n");
        //??????
        sb.append("\""+requestVO.get("ENTITYCODE")+"\"\n");
        //????????????????????????????????????????????????????????????
        sb.append("\"CP99\"\n").append("\"WL99\"\n").append("\"FZ99\"\n").append("\"ZH99\"\n");
        sb.append("\""+requestVO.get("CURRENYCODE")+"\"\n").append("\"BY99\"\n").append("\"QY99\"\n");
        if(SUBJECT.equals(requestVO.get("PROJECTCODE"))){
            sb.append("\"XM99\"\n!");
        }else{
            sb.append("\""+requestVO.get("PROJECTCODE")+"\"\n!");
        }
        logger.info(massge +"?????????" + sb.toString());
        DimData dataBudget = iEssBaseData.executeReport(cube, IEssBaseData.ReportType.scriptString,sb.toString());
        logger.info(massge +"?????????" + JSON.toJSONString(dataBudget.getData()));
        if(StringUtils.isNotBlank(dataBudget.getData())){
            String []data = dataBudget.getData().split("\n");
            for (String datum : data) {
                String [] splitStr = datum.split("\t");
                BigDecimal bigDecimal = BigDecimal.ZERO;
                if(!flag.equals(CommonEnum.QUERY_C.getFlag())){
                    bigDecimal = CommonUtil.isBigDecimal(splitStr[splitStr.length-2])?
                            new BigDecimal(splitStr[splitStr.length-2]):BigDecimal.ZERO;
                }
                total = total.add(new BigDecimal(splitStr[splitStr.length-1])).add(bigDecimal);
            }
        }
        return total;
    }
    /**
     * ????????????????????????
     * @param resultInfo
     * @param olapSvr
     * @return  int
     * @throws Exception
     */
    private int loadActualData(JSONArray resultInfo,IEssOlapServer olapSvr)throws Exception{
        IEssCube cube = olapSvr.getApplication(essConnectUtil.getAppName()).getCube(essConnectUtil.getAppCube());
        StringBuffer buffer = new StringBuffer();
        int noMatchCount = 0 ;
        //???????????????
        int count = 0 ;
        for (int i = 0; i < resultInfo.size(); i++)  {
            StringBuffer sb = new StringBuffer();
            JSONObject value = (JSONObject) resultInfo.get(i);
            //??????????????????????????????????????????
            boolean isExpenses = SUBJECT.equals(value.get("PROJECTCODE").toString());
            //?????????????????????????????????
            String code = commonService.getEntityValue(value.get("ENTITYCODE").toString(),value.get("DEPTCODE").toString(),isExpenses,false);
            if(StringUtils.isBlank(code)){
                noMatchCount++;
                continue;
            }
            //????????????????????????????????????900901???????????????????????????KM0615???????????????
            if(isExpenses){
                String accountCode = value.get("ACCOUNTCODE").toString();
                sb.append("\""+accountCode+"\" ");
            }else{
                sb.append("\"KM0615\" ");
            }
            sb.append("\""+value.get("PERIODCODE")+"???\" ").append("\""+ essConnectUtil.getYear(value.get("YEARCODE").toString())+"\" ");
            sb.append("\""+ScennarioEnum.S14.getCode()+"\" ").append("\""+VersionsEnum.OA_ver.getCode()+"\" ");
            //????????????
            sb.append("\""+code+"\" ");
            //????????????????????????????????????????????????????????????
            sb.append("\"CP99\" ").append("\"WL99\" ").append("\"FZ99\" ").append("\"ZH99\" ");
            sb.append("\""+value.get("CURRENYCODE")+"\" ").append("\"BY99\" ").append("\"QY99\" ");
            //????????????????????????0  ????????????????????????XM99
            if(isExpenses){
                sb.append("\"XM99\" ");
            }else{
                sb.append("\""+value.get("PROJECTCODE")+"\" ");
            }
            BigDecimal zys = new BigDecimal(value.getString("P_ACT_U"));
            BigDecimal sjs = new BigDecimal(value.getString("P_ACT"));
            //???????????????????????????
            String script1 = sb.toString().toString()+ sjs;
            buffer.append(script1).append("\n");
            //???????????????????????????
            String script2 = sb.toString().toString().replace(ScennarioEnum.S14.getCode(),ScennarioEnum.S15.getCode())+ zys;
            buffer.append(script2).append("\n");
            count++;
            //??????????????????????????? ????????????????????????
            if(count > 100 || count == 100){
                logger.info("??????OA??????????????????????????????????????????????????????????????????????????????" + buffer.toString());
                iEssBaseData.loadData(cube,buffer.toString());
                buffer.setLength(0);
                count = 0;
            }
        }
        if(buffer.length()!=0){
            logger.info("??????OA??????????????????????????????????????????" + buffer.toString());
            iEssBaseData.loadData(cube,buffer.toString());
        }
        return noMatchCount;
    }
}
