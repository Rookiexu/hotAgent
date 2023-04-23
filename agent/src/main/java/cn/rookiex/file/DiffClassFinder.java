package cn.rookiex.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.MD5;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.util.*;

/**
 * @author rookieX 2023/4/20
 */
public class DiffClassFinder {

    private static final Logger log = LogManager.getLogger(DiffClassFinder.class);

    private static long MAX_FILE_SIZE = 1024 * 1024;

    private Map<String, String> srcMD5 = new HashMap<>();

    private Map<String, byte[]> diffClass = new HashMap<>();

    private Set<String> onlyNewClass = new HashSet<>();

    public Map<String, byte[]> findDiffClass(String srcPath, String newPath){
        //获取新旧目录class文件
        File srcFile = FileUtil.file(srcPath);
        if (checkPathErr(srcPath, srcFile)) return null;

        File newFile = FileUtil.file(newPath);
        if (checkPathErr(newPath, newFile)) return null;

        List<File> list = FileUtil.loopFiles(srcFile);
        if (list == null){
            log.error("srcFile list is null : " + srcPath);
            return null;
        }

        MD5 md5 = MD5.create();
        for (File s : list) {
            if (s.getName().endsWith(".class")){
                if (checkFileErr(s.getName(), s)) return null;
                byte[] bytes = FileUtil.readBytes(s);
                String className = readClassName(bytes);
                String md5Str = md5.digestHex(bytes);
                srcMD5.put(className, md5Str);
            }
        }

        List<File> newList = FileUtil.loopFiles(newFile);
        if (newList == null){
            log.error("newFile list is null : " + newPath);
            return null;
        }
        for (File s : newList) {
            if (s.getName().endsWith(".class")){
                if (checkFileErr(s.getName(), s)) return null;
                byte[] bytes = FileUtil.readBytes(s);
                String className = readClassName(bytes);

                if (srcMD5.containsKey(className)){
                    String md5Str = md5.digestHex(bytes);
                    if (!srcMD5.get(className).equals(md5Str)){
                        diffClass.put(className, bytes);
                    }
                }else {
                    onlyNewClass.add(className);
                }
            }
        }

        if (onlyNewClass.size() > 0){
            log.error("新目录存在新class类,相关文件无法被热更新加载 : " + onlyNewClass);
        }

        if( diffClass.isEmpty()){
            log.error("差异文件数量为0");
            return null;
        }else {
            log.info("差异文件数量 : " + diffClass.size());
        }

        return diffClass;
    }

    private static boolean checkPathErr(String srcPath, File file) {
        if (checkFileErr(srcPath, file)){
            return true;
        }
        if (!file.isDirectory()){
            log.error("path is not directory : " + srcPath);
            return true;
        }
        return false;
    }

    private static boolean checkFileErr(String srcPath, File file) {
        if (file == null){
            log.error("path is null : ");
            return true;
        }
        if (!file.exists()){
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
}
