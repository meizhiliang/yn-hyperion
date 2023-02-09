package yuanian.middleconsole.hyperion.common.model.enums;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/19
 * @menu: TODO
 */
public enum CommonEnum {

    QUERY_A("QUERY_A","A","预算及预算调整场景"),
    QUERY_B("QUERY_B","B","累计占用及累计执行场景"),
    QUERY_C("QUERY_C","C","预算场景"),
    /**
     * 特殊请求返回
     */
    REQUEST_UPPER("REQUEST_UPPER","-100","接口请求"),
    REQUEST_LOWER("REQUEST_LOWER","-200","接口请求"),
    OA_RESPONSE("S","100","OA请求响应成功"),


    /**
     * 调整预算中间表状态
     *  A 待同步; B 汇联易同步成功; C 汇联易同步失败; D 预算系统同步失败; E 同步完成
     */
    A("A","100","待同步"),
    B("B","200","预算系统同步成功"),
    C("C","300","预算系统同步失败"),
    D("D","400","汇联易同步失败"),
    E("E","500","同步完成"),

    /**
     * 币种
     */
    AED("288","AED",""),
    AUD("049","AUD",""),
    BRL("222","BRL",""),
    KRW("218","KRW",""),
    MYR("060","MYR",""),
    TRY("193","TRY",""),
    ZAR("076","ZAR",""),
    USD("USD_ALL","USD",""),
    JPY("JPY_ALL","JPY",""),
    EUR("EUR_ALL","EUR",""),
    /**
     * 预算系统请求返回
     */
    SUSS("S","200","响应成功"),
    WARN("W","400","请求入参异常"),
    MISS("M","400","预算信息不存在"),
    FAIL("E","500","程序执行异常");

    private String flag;
    private String code;
    private String title;


    private CommonEnum(String flag,String code,String title) {
        this.flag = flag;
        this.code = code;
        this.title = title;
    }

    public String getFlag() {
        return this.flag;
    }

    public String getCode() {
        return this.code;
    }

    public String getTitle() {
        return this.title;
    }
}
