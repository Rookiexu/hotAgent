package cn.rookiex;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.MD5;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.util.*;


/**
 * @author rookieX 2023/4/20
 */
public class AgentFileTools {

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
        System.out.println("开始检查目录 src : {" + srcPath + "}, new : {" + newPath + "}, 差异文件");
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
            System.out.println("err ==> " + "srcFile list is null : " + srcPath);
            return null;
        }
        System.out.println("目录 src : {" + srcPath + "}, class文件数量 : {" + list.size() + "}");

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
            System.out.println("err ==> " + "newFile list is null : " + newPath);
            return null;
        }

        System.out.println("目录 newClass : {" + newPath + "}, class文件数量 : {" + newList.size() + "}");
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
            System.out.println("err ==> " + "新目录存在新class类,相关文件无法被热更新加载 : " + onlyNewClass);
        }

        if (diffClass.isEmpty()) {
            System.out.println("err ==> " + "差异文件数量为0");
            return null;
        } else {
            System.out.println("差异文件数量 : " + diffClass.size());
            System.out.println("差异文件 : " + diffClass.keySet());
        }

        return diffClass;
    }

    private static boolean checkPathErr(String srcPath, File file) {
        if (checkFileErr(srcPath, file)) {
            return true;
        }
        if (!file.isDirectory()) {
            System.out.println("err ==> " + "path is not directory : " + srcPath);
            return true;
        }
        return false;
    }

    private static boolean checkFileErr(String srcPath, File file) {
        if (file == null) {
            System.out.println("err ==> " + "path is null : ");
            return true;
        }
        if (!file.exists()) {
            System.out.println("err ==> " + "path is not exists : " + srcPath);
            return true;
        }

        if (!file.isDirectory() && file.length() >= 1024 * 1024) {
            System.out.println("err ==> " + "file size: " + file.length() + " >= " + 1024 * 1024 + ", path: " + srcPath);
            return true;
        }
        return false;
    }

    private static String readClassName(final byte[] bytes) {
        return new ClassReader(bytes).getClassName().replace("/", ".");
    }

}
