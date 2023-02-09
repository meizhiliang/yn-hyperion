package yuanian.middleconsole.hyperion.model.enums;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/27
 * @menu: TODO
 */
public enum ScennarioEnum {

    S01("S01", "实际数"),
    S02("S02", "预算数"),
    S03("S03", "调整数"),
    S04("S04", "占用数"),
    S14("S14", "累计实际数"),
    S15("S15", "累计占用数"),
    S05("S05", "上年预计数");

    private String code;
    private String title;

    private ScennarioEnum(String code, String title) {
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
