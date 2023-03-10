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
import org.springframework.beans.BeanUtils;
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
import yuanian.middleconsole.hyperion.model.vo.LineVO;
import yuanian.middleconsole.hyperion.service.HlySyncBudgetService;
import yuanian.middleconsole.hyperion.service.IEssBaseData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/11/15
 * @menu: TODO
 */
@Service
public class HlySyncBudgetServiceImpl implements HlySyncBudgetService {

    private static final Logger logger = LoggerFactory.getLogger(HlySyncBudgetServiceImpl.class);

    @Autowired
    private IEssBaseData iEssBaseData;
    @Autowired
    private DimObjecctDAO dimObjecctDAO;
    @Autowired
    private SyncAdjustBudgetDAO syncAdjustBudgetDAO;
    @Autowired
    private CommonService commonService;
    @Autowired
    private EssConnectUtil essConnectUtil;

    @Value("${HLY_BUDGET_URL}")
    private String HLY_BUDGET_URL;

    @Value("${HLY_ACTUAL_URL}")
    private String HLY_ACTUAL_URL;

    @Value("${HLY_USERNAME}")
    private String HLY_USERNAME;

    @Value("${HLY_PASSWORD}")
    private String HLY_PASSWORD;
    /**
     * ?????????????????????
     */
    private static final String ERROR_CODE = "0000";
    /**
     * ????????????
     */
    private static final String SUBJECT = "0";
    /**
     * shell????????????
     */
    private static final String SHELL_PATH = "/yn/CalcProjSumJA.sh";
    /**
     * ?????????????????????????????????
     * @param requestVO
     * @return
     */
    @Override
    public Map<String,Object> syncBudget(Map<String,Object> requestVO) {
        logger.info(LogConstants.LOG_ENTER_METHOD,"get budget start:" + JSON.toJSONString(requestVO));
        //????????????
        Map<String,Object> resultMap  = new HashMap<>(2);
        //????????????
        Map<String,String> esbInfoVO = (Map) requestVO.get("ESBINFO");
        Map<String,String> requestMap =  (Map) requestVO.get("REQUESTINFO");
        //1 ????????????
        if(null == esbInfoVO || null == requestMap || null == requestMap.get("PERIODCODE") ||
                null == requestMap.get("YEARCODE") || null == requestMap.get("ENTITYCODE")){
            CommonUtil.packageObj(esbInfoVO,CommonEnum.WARN.getFlag(),"?????????????????????", null);
            resultMap.put("ESBINFO", esbInfoVO);
            return resultMap;
        }
        String [] entityCodes = requestMap.get("ENTITYCODE").split(";");
        //2 ???????????????
        Map map = essConnectUtil.connectEssbase();
        IEssbase ess = (IEssbase) map.get("ess");
        IEssOlapServer olapSvr = (IEssOlapServer) map.get("olapSvr");
        List<Map<String,String>> resultInfo = new ArrayList<>();
        try {
            IEssCube cube = olapSvr.getApplication(essConnectUtil.getAppName()).getCube(essConnectUtil.getAppCube());
            AdjustBudgetVO budgetVO = new AdjustBudgetVO();
            budgetVO.setPeriodCode(requestMap.get("PERIODCODE"));
            budgetVO.setYearCode(requestMap.get("YEARCODE"));
            //?????????????????????????????????
            String costBudget = this.concatQueryScript(budgetVO,entityCodes,true);
            this.getBudgetList(resultInfo,budgetVO,true,costBudget,cube);
            //???????????????????????????????????????
            String capitalBudget = this.concatQueryScript(budgetVO,entityCodes,false);
            this.getBudgetList(resultInfo,budgetVO,false,capitalBudget,cube);
            if(CollectionUtils.isEmpty(resultInfo)){
                CommonUtil.packageObj(esbInfoVO,CommonEnum.MISS.getFlag(),"?????????????????????", null);
            }else {
                CommonUtil.packageObj(esbInfoVO,CommonEnum.SUSS.getFlag(),"?????????????????????", null);
            }
            resultMap.put("ESBINFO", esbInfoVO);
            resultMap.put("RESULTINFO", resultInfo);
        }catch (Exception e){
            CommonUtil.packageObj(esbInfoVO,CommonEnum.FAIL.getFlag(),"?????????????????????", e.getMessage());
            resultMap.put("ESBINFO", esbInfoVO);
            logger.error("get budget from essbase Error: " + e);
        } finally {
            essConnectUtil.closeEssConnect(ess,olapSvr);
            String logFlag = esbInfoVO.get("RETURNSTATUS").equals(CommonEnum.FAIL.getFlag())? CommonEnum.FAIL.getFlag(): CommonEnum.SUSS.getFlag();
            commonService.saveInterLogs(esbInfoVO.get("INSTID"),"??????????????????","syncBudget","???????????????",
                    "????????????", JSON.toJSONString(requestVO),JSON.toJSONString(resultMap),logFlag,esbInfoVO.get("REQUESTTIME"));
        }
        logger.info(LogConstants.LOG_LEAVE_METHOD,"get budget end:" + JSON.toJSONString(requestVO));
        return resultMap;
    }

    @Override
    public Map<String, String> syncActualData() {
        logger.info(LogConstants.LOG_ENTER_METHOD,"====get actual data start===");
        Map<String,String> esbInfoVO = new HashMap<>(2);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LogConstants.LACALDATE_TIME_FORMAT_PATTERN);
        String date = simpleDateFormat.format(new Date());
        //1 ???????????????
        Map map = essConnectUtil.connectEssbase();
        IEssbase ess = (IEssbase) map.get("ess");
        IEssOlapServer olapSvr = (IEssOlapServer) map.get("olapSvr");
        try{
            IEssCube cube = olapSvr.getApplication(essConnectUtil.getAppName()).getCube(essConnectUtil.getAppCube());
            //2 ????????????
            List<Map<String,String>> booksAndOrgs  = dimObjecctDAO.getBooksAndOrgs();
            StringBuffer sb = new StringBuffer();
            //????????????
            List<String> childrenAccount = dimObjecctDAO.getChildrenAccount();
            //????????????
            List<String> childrenProject = dimObjecctDAO.getChildrenProject();
            String logFlag =  CommonEnum.SUSS.getFlag();
            for (Map<String, String> booksAndOrg : booksAndOrgs) {
                String book = booksAndOrg.get("ORGANIZATIONCODE").toString();
                sb.append("?????????"+book+"???:");
                //3 ????????????????????????????????????
                Map<String,Object> requestCostMap = this.packageRequestMap(booksAndOrg,true);
                logger.info("??????????????????????????????????????????????????? ???" + JSONObject.toJSONString(requestCostMap));
                //4 ????????????
                String costResult = HttpClientUtils.doPostJson(HLY_ACTUAL_URL,JSONObject.toJSONString(requestCostMap),HLY_USERNAME,HLY_PASSWORD);
                logger.info("????????????????????????????????????????????????????????? ???" + costResult);
                //5 ????????????????????????????????????
                Map<String,Object> costMap = this.loadActualData(costResult,true,cube,childrenAccount);
                sb.append("??????????????????"+costMap.get("TotalCount")+"????????????????????????????????????"+costMap.get("UnMatchEntity")+"?????????????????????????????????????????????"+costMap.get("UnMatchAccount")+"?????????");
                commonService.saveInterLogs(costMap.get("InstId").toString(),"????????????????????????","syncActualData","????????????",
                        "???????????????", JSON.toJSONString(requestCostMap),JSON.toJSONString(costResult),logFlag,date);

                //6 ??????????????????????????????????????????
                Map<String,Object> requestCapitalMap = this.packageRequestMap(booksAndOrg,false);
                logger.info("????????????????????????????????????????????????????????? ???" + JSONObject.toJSONString(requestCapitalMap));
                //7 ????????????
                String capitalresult = HttpClientUtils.doPostJson(HLY_ACTUAL_URL,JSONObject.toJSONString(requestCapitalMap),HLY_USERNAME,HLY_PASSWORD);
                logger.info("??????????????????????????????????????????????????????????????? ???" + capitalresult);
                //8 ??????????????????????????????????????????
                Map<String,Object> captailMap = this.loadActualData(capitalresult,false,cube,childrenProject);
                sb.append("????????????????????????"+captailMap.get("TotalCount")+"????????????????????????????????????"+captailMap.get("UnMatchEntity")+"????????????????????????????????????????????????"+captailMap.get("UnMatchProject")+"?????????");
                commonService.saveInterLogs(captailMap.get("InstId").toString(),"??????????????????????????????","syncActualData","????????????",
                        "???????????????", JSON.toJSONString(requestCapitalMap),JSON.toJSONString(capitalresult),logFlag,date);
            }
            CommonUtil.packageObj(esbInfoVO,CommonEnum.SUSS.getFlag(),sb.toString(),null);
        }catch (Exception e){
            CommonUtil.packageObj(esbInfoVO,CommonEnum.FAIL.getFlag(),"??????????????????",e.getMessage());
            logger.error("get actual data to essbase Error: " + e);
        }finally {
            essConnectUtil.closeEssConnect(ess,olapSvr);
        }
        logger.info(LogConstants.LOG_LEAVE_METHOD,"get actual data end:" + JSON.toJSONString(esbInfoVO));
        return esbInfoVO;
    }

    @Override
    public Map<String, String> syncAdjustBudget(AdjustBudgetVO adjustBudgetVO){
        Map<String,String> esbInfoVO = new HashMap<>(2);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LogConstants.LACALDATE_TIME_FORMAT_PATTERN);
        String date = simpleDateFormat.format(new Date());
        //1 ???????????????????????????
        List<AdjustBudgetVO> budgetVOS = syncAdjustBudgetDAO.selectList(adjustBudgetVO);
        //2 ???????????????
        Map map = essConnectUtil.connectEssbase();
        IEssbase ess = (IEssbase) map.get("ess");
        IEssOlapServer olapSvr = (IEssOlapServer) map.get("olapSvr");
        IEssCube cube = null;
        try{
            cube = olapSvr.getApplication(essConnectUtil.getAppName()).getCube(essConnectUtil.getAppCube());
        }catch (Exception e){
            CommonUtil.packageObj(esbInfoVO,CommonEnum.FAIL.getFlag(),"????????????????????????",e.getMessage());
            return esbInfoVO;
        }
        int count = 1 ;
        for (AdjustBudgetVO budgetVO : budgetVOS) {
            AdjustBudgetVO returnVO = new AdjustBudgetVO();
            returnVO.setId(budgetVO.getId());
            returnVO.setUpdateTime(LocalDateTime.now());
            //3 ?????????????????????????????????
            if(budgetVO.getSyncStatus().equals(CommonEnum.D.getFlag())){
                returnVO.setSyncStatus(CommonEnum.B.getFlag());
            }else{
                try {
                    this.updateHyperionAdjust(budgetVO,returnVO,cube);
                    handleShell(SHELL_PATH);
                }catch (Exception e){
                    returnVO.setSyncStatus(CommonEnum.C.getFlag());
                    returnVO.setSyncMsg("??????????????????OA????????????????????????"+ e.getMessage());
                }finally {
                    if(count == budgetVOS.size()){
                        essConnectUtil.closeEssConnect(ess,olapSvr);
                    }
                }
            }
            //4 ?????????????????????????????????????????????
            if(returnVO.getSyncStatus().equals(CommonEnum.B.getFlag())){
                returnVO.setPushCount(budgetVO.getPushCount()+1);
                this.pushAdjustToHly(budgetVO,esbInfoVO,returnVO,date);
            }
            //5 ???????????????????????????????????????
            syncAdjustBudgetDAO.updateByPrimaryKeySelective(returnVO);
            count++;
        }
        CommonUtil.packageObj(esbInfoVO,CommonEnum.SUSS.getFlag(),"????????????????????????????????????",null);
        return esbInfoVO;
    }

    /**
     * ??????????????????shell??????
     * @param command
     */
    private void handleShell(String command){
        InputStreamReader stdISR = null;
        InputStreamReader errISR = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            int exitValue = process.waitFor();
            String line = null;
            stdISR = new InputStreamReader(process.getInputStream());
            BufferedReader stdBR = new BufferedReader(stdISR);
            while ((line = stdBR.readLine()) != null) {
                logger.info("??????shell?????? STD line:" + line);
            }
            errISR = new InputStreamReader(process.getErrorStream());
            BufferedReader errBR = new BufferedReader(errISR);
            while ((line = errBR.readLine()) != null) {
                logger.info("??????shell?????? ERR line:" + line);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stdISR != null) {
                    stdISR.close();
                }
                if (errISR != null) {
                    errISR.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                logger.info("??????shell?????????" + command + "???IO??????");
            }
        }
    }

    /**
     * ??????????????????
     * @param resultInfo
     * @param budgetVO
     * @param flag
     * @param querySql
     * @param cube
     * @return
     * @throws Exception
     */
    private void getBudgetList(List<Map<String,String>> resultInfo,AdjustBudgetVO budgetVO,
                               boolean flag,String querySql,IEssCube cube) throws Exception{
        String massge = flag?"???????????????":"???????????????";
        logger.info("??????"+massge+"?????????????????????" + querySql);
        DimData dataBudget = iEssBaseData.executeReport(cube, IEssBaseData.ReportType.scriptString,querySql);
        logger.info("??????"+massge+"?????????????????????" + JSON.toJSONString(dataBudget.getData()));
        if(StringUtils.isNotBlank(dataBudget.getData())){
            String []data = dataBudget.getData().split("\n");
            for (String datum : data) {
                String [] strings = datum.split("\t");
                Map<String,String> responseMap = new HashMap<>(4);
                if(flag){
                    responseMap.put("ACCOUNTCODE",strings[0]);
                    responseMap.put("PROJECTCODE","0");
                }else{
                    responseMap.put("ACCOUNTCODE","900901");
                    responseMap.put("PROJECTCODE",strings[strings.length-2]);
                }
                List<Map<String,String>> mapList = dimObjecctDAO.getEntityByCode(strings[5]);
                if(CollectionUtils.isEmpty(mapList)){
                    responseMap.put("ENTITYCODE","");
                    responseMap.put("DEPTCODE","");
                }else{
                    if(flag){
                        responseMap.put("ENTITYCODE",mapList.get(0).get("EBSCOMPANYCODE"));
                        responseMap.put("DEPTCODE",mapList.get(0).get("EBSDEPTCODE"));
                    }else{
                        responseMap.put("ENTITYCODE",strings[5].substring(1,strings[5].length()));
                        responseMap.put("DEPTCODE","0");
                    }
                }
                responseMap.put("YEARCODE",budgetVO.getYearCode());
                responseMap.put("PERIODCODE",budgetVO.getPeriodCode());
                responseMap.put("CURRENYCODE",strings[strings.length-5]);
                responseMap.put("P_BUG",strings[strings.length-1]);
                resultInfo.add(responseMap);
            }
        }
    }
    /**
     * ??????????????????
     * @param budgetVO
     * @param scenario
     * @return
     */
    private String concatQuerySql(AdjustBudgetVO budgetVO,String scenario){
        StringBuffer sb = new StringBuffer();
        sb.append("//ESS_LOCALE SimplifiedChinese_China.MS936@Binary\n");
        sb.append("<Sym\n").append("{SUPALL}{TABDELIMIT} {NAMESON}{ROWREPEAT}{NOINDENTGEN}{SUPMISSINGROWS}\n");
        sb.append("{MISSINGTEXT \"0\"}\n");
        sb.append("<COLUMN(\"Periods\")\n");
        sb.append("<Row(").append("Account").append(",Periods").append(",Years");
        sb.append(",Scenario").append(",Versions").append(",Entity").append(",Product");
        sb.append(",Material").append(",Auxiliary").append(",Synthesis").append(",Currency");
        sb.append(",Spare").append(",Area").append(",Project)").append("{ ROWREPEAT }\n").append("{decimal 2}\n");
        //???????????????
        if(SUBJECT.equals(budgetVO.getProjectCode())){
            String accountCode = budgetVO.getAccountCode();
            sb.append("\""+accountCode+"\"\n");
            sb.append("\"Jan\" ").append("\"Apr\" ").append("\"Jul\" ").append("\"Oct\"\n");
        }else{
            //????????? ??? ???????????????
            sb.append("\"KM0630\" ").append("\"KM0615\"\n");
            String period = budgetVO.getPeriodCode();
            if(StringUtils.isNotBlank(period) && period.startsWith("0")){
                period = period.replace("0","");
            }
            sb.append("\"QJ99\" ").append("\""+period+"\"\n");
        }
        //???????????????
        sb.append("\""+essConnectUtil.getYear(budgetVO.getYearCode())+"\"\n").append("\""+scenario+"\"\n");
        //??????
        sb.append("\""+ VersionsEnum.OA_ver.getCode()+"\"\n");
        sb.append("\""+ budgetVO.getEntityCode() +"\"\n");
        sb.append("\"CP99\"\n").append("\"WL99\"\n").append("\"FZ99\"\n").append("\"ZH99\"\n");
        sb.append("\""+budgetVO.getCurrenyCode()+"\"\n").append("\"BY99\"\n").append("\"QY99\"\n");
        //????????????????????????0  ????????????????????????XM99
        if(SUBJECT.equals(budgetVO.getProjectCode())){
            sb.append("\"XM99\"\n!");
        }else{
            sb.append("\""+budgetVO.getProjectCode()+"\"\n!");
        }
        return sb.toString();
    }
    /**
     * ??????????????????
     * @param budgetVO
     * @param queryData
     * @param scenario
     * @return
     */
    private String concatUpdateSql(AdjustBudgetVO budgetVO,String queryData,String scenario){
        StringBuffer sb = new StringBuffer();
        //??????????????? ??? ???????????????
        if(SUBJECT.equals(budgetVO.getProjectCode())){
            String accountCode = budgetVO.getAccountCode();
            sb.append("\""+accountCode+"\" ");
            sb.append("\"Jan\" ");
        }else {
            sb.append("\"KM0630\" ");
            sb.append("\"QJ99\" ");
        }
        //???????????????
        sb.append("\""+essConnectUtil.getYear(budgetVO.getYearCode())+"\" ").append("\""+scenario+"\" ");
        //??????
        sb.append("\""+VersionsEnum.OA_ver.getCode()+"\" ");
        sb.append("\""+ budgetVO.getEntityCode() +"\" ");
        //????????????????????????????????????????????????????????????
        sb.append("\"CP99\" ").append("\"WL99\" ").append("\"FZ99\" ").append("\"ZH99\" ");
        sb.append("\""+budgetVO.getCurrenyCode()+"\" ").append("\"BY99\" ").append("\"QY99\" ");
        //????????????????????????0  ????????????????????????XM99
        if(SUBJECT.equals(budgetVO.getProjectCode())){
            sb.append("\"XM99\" ");
        }else{
            sb.append("\""+budgetVO.getProjectCode()+"\" ");
        }
        //????????????
        boolean flag = StringUtils.isNotBlank(queryData);
        if(SUBJECT.equals(budgetVO.getProjectCode())){
            //1???
            BigDecimal q1 = budgetVO.getQ1Amount();
            //2???
            BigDecimal q2 = budgetVO.getQ2Amount();
            //3???
            BigDecimal q3 = budgetVO.getQ3Amount();
            //4???
            BigDecimal q4 = budgetVO.getQ4Amount();
            if(flag){
                String []data = queryData.split("\n");
                String [] strings = data[0].split("\t");
                q1 = q1.add(new BigDecimal(strings[strings.length-4]));
                q2 = q2.add(new BigDecimal(strings[strings.length-3]));
                q3 = q3.add(new BigDecimal(strings[strings.length-2]));
                q4 = q4.add(new BigDecimal(strings[strings.length-1]));
            }
            //????????????
            String script1 = sb.toString().toString().replace("Jan","Apr")+ q2.toString();
            String script2 = sb.toString().toString().replace("Jan","Jul")+ q3.toString();
            String script3 = sb.toString().toString().replace("Jan","Oct")+ q4.toString();
            //?????????????????????????????????
            sb.append(q1.toString()).append("\n").append(script1).append("\n").append(script2).append("\n").append(script3);
        }else{
            //?????????
            BigDecimal tze = budgetVO.getInvestAdjustAmount();
            //?????????
            BigDecimal yse = budgetVO.getAdjustAmount();
            if(flag){
                String []data = queryData.split("\n");
                for (String datum : data) {
                    String [] strings = datum.split("\t");
                    if("KM0630".equals(strings[1])){
                        tze = tze.add(new BigDecimal(strings[strings.length-2]));
                    }
                    if("KM0615".equals(strings[1])){
                        yse = yse.add(new BigDecimal(strings[strings.length-1]));
                    }
                }
            }
            String period = budgetVO.getPeriodCode();
            if(StringUtils.isNotBlank(period) && period.startsWith("0")){
                period = period.replace("0","");
            }
            //???????????????
            String script = sb.toString().toString().replace("KM0630","KM0615")
                    .replace("QJ99",period)+ yse.toString();
            //???????????????????????????
            sb.append(tze.toString()).append("\n").append(script) ;
        }
        return sb.toString();
    }

    /**
     * ????????????????????????
     * @param result
     * @param flag
     * @param cube
     * @param list
     * @throws Exception
     */
    private Map<String,Object> loadActualData(String result,boolean flag,IEssCube cube,List<String> list)throws Exception{
        Map<String,Object> resultMap = new HashMap<>(2);
        resultMap.put("TotalCount",0); resultMap.put("UnMatchEntity",0);resultMap.put("UnMatchAccount",0);resultMap.put("UnMatchProject",0);
        JSONObject resultJson = JSON.parseObject(result);
        JSONObject esbInfo = JSON.parseObject(resultJson.getString("esbInfo"));
        resultMap.put("InstId",esbInfo.getString("instId"));
        JSONObject resultInfo = JSON.parseObject(resultJson.getString("resultInfo"));
        JSONObject data = JSON.parseObject(resultInfo.getString("data"));
        if(ERROR_CODE.equals(resultInfo.getString("errorCode")) && null != data){
            JSONArray resultList = JSONArray.parseArray(data.getString("queryResultList"));
            resultMap.put("TotalCount",resultList.size());
            StringBuffer buffer = new StringBuffer();
            //??????????????????
            int unMatchEntity = 0 ;
            //??????????????????
            int unMatchAccount = 0 ;
            //??????????????????
            int unMatchProject = 0 ;
            //???????????????
            int count = 0 ;
            for (int i = 0; i < resultList.size(); i++)  {
                StringBuffer sb = new StringBuffer();
                //???????????????????????????
                JSONObject value = (JSONObject) resultList.get(i);
                //?????????????????????????????????
                String code = commonService.getEntityValue(value.getString("companyCode"),value.getString("unitCode"),flag,false);
                if(StringUtils.isBlank(code)){
                    unMatchEntity++;
                    continue;
                }
                //????????????????????????????????????900901???????????????????????????KM0615???????????????
                if(flag){
                    if(!list.contains(value.getString("itemCode"))){
                        unMatchAccount++;
                        continue;
                    }
                    sb.append("\""+value.getString("itemCode")+"\" ");
                }else{
                    sb.append("\"KM0615\" ");
                }
                String period = value.getString("periodName");
                if(StringUtils.isNotBlank(period) && period.startsWith("0")){
                    period = period.replace("0","");
                }
                sb.append("\""+period+"\" ");
                sb.append("\""+ essConnectUtil.getYear(value.getString("periodYear"))+"\" ");
                sb.append("\""+ScennarioEnum.S14.getCode()+"\" ").append("\""+VersionsEnum.HLY_ver.getCode()+"\" ");
                //????????????
                sb.append("\""+code+"\" ");
                //????????????????????????????????????????????????????????????
                sb.append("\"CP99\" ").append("\"WL99\" ").append("\"FZ99\" ").append("\"ZH99\" ");
                sb.append("\""+value.getString("currency")+"\" ").append("\"BY99\" ").append("\"QY99\" ");
                //????????????????????????0  ????????????????????????XM99
                if(flag){
                    sb.append("\"XM99\" ");
                }else{
                    if(!list.contains(value.getString("dimension1Code"))){
                        unMatchProject++;
                        continue;
                    }
                    sb.append("\""+value.getString("dimension1Code")+"\" ");
                }
                BigDecimal zys = new BigDecimal(value.getString("expReserveFunAmount"));
                BigDecimal sjs = new BigDecimal(value.getString("expUsedFunAmount"));
                //???????????????????????????
                String script1 = sb.toString().toString()+ sjs;
                buffer.append(script1).append("\n");
                //???????????????????????????
                String script2 = sb.toString().toString().replace(ScennarioEnum.S14.getCode(),ScennarioEnum.S15.getCode())+ zys;
                buffer.append(script2).append("\n");
                count ++;
                //??????????????????????????? ????????????????????????
                if(count > 100 || count == 100){
                    logger.info("?????????????????????????????????????????????????????????????????????????????????????????????" + buffer.toString());
                    iEssBaseData.loadData(cube,buffer.toString());
                    buffer.setLength(0);
                    count = 0;
                }
            }
            if(buffer.length()!=0){
                logger.info("?????????????????????????????????????????????????????????????????????" + buffer.toString());
                iEssBaseData.loadData(cube,buffer.toString());
            }
            resultMap.put("UnMatchEntity",unMatchEntity); resultMap.put("UnMatchAccount",unMatchAccount); resultMap.put("UnMatchProject",unMatchProject);
        }
        return resultMap;
    }
    /**
     * ?????????????????????????????????
     * @param budgetVO
     * @param returnVO
     * @param cube
     * @return
     */
    private void updateHyperionAdjust(AdjustBudgetVO budgetVO,AdjustBudgetVO returnVO,IEssCube cube) throws Exception{
        //3 ????????????????????????????????????????????????
        boolean isExpenses = SUBJECT.equals(budgetVO.getProjectCode());
        String code = commonService.getEntityValue(budgetVO.getEntityCode(),budgetVO.getDeptCode(),isExpenses,false);
        if(StringUtils.isBlank(code)){
            returnVO.setSyncStatus(CommonEnum.C.getFlag());
            returnVO.setSyncMsg("??????????????????????????????????????????,?????????????????????");
        }else{
            budgetVO.setEntityCode(code);
            //4 ????????????????????????
            String querySql = this.concatQuerySql(budgetVO, ScennarioEnum.S03.getCode());
            logger.info("?????????????????????????????????????????????" + querySql);
            DimData dimData = iEssBaseData.executeReport(cube, IEssBaseData.ReportType.scriptString, querySql);
            logger.info("?????????????????????????????????????????????" + JSON.toJSONString(dimData.getData()));
            //5 ??????????????????????????????
            String updateSql = this.concatUpdateSql(budgetVO, dimData.getData(), ScennarioEnum.S03.getCode());
            //6 ???????????????????????????
            logger.info("??????????????????OA????????????????????????" + updateSql);
            iEssBaseData.loadData(cube,updateSql);
            returnVO.setSyncStatus(CommonEnum.B.getFlag());
        }
    }
    /**
     * ????????????????????????????????????
     * @param budgetVO
     * @param esbInfoVO
     * @param returnVO
     * @return
     */
    private AdjustBudgetVO pushAdjustToHly(AdjustBudgetVO budgetVO,Map<String,String> esbInfoVO,AdjustBudgetVO returnVO,String date){
        CommonUtil.packageObj(esbInfoVO,CommonEnum.REQUEST_LOWER.getFlag(),"????????????????????????????????????",null);
        Map<String,Object> requestMap = new HashMap<>(3);
        requestMap.put("esbInfo",esbInfoVO);
        Map<String,Object> requestInfo = new HashMap<>(3);
        requestInfo.put("employeeId","admin");
        requestInfo.put("organizationCode",budgetVO.getEntityCode());
        Map<String,String> headMap = new HashMap<>(1);
        headMap.put("description",budgetVO.getBatchNo());
        requestInfo.put("head",headMap);

        List<LineVO> lineList = new ArrayList<>();
        String year = budgetVO.getYearCode().replace("???","");
        LineVO lineVO = new LineVO();
        lineVO.setCurrency(budgetVO.getCurrenyCode());
        lineVO.setItemCode(budgetVO.getAccountCode());
        lineVO.setPeriodYear(year);
        lineVO.setCompanyCode(budgetVO.getEntityCode());
        lineVO.setUnitCode(budgetVO.getDeptCode());
        lineVO.setDimension1ValueCode(budgetVO.getProjectCode());
        if(SUBJECT.equals(budgetVO.getProjectCode())){
            LineVO lineVO1 = new LineVO();
            BeanUtils.copyProperties(lineVO,lineVO1);
            lineVO1.setPeriodName(year+"-01");
            lineVO1.setAmount(budgetVO.getQ1Amount());
            lineVO1.setFunctionalAmount(budgetVO.getQ1Amount());
            lineList.add(lineVO1);
            LineVO lineVO2 = new LineVO();
            BeanUtils.copyProperties(lineVO,lineVO2);
            lineVO2.setPeriodName(year+"-04");
            lineVO2.setAmount(budgetVO.getQ2Amount());
            lineVO2.setFunctionalAmount(budgetVO.getQ2Amount());
            lineList.add(lineVO2);
            LineVO lineVO3 = new LineVO();
            BeanUtils.copyProperties(lineVO,lineVO3);
            lineVO3.setPeriodName(year+"-07");
            lineVO3.setAmount(budgetVO.getQ3Amount());
            lineVO3.setFunctionalAmount(budgetVO.getQ3Amount());
            lineList.add(lineVO3);
            LineVO lineVO4 = new LineVO();
            BeanUtils.copyProperties(lineVO,lineVO4);
            lineVO4.setPeriodName(year+"-10");
            lineVO4.setAmount(budgetVO.getQ4Amount());
            lineVO4.setFunctionalAmount(budgetVO.getQ4Amount());
            lineList.add(lineVO4);
        }else{
            String month = budgetVO.getPeriodCode().replace("???","");
            LineVO lineVO1 = new LineVO();
            BeanUtils.copyProperties(lineVO,lineVO1);
            lineVO1.setPeriodName(year+"-"+month);
            lineVO1.setAmount(budgetVO.getAdjustAmount());
            lineVO1.setFunctionalAmount(budgetVO.getAdjustAmount());
            lineList.add(lineVO1);
        }
        requestInfo.put("lines",lineList);
        requestMap.put("requestInfo",requestInfo);
        String result = "";
        try{
            logger.info("?????????????????????????????????????????? ???" + JSONObject.toJSONString(requestMap));
            //3 ????????????
            result = HttpClientUtils.doPostJson(HLY_BUDGET_URL, JSONObject.toJSONString(requestMap),HLY_USERNAME,HLY_PASSWORD);
            logger.info("???????????????????????????????????????????????? ???" + result);
            JSONObject resultJson = JSON.parseObject(result);
            JSONObject resultInfo = JSON.parseObject(resultJson.getString("resultInfo"));
            String pushResultCode = resultInfo.getString("errorCode");
            if(pushResultCode.equals(ERROR_CODE)){
                returnVO.setSyncStatus(CommonEnum.E.getFlag());
                returnVO.setSyncMsg(CommonEnum.E.getTitle());
            }else{
                returnVO.setSyncStatus(CommonEnum.D.getFlag());
                String pushResultMsg = resultInfo.getString("message");
                returnVO.setSyncMsg(pushResultMsg);
            }
            return returnVO;
        }catch (Exception e){
            returnVO.setSyncStatus(CommonEnum.D.getFlag());
            returnVO.setSyncMsg("??????????????????????????????????????????"+ e.getMessage());
            logger.info("???????????????????????????????????????" + e.getMessage());
            return returnVO;
        }finally {
            String logFlag = returnVO.getSyncStatus().equals(CommonEnum.D.getFlag())? CommonEnum.FAIL.getFlag(): CommonEnum.SUSS.getFlag();
            commonService.saveInterLogs(esbInfoVO.get("INSTID"),"?????????????????????","pushAdjustToHly","????????????",
                    "???????????????", JSON.toJSONString(requestMap),JSON.toJSONString(result),logFlag,date);
        }
    }
    /**
     * ??????????????????????????????????????????
     * @param booksAndOrg
     * @param flag
     * @return
     */
    private Map<String,Object> packageRequestMap(Map<String, String> booksAndOrg,boolean flag){
        Map<String,Object> requestMap = new HashMap<>(2);
        //????????????
        Map<String,String> esbInfo = new HashMap<>(2);
        CommonUtil.packageObj(esbInfo,CommonEnum.REQUEST_LOWER.getFlag(),"????????????????????????",null);
        Map<String,Object> requestinfo = new HashMap<>(2);
        //??????????????????
        Calendar calendar = Calendar.getInstance();
        // ???????????????
        int year = calendar.get(Calendar.YEAR);
        // ???????????????
        int month = calendar.get(Calendar.MONTH) + 1;
        requestinfo.put("page",0);
        requestinfo.put("size",20000);
        requestinfo.put("organizationCode",booksAndOrg.get("ORGANIZATIONCODE"));
        requestinfo.put("scenarioCode",booksAndOrg.get("SCENARIOCODE"));
        requestinfo.put("versionCode",booksAndOrg.get("VERSIONCODE"));
        requestinfo.put("structureCode",booksAndOrg.get("STRUCTURECODE"));
        requestinfo.put("periodSummaryFlag",true);
        requestinfo.put("periodLowerLimit",String.valueOf(year)+"-01");
        requestinfo.put("periodUpperLimit",String.valueOf(year)+"-"+String.valueOf(month >= 10 ? month : "0" + month));
        requestinfo.put("yearLimit",String.valueOf(year));
        if(flag){
            requestinfo.put("isFilter",false);
        }else{
            requestinfo.put("isFilter",true);
        }
        List<Map<String,Object>> lineList = new ArrayList<>();
        Map<String,Object> companyMap = new HashMap<>(2);
        companyMap.put("parameterType","BGT_RULE_PARAMETER_ORG");
        companyMap.put("parameterCode","COMPANY");
        companyMap.put("isAll",true);
        lineList.add(companyMap);

        Map<String,Object> projectMap = new HashMap<>(2);
        projectMap.put("parameterType","BGT_RULE_PARAMETER_DIM");
        projectMap.put("parameterCode","PROJECT");
        projectMap.put("isAll",true);
        lineList.add(projectMap);
        if(flag){
            Map<String,Object> unitMap = new HashMap<>(2);
            unitMap.put("parameterType","BGT_RULE_PARAMETER_ORG");
            unitMap.put("parameterCode","UNIT");
            unitMap.put("isAll",true);
            lineList.add(unitMap);

            Map<String,Object> budgetMap = new HashMap<>(2);
            budgetMap.put("parameterType","BGT_RULE_PARAMETER_BUDGET");
            budgetMap.put("parameterCode","BUDGET_ITEM");
            budgetMap.put("isAll",true);
            lineList.add(budgetMap);
        }
        requestinfo.put("queryLineList",lineList);
        //???????????????
        requestMap.put("esbInfo",esbInfo);
        requestMap.put("requestInfo",requestinfo);
        return requestMap;
    }


    /**
     * ????????????????????????
     * @param budgetVO
     * @param entityCodes
     * @param type
     * @return
     */
    private String concatQueryScript(AdjustBudgetVO budgetVO,String[] entityCodes,boolean type){
        StringBuffer sb = new StringBuffer();
        sb.append("//ESS_LOCALE SimplifiedChinese_China.MS936@Binary\n");
        sb.append("<Sym\n").append("{SUPALL}{TABDELIMIT} {NAMESON}{ROWREPEAT}{NOINDENTGEN}{SUPMISSINGROWS}\n");
        sb.append("{MISSINGTEXT \"0\"}\n");
        sb.append("<Row(").append("Account").append(",Periods").append(",Years");
        sb.append(",Scenario").append(",Versions").append(",Entity").append(",Product");
        sb.append(",Material").append(",Auxiliary").append(",Synthesis").append(",Currency");
        sb.append(",Spare").append(",Area").append(",Project)").append("{ ROWREPEAT }\n").append("{decimal 2}\n");
        //?????????????????????
        if(type){
            sb.append("<LINK(<DESCENDANTS(\"5101\",\"Lev0,Account\") or <DESCENDANTS(\"6601\",\"Lev0,Account\") " +
                    "or <DESCENDANTS(\"6602\",\"Lev0,Account\") or <DESCENDANTS(\"660221\",\"Lev0,Account\"))\n");
            String period = budgetVO.getPeriodCode();
            if(StringUtils.isNotBlank(period) && period.startsWith("0")){
                period = period.replace("0","");
            }
            sb.append("\""+period+"\"\n");
        }else {
            sb.append("\"KM0615\"\n");
            sb.append("\"YearTotal\"\n");
        }
        //????????????????????????
        sb.append("\""+budgetVO.getYearCode()+"\"\n").append("\""+ScennarioEnum.S02.getCode()+"\"\n");
        //???????????????
        sb.append("\""+ VersionsEnum.OA_ver.getCode()+"\"\n");
        sb.append("<LINK(");
        for (int i = 0; i < entityCodes.length; i++) {
            String code = "E"+entityCodes[i];
            if(i != entityCodes.length-1){
                sb.append("<DESCENDANTS(\""+code+"\",\"Lev0,Entity\") or ");
            }else{
                sb.append("<DESCENDANTS(\""+code+"\",\"Lev0,Entity\")");
            }
        }
        sb.append(")\n");
        //????????????????????????
        sb.append("\"CP99\"\n").append("\"WL99\"\n").append("\"FZ99\"\n").append("\"ZH99\"\n");
        //??????
        sb.append("<LINK(<DESCENDANTS(\"BZT\",\"Lev0,Currency\") AND NOT <IDESCENDANTS( \"BZ01\",\"Lev0,Currency\"))\n");
        //???????????????
        sb.append("\"BY99\"\n").append("\"QY99\"\n");
        //??????
        if(type){
            sb.append("\"XM99\"\n!");
        }else{
            List<Map<String,Object>>  mapList = dimObjecctDAO.getProjectsFromDb();
            for (int i = 0; i < mapList.size(); i++) {
                String projectCode = mapList.get(i).get("OBJECT_NAME").toString();
                if(i != mapList.size()-1){
                    sb.append("\""+projectCode+"\" ");
                }else{
                    sb.append("\""+projectCode+"\"\n!");
                }
            }
        }
        return sb.toString();
    }
}
