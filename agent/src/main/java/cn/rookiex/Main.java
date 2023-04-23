package cn.rookiex;

import cn.rookiex.file.AgentFileTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;

/**
 * @author rookieX 2023/4/21
 */
public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);
    AgentFileTools agentFileTools = new AgentFileTools();

    public static void main(String[] args) throws Exception {

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
            Attach.attachLoadAgent(AgentConfig.serverPid, AgentConfig.jarPath, AgentConfig.agentPath);
        }

        log.info("finish agent vm, runMode {}", runMode);
    }
}
