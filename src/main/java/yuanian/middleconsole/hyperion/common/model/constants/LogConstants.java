package yuanian.middleconsole.hyperion.common.model.constants;

import yuanian.middleconsole.hyperion.common.model.enums.LogLevelEnum;

import java.util.Arrays;
import java.util.List;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/19
 * @menu: TODO
 */
public class LogConstants {

    public static final String SYSTEM_PROPERTY_SERVER_KEY = "SERVER_KEY";
    public static final String NULL_TARGET_METHOD_FUNCTION = "NULL_TARGET_METHOD_FUNCTION";
    public static final String NULL_TARGET_METHOD_OPERATION = "NULL_TARGET_METHOD_OPERATION";
    public static final String TIMEZONE_GMT8_STR = "GMT+8";
    public static final String DATE_FORMAT_PATTERN = "yyyyMMddHHmmssSSS";
    public static final String LACALDATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String CLASS_PATH_SEPARATOR = "\\.";
    public static final List<String> CLASS_LIST_EXCLUDED_FROM_THREAD_STACK_FOR_LOGFILE_SERVICE = Arrays.asList("Thread.java", "ILogFileService.java", "LogFileService.java", "<generated>", "MethodProxy.java", "CglibAopProxy.java", "ReflectiveMethodInvocation.java", "MethodInvocationProceedingJoinPoint.java", "TraceLogAspect.java", "NativeMethodAccessorImpl.java", "DelegatingMethodAccessorImpl.java", "Method.java", "AbstractAspectJAdvice.java", "AspectJAroundAdvice.java", "ExposeInvocationInterceptor.java", "ExceptionAdviceHandler.java");
    public static final List<String> CLASS_LIST_EXCLUDED_FROM_THREAD_STACK_FOR_LOG_SERVICE = Arrays.asList("Thread.java", "ILogService.java", "LogService.java", "<generated>", "MethodProxy.java", "CglibAopProxy.java", "ReflectiveMethodInvocation.java", "MethodInvocationProceedingJoinPoint.java", "TraceLogAspect.java", "NativeMethodAccessorImpl.java", "DelegatingMethodAccessorImpl.java", "Method.java", "AbstractAspectJAdvice.java", "AspectJAroundAdvice.java", "ExposeInvocationInterceptor.java", "ExceptionAdviceHandler.java");
    public static final String LOG_ENTER_METHOD = "ENTER [{}] METHOD";
    public static final String LOG_LEAVE_METHOD = "LEAVE [{}] METHOD";
    public static final LogLevelEnum SYNC_LOG_LEVEL_THREAD_LOCAL;

    private LogConstants() {
    }

    static {
        SYNC_LOG_LEVEL_THREAD_LOCAL = LogLevelEnum.DEBUG;
    }
}
