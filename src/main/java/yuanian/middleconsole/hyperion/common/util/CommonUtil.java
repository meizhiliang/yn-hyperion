package yuanian.middleconsole.hyperion.common.util;

import yuanian.middleconsole.hyperion.common.model.enums.CommonEnum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/11/29
 * @menu: TODO
 */
public class CommonUtil {

    /**
     * date formatt
     */
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 封装接口出参对象
     * @param esbInfoVO
     * @param flag
     * @param msg
     * @return
     */
    public static void packageObj(Map<String,String> esbInfoVO, String flag, String msg, String errorMsg){
        if(flag.equals(CommonEnum.SUSS.getFlag())){
            esbInfoVO.put("RETURNSTATUS",CommonEnum.SUSS.getFlag());
            esbInfoVO.put("RETURNMSG", msg);
            esbInfoVO.put("RETURNCODE","A001");
            esbInfoVO.put("RESPONSETIME",FORMATTER.format(new Date()));
        }
        if(flag.equals(CommonEnum.FAIL.getFlag())){
            esbInfoVO.put("RETURNSTATUS",CommonEnum.FAIL.getFlag());
            esbInfoVO.put("RETURNMSG",msg + "数据同步失败："+ errorMsg);
            esbInfoVO.put("RETURNCODE","E001");
            esbInfoVO.put("RESPONSETIME",FORMATTER.format(new Date()));
        }
        if(flag.equals(CommonEnum.WARN.getFlag())){
            esbInfoVO.put("RETURNSTATUS",CommonEnum.WARN.getFlag());
            esbInfoVO.put("RETURNMSG",CommonEnum.WARN.getTitle());
            esbInfoVO.put("RETURNCODE","E004");
            esbInfoVO.put("RESPONSETIME",FORMATTER.format(new Date()));
        }
        if(flag.equals(CommonEnum.MISS.getFlag())){
            esbInfoVO.put("RETURNSTATUS",CommonEnum.MISS.getFlag());
            esbInfoVO.put("RETURNMSG",CommonEnum.MISS.getTitle());
            esbInfoVO.put("RETURNCODE","E005");
            esbInfoVO.put("RESPONSETIME",FORMATTER.format(new Date()));
        }
        if(flag.equals(CommonEnum.REQUEST_LOWER.getFlag())){
            esbInfoVO.put("instId","");
            esbInfoVO.put("requestTime",FORMATTER.format(new Date()));
        }
        if(flag.equals(CommonEnum.REQUEST_UPPER.getFlag())){
            esbInfoVO.put("INSTID","");
            esbInfoVO.put("REQUESTTIME",FORMATTER.format(new Date()));
        }
    }
    /**
     * 判断字符串是否可以转换成数字
     * @param str
     * @return
     */
    public static boolean isBigDecimal(String str){
        if(str==null || str.trim().length() == 0){
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        int i = (chars[0] == '-') ? 1 : 0;
        if(i == sz) {
            return false;
        }
        //除了负号，第一位不能为'小数点'
        if(chars[i] == '.'){
            return false;
        }
        boolean radixPoint = false;
        for(; i < sz; i++){
            if(chars[i] == '.'){
                if(radixPoint) {
                    return false;
                }
                radixPoint = true;
            }else if(!(chars[i] >= '0' && chars[i] <= '9')){
                return false;
            }
        }
        return true;
    }
    /**
     * 获取月份对应的季度
     * @return
     */
    public static String getQuarter(int month){
        String quarter = null;
        if (month >= 1 && month <= 3) {
            quarter = "\"Q1\" ";
        } else if (month >= 4 && month <= 6) {
            quarter = "\"Q1\" \"Q2\" ";
        } else if (month >= 7 && month <= 9) {
            quarter = "\"Q1\" \"Q2\" \"Q3\" ";
        } else if (month >= 10 && month <= 12) {
            quarter = "\"Q1\" \"Q2\" \"Q3\" \"Q4\" ";
        }
        return quarter;
    }
}
