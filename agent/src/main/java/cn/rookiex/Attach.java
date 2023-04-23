package cn.rookiex;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

/**
 * 连接指定服务器,并热更新代码
 *
 * @author rookieX 2023/4/23
 */
@Log4j2
public class Attach {

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
