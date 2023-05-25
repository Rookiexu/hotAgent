import cn.rookiex.Attach;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.log4j.Log4j2;

import java.lang.management.ManagementFactory;
import java.util.Scanner;

/**
 * @author rookieX 2023/4/21
 */
@Log4j2
public class Main {

    public static void main(String[] args) throws Exception {
        Attach.attachMain(args);
    }
}
