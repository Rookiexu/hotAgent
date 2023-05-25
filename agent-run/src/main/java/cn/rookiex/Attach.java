package cn.rookiex;

import cn.hutool.core.io.FileUtil;
import cn.rookiex.file.AgentFileTools;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * 连接指定服务器,并热更新代码
 *
 * @author rookieX 2023/4/23
 */
@Log4j2
public class Attach {

    public static void attachMain(String[] args) throws IOException, AttachNotSupportedException {
        System.out.println();

        log.info("start agent vm args : " + Arrays.toString(args));

        String runMode = "check";
        String serverType = "GameServer";

        if (args.length == 1){
            String arg = args[0];
            if (arg.equals("agent") || arg.equals("all")){
                runMode = arg;
            }
        }
        if (args.length == 2){
            String arg = args[0];
            serverType = arg;
        }


        log.info("start agent vm, runMode {}", runMode);
        AgentConfig.initConfig();
        String pidPath = AgentConfig.pidPath.replace("?", serverType);
        String serverPid = initPid(pidPath);

        String srcPath = AgentConfig.srcClassPath.replace("?", serverType);
        String newtPath = AgentConfig.newClassPath.replace("?", serverType);
        String agentPath = AgentConfig.agentClassPath.replace("?", serverType);
        String jarPath = AgentConfig.agentJarPath.replace("?", serverType);

        if (runMode.equals("all") || runMode.equals("check")) {
            Map<String, byte[]> diffClass = AgentFileTools.findDiffClass(srcPath, newtPath);

            if (diffClass == null) {
                log.error("not find any different class ");
                System.exit(-1);
            } else {
                log.info("差异class : {} ", diffClass.keySet());
            }
            AgentFileTools.clearAgentPath(agentPath);
            log.info("完成目录清理 : {} ", agentPath);

            Set<String> strings = AgentFileTools.saveAgentFile(agentPath, diffClass);
            log.info("目录 {} ,写入class文件 : {} ", agentPath, strings);
        }

        if (runMode.equals("all") || runMode.equals("agent")){
            log.info("开始热更程序, runMode {}", runMode);
            attachLoadAgent(serverPid, jarPath, agentPath);
        }

        log.info("finish agent vm, runMode {}", runMode);
    }

    private static String initPid(String pidPath) {
        String serverPid = null;
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
            return serverPid;
        } else {
            log.error("can not fid server pid, path {}", pidPath);
        }
        System.exit(-1);
        return serverPid;
    }


    /**
     * @param pid       连接项目pid
     * @param jarPath   执行agentMain的jar包路径
     * @param agentPath
     */
    public static void attachLoadAgent(String pid, String jarPath, String agentPath) throws IOException, AttachNotSupportedException {
        log.info("start attach vm, pid: {} ,path : {}", pid, jarPath);
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
            log.info("start loadAgent, pid : {}, jarPath : {}",  pid, jarPath);
            vm.loadAgent(jarPath, agentPath);
            vm.detach();
            log.info("end attach, pid : {}, jarPath : {}", pid, jarPath);
        } catch (AgentLoadException | AgentInitializationException e) {
            throw new RuntimeException(e);
        } finally {
            if (vm != null) {
                vm.detach();
            }
        }

        log.info("end attach");
    }
}
