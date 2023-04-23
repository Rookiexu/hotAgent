package cn.rookiex;

import com.sun.tools.attach.VirtualMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author rookieX 2023/4/21
 */
public class AttachMain {

    private static final Logger log = LogManager.getLogger(AttachMain.class);

    public static void main(String[] args) {
        log.info("start attach vm");
        new Thread(()->{
            VirtualMachine vm = null;
            try {
                String pid = "87396";
//                if (FileUtil.exist("server.pid")) {
//                    pid = FileUtil.readUtf8String("server.pid").replace(" ", "").replace("\r\n", "")
//                            .replace("\n", "");
//                    if (pid.isEmpty()) {
//                        log.error("cat not pid");
//                        System.exit(-1);
//                    }
//
//                    log.info("read pid:" + pid);
//                } else {
//                    log.error("can not fid serverpid");
//                    System.exit(-1);
//                }

                log.info("begin attach, pid:" + pid);
                vm = VirtualMachine.attach(pid);
                try {
                    log.info("begin load, vm:" + vm.toString() + ",pid:" + pid);
                    vm.loadAgent("E:\\IdeaProjects\\htAgent\\agent\\target\\agent-1.0.0.jar");
                    vm.detach();
                    log.info("end attach,  vm:" + vm.toString() + ",pid:" + pid);
                } finally {
                    if (vm != null) {
                        vm.detach();
                    }
                }
            } catch (Exception e) {
                log.error(e, e);
            }

            log.info("end AgentAttach");
        }).start();
    }
}
