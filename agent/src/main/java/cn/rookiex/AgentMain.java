package cn.rookiex;

import lombok.extern.log4j.Log4j2;
import cn.rookiex.file.DiffClassFinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author rookieX 2023/4/20
 */
public class AgentMain {

    private static final Logger log = LogManager.getLogger(AgentMain.class);

    public static void agentmain(String args, Instrumentation inst) {
        DiffClassFinder diffClassFinder = new DiffClassFinder();
        Map<String, byte[]> diffClass = diffClassFinder.findDiffClass("bin", "agent");
        System.out.println(diffClass.keySet());

        reload(inst, diffClass);
    }


    public static void reload(Instrumentation inst, Map<String, byte[]> bytesMap) {
        List<String> redefineModel = new ArrayList<>();
        List<ClassDefinition> definitions = new ArrayList<>();
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (bytesMap.containsKey(clazz.getName())) {

                definitions.add(new ClassDefinition(clazz, bytesMap.get(clazz.getName())));
                redefineModel.add(clazz.getName());
                log.info("Try redefine class name: {}, ClassLoader: {}", clazz.getName(), clazz.getClassLoader());
            }
        }

        try {
            if (definitions.isEmpty()) {
                log.error("These classes are not found in the JVM and may not be loaded: " + bytesMap.keySet());
                return;
            }
            inst.redefineClasses(definitions.toArray(new ClassDefinition[0]));
        } catch (Throwable e) {
            String message = "redefine error! " + e;
            log.error(message, e);
        }
    }
}
