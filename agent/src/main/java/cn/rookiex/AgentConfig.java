package cn.rookiex;

import lombok.Data;

/**
 * @author rookieX 2023/4/23
 */
public class AgentConfig {
    public static int serverPid = 0;

    /**
     * agent.jar 文件路径
     */
    public static String jarPath = "";

    /**
     * 对比差异的源代码路径
     */
    public static String srcPath = "";

    /**
     * 对比差异的心代码路径
     */
    public static String agentPath = "";

    /**
     * 输出的差异文件路径
     */
    public static String outPath = "";


}
