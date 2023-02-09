package yuanian.middleconsole.hyperion.common.util;

import com.essbase.api.base.EssException;
import com.essbase.api.datasource.IEssOlapServer;
import com.essbase.api.domain.IEssDomain;
import com.essbase.api.session.IEssbase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import yuanian.middleconsole.hyperion.common.exception.BussinessException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/17
 * @menu: TODO
 */
@Component
public class EssConnectUtil {

    private static final Logger logger = LoggerFactory.getLogger(EssConnectUtil.class);

    @Value("${ESS_USERNAME}")
    private String ESS_USERNAME;

    @Value("${ESS_PASS}")
    private String ESS_PASS;

    @Value("${ESS_PROVIDER}")
    private String ESS_PROVIDER;

    @Value("${ESS_OLAPSERVER}")
    private String ESS_OLAPSERVER;

    @Value("${APP_CUBE}")
    private String APP_CUBE;

    @Value("${APP_NAME}")
    private String APP_NAME;

    @Value("${PROCESS_YEAR}")
    private boolean PROCESS_YEAR;
    /**
     * 传了年字
     */
    private static final String TEXT = "年";


    /**
     * 获取数据cube
     * @return
     */
    public String getAppCube(){
        return APP_CUBE;
    }
    /**
     * 获取数据Name
     * @return
     */
    public String getAppName(){
        return APP_NAME;
    }

    /**
     * 链接Ess多维库
     * @return
     */
    public Map connectEssbase() {
        //创建链接集
        Map map = new HashMap(3);
        //创建链接对象
        IEssbase ess = null;
        try {
            ess = IEssbase.Home.create(IEssbase.JAPI_VERSION);
        } catch (EssException e) {
            logger.error("create connection to essbase server Error: " + e);
            throw new BussinessException("ESS-001", "create connection to essbase server Error", e);
        }
        //创建链接登录对象
        IEssDomain dom = null;
        try {
            dom = ess.signOn(ESS_USERNAME, ESS_PASS, false, null, ESS_PROVIDER);
        } catch (EssException e) {
            logger.error("login to essbase server Error: " + e);
            throw new BussinessException("ESS-002", "login to essbase server Error", e);
        }
        //创建链接登录服务对象
        IEssOlapServer olapSvr = null;
        try {
            olapSvr = dom.getOlapServer(ESS_OLAPSERVER);
        } catch (EssException e) {
            closeEssServer(ess);
            logger.error("create OlapServer Error: " + e);
            throw new BussinessException("ESS-003", "create OlapServer Error", e);
        }
        //开启链接
        try {
            olapSvr.connect();
        } catch (EssException e) {
            closeEssServer(ess);
            logger.error("connect to olap server error: " + e);
            throw new BussinessException("ESS-004", "connect to olap server error", e);
        }
        map.put("ess", ess);
        map.put("dom", dom);
        map.put("olapSvr",olapSvr);
        return map;
    }

    /**
     * 关闭链接对象
     * @param olapSvr
     */
    private void closeOlapServer(IEssOlapServer olapSvr) {
        try {
            if (olapSvr != null && olapSvr.isConnected() == true){
                olapSvr.disconnect();
            }
        } catch (EssException x) {
            logger.error("olapSvr.disconnect Error: " + x);
        }
    }

    /**
     * 关闭链接对象服务
     * @param ess
     */
    private void closeEssServer(IEssbase ess) {
        try {
            if (ess != null && ess.isSignedOn() == true){
                ess.signOff();
            }
        } catch (EssException x) {
            logger.error("ess.signOff Error: " + x);
        }
    }

    /**
     * 关闭链接
     * @param ess
     * @param olapSvr
     */
    public void closeEssConnect(IEssbase ess, IEssOlapServer olapSvr) {
        closeOlapServer(olapSvr);
        closeEssServer(ess);
    }
    /**
     * 更新多维库年度特殊处理
     * @return
     */
    public String getYear(String year){
        if(year.contains(TEXT)){
            year = year.replace(TEXT,"");
        }
        int intYear = PROCESS_YEAR?Integer.parseInt(year)+1:Integer.parseInt(year);
        return String.valueOf(intYear)+TEXT;
    }
}
