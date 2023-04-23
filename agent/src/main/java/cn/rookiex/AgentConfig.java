package cn.rookiex;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author rookieX 2023/4/23
 */
@Log4j2
public class AgentConfig {
    public static String serverPid;

    /**
     * agent.jar 文件路径
     */
    public static String jarPath = "";

    /**
     * 对比差异的源代码路径
     */
    public static String srcPath = "oldClass";

    /**
     * 对比差异的心代码路径
     */
    public static String newtPath = "newClass";

    /**
     * 热更文件路径
     */
    public static String agentPath = "agent";


    public static long MAX_FILE_SIZE = 1024 * 1024;

    public static void initConfig() {
        initConfig("agent.properties");
    }

    public static void initConfig(String path) {
        Properties properties = new Properties();
        try {
            properties.load(AgentConfig.class.getClassLoader().getResourceAsStream(path));
            initProperties(properties);
        } catch (IOException | IllegalAccessException e) {
            log.error(e, e);
        }
    }

    private static void initProperties(Properties properties) throws IllegalAccessException {
        Field[] fields = AgentConfig.class.getFields();
        setFieldValue(properties, fields);
        Field[] declaredFields = AgentConfig.class.getDeclaredFields();
        setFieldValue(properties, declaredFields);
    }

    private static void setFieldValue(Properties properties, Field[] fields) throws IllegalAccessException {
        for (Field field : fields) {
            Class<?> type = field.getType();
            String name = field.getName();
            String property = properties.getProperty(name);
            if (property != null) {
                field.set(null, objTypeChange(property, type));
            }
        }
    }

    private static Object objTypeChange(String from, Class<?> parameterType) {
        String simpleName = parameterType.getSimpleName();
        switch (simpleName) {
            case "Integer":
            case "int":
                return Integer.parseInt(from);
            case "Boolean":
            case "boolean":
                return Boolean.parseBoolean(from);
            case "Long":
            case "long":
                return Long.parseLong(from);
            case "String":
                return from;
        }
        log.error("AgentConfig 配置加载异常,存在不能加载类型 : {}", from);
        System.exit(-1);
        return null;
    }
}
