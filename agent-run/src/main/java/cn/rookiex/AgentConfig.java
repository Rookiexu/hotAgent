package cn.rookiex;

import cn.hutool.core.io.FileUtil;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * @author rookieX 2023/4/23
 */
@Log4j2
public class AgentConfig {
    public static String serverPid;

    public static String pidPath;

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

        if (FileUtil.exist(pidPath)) {
            String s = FileUtil.readString(pidPath, Charset.defaultCharset());
            String[] split = s.split("\\.");
            if (split.length < 1) {
                log.error("cat not pid , path : {}, value : {}", pidPath, s);
                System.exit(-1);
            }
            serverPid = split[0];
            if (serverPid == null) {
                log.error("cat not pid , path : {}", pidPath);
                System.exit(-1);
            }

            log.info("load pid:" + serverPid);
        } else {
            log.error("can not fid server pid, path {}", pidPath);
            System.exit(-1);
        }
    }

    public static void initConfig(String path) {
        Properties properties = new Properties();
        path = checkPropertyPath(path);
        System.out.println(path);
        try {
            properties.load(FileUtil.getInputStream(path));
            initProperties(properties);
        } catch (IOException | IllegalAccessException e) {
            log.error(e, e);
        }
    }

    private static String checkPropertyPath(String path) {
        String newPath = path;
        boolean exist = FileUtil.exist(newPath);
        if (exist) {
            return newPath;
        }
        newPath = "config" + FileUtil.FILE_SEPARATOR + path;
        exist = FileUtil.exist(newPath);
        if (exist) {
            return newPath;
        }


        URL resource = AgentConfig.class.getResource("/");
        if (resource != null) {
            String resourcePath = resource.getPath();
            newPath = resourcePath + FileUtil.FILE_SEPARATOR + path;
            exist = FileUtil.exist(newPath);
            if (exist) {
                return newPath;
            }

            newPath = resourcePath + FileUtil.FILE_SEPARATOR + "config" + FileUtil.FILE_SEPARATOR + path;
            exist = FileUtil.exist(newPath);
            if (exist) {
                return newPath;
            }
        }

        return path;
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
