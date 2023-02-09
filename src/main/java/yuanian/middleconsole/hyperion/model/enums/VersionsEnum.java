package yuanian.middleconsole.hyperion.model.enums;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/27
 * @menu: TODO
 */
public enum VersionsEnum {

    GD_curver("GD_curver", "滚动当前版"),
    ZX_ver("ZX_ver", "执行版"),
    V01("V01", "编制第一版"),
    V02("V02", "编制第二版"),
    V03("V03", "编制第三版"),
    OA_ver("OA_ver", "OA接口版"),
    HLY_ver("HLY_ver", "汇联易接口版"),
    HFM_ver("HFM_ver", "合并接口版");

    private String code;
    private String title;

    private VersionsEnum(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return this.code;
    }

    public String getTitle() {
        return this.title;
    }
}
