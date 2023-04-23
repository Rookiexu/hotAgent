package cn.rookiex.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.MD5;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.util.*;

import static cn.rookiex.AgentConfig.MAX_FILE_SIZE;


/**
 * @author rookieX 2023/4/20
 */
public class AgentFileTools {

    private static final Logger log = LogManager.getLogger(AgentFileTools.class);


    public static Map<String, byte[]> findClass(String path) {
        Map<String, byte[]> resultMap = new HashMap<>();

        File file = FileUtil.file(path);
        if (checkPathErr(path, file)) return null;
        for (File loopFile : FileUtil.loopFiles(file, pathname -> pathname != null && pathname.getName().endsWith(FileUtil.CLASS_EXT))) {
            byte[] bytes = FileUtil.readBytes(loopFile);
            String className = readClassName(bytes);
            resultMap.put(className, bytes);
        }

        return resultMap;
    }


    public static Map<String, byte[]> findDiffClass(String srcPath, String newPath) {
        log.info("开始检查目录 src : {}, new : {}, 差异文件", srcPath , newPath);
        Map<String, String> srcMD5 = new HashMap<>();
        Map<String, byte[]> diffClass = new HashMap<>();
        Set<String> onlyNewClass = new HashSet<>();

        //获取新旧目录class文件
        File srcFile = FileUtil.file(srcPath);
        if (checkPathErr(srcPath, srcFile)) return null;

        File newFile = FileUtil.file(newPath);
        if (checkPathErr(newPath, newFile)) return null;

        List<File> list = FileUtil.loopFiles(srcPath, pathname -> pathname != null && pathname.getName().endsWith(FileUtil.CLASS_EXT));
        if (list == null) {
            log.error("srcFile list is null : " + srcPath);
            return null;
        }
        log.info("目录 src : {}, class文件数量 : {}", srcPath , list.size());

        MD5 md5 = MD5.create();
        for (File s : list) {
            if (checkFileErr(s.getName(), s)) return null;
            byte[] bytes = FileUtil.readBytes(s);
            String className = readClassName(bytes);
            String md5Str = md5.digestHex(bytes);
            srcMD5.put(className, md5Str);
        }

        List<File> newList = FileUtil.loopFiles(newFile, pathname -> pathname != null && pathname.getName().endsWith(FileUtil.CLASS_EXT));
        if (newList == null) {
            log.error("newFile list is null : " + newPath);
            return null;
        }

        log.info("目录 newClass : {}, class文件数量 : {}", newPath , newList.size());
        for (File s : newList) {
            if (checkFileErr(s.getName(), s)) return null;
            byte[] bytes = FileUtil.readBytes(s);
            String className = readClassName(bytes);

            if (srcMD5.containsKey(className)) {
                String md5Str = md5.digestHex(bytes);
                if (!srcMD5.get(className).equals(md5Str)) {
                    diffClass.put(className, bytes);
                }
            } else {
                onlyNewClass.add(className);
            }
        }

        if (onlyNewClass.size() > 0) {
            log.error("新目录存在新class类,相关文件无法被热更新加载 : " + onlyNewClass);
        }

        if (diffClass.isEmpty()) {
            log.error("差异文件数量为0");
            return null;
        } else {
            log.info("差异文件数量 : " + diffClass.size());
            log.info("差异文件 : " + diffClass.keySet());
        }

        return diffClass;
    }

    private static boolean checkPathErr(String srcPath, File file) {
        if (checkFileErr(srcPath, file)) {
            return true;
        }
        if (!file.isDirectory()) {
            log.error("path is not directory : " + srcPath);
            return true;
        }
        return false;
    }

    private static boolean checkFileErr(String srcPath, File file) {
        if (file == null) {
            log.error("path is null : ");
            return true;
        }
        if (!file.exists()) {
            log.error("path is not exists : " + srcPath);
            return true;
        }

        if (!file.isDirectory() && file.length() >= MAX_FILE_SIZE) {
            log.error("file size: " + file.length() + " >= " + MAX_FILE_SIZE + ", path: " + srcPath);
            return true;
        }
        return false;
    }

    private static String readClassName(final byte[] bytes) {
        return new ClassReader(bytes).getClassName().replace("/", ".");
    }

    public static void clearAgentPath(String agentPath) {
        File file = FileUtil.file(agentPath);
        if (file != null && file.exists()) {
            FileUtil.del(file);
        }
    }

    public static Set<String> saveAgentFile(String agentPath, Map<String, byte[]> diffClass) {
        Set<String> addFile = new HashSet<>();
        File file = FileUtil.mkdir(agentPath);
        if (file != null && file.exists()) {
            for (String s : diffClass.keySet()) {
                String path = file.getPath() + FileUtil.FILE_SEPARATOR + s + FileUtil.CLASS_EXT;
                File touch = FileUtil.touch(path);
                FileUtil.writeBytes(diffClass.get(s), touch);
                addFile.add(path);
            }
        }
        return addFile;
    }
}
