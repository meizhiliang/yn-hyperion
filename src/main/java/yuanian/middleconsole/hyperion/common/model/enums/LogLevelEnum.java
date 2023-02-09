package yuanian.middleconsole.hyperion.common.model.enums;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/19
 * @menu: TODO
 */
public enum LogLevelEnum {

    DEBUG(10, "调试"),
    INFO(30, "一般"),
    WARN(50, "警告"),
    ERROR(70, "错误"),
    UNSPECIFIED(0, "未指定");

    private int level;
    private String title;

    private LogLevelEnum(int level, String title) {
        this.level = level;
        this.title = title;
    }

    public int getLevel() {
        return this.level;
    }

    public String getTitle() {
        return this.title;
    }
}
