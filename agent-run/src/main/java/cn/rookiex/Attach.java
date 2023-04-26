package cn.rookiex;

import cn.rookiex.file.AgentFileTools;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
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
        for (String arg : args) {
            if (arg.equals("agent") || arg.equals("all")){
                runMode = arg;
            }
        }

        log.info("start agent vm, runMode {}", runMode);
        AgentConfig.initConfig();

        if (runMode.equals("all") || runMode.equals("check")) {
            Map<String, byte[]> diffClass = AgentFileTools.findDiffClass(AgentConfig.srcPath, AgentConfig.newtPath);

            if (diffClass == null) {
                log.error("not find any different class ");
                System.exit(-1);
            } else {
                log.info("差异class : {} ", diffClass.keySet());
            }
            AgentFileTools.clearAgentPath(AgentConfig.agentPath);
            log.info("完成目录清理 : {} ", AgentConfig.agentPath);

            Set<String> strings = AgentFileTools.saveAgentFile(AgentConfig.agentPath, diffClass);
            log.info("目录 {} ,写入class文件 : {} ", AgentConfig.agentPath, strings);
        }

        if (runMode.equals("all") || runMode.equals("agent")){
            log.info("开始热更程序, runMode {}", runMode);
            attachLoadAgent(AgentConfig.serverPid, AgentConfig.jarPath, AgentConfig.agentPath);
        }

        log.info("finish agent vm, runMode {}", runMode);
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
