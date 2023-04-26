package cn.rookiex;


import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author rookieX 2023/4/20
 */
public class AgentMain {


    public static void agentmain(String args, Instrumentation inst) {
        String path = "agent";
        if (args != null){
            path = args;
        }
        Map<String, byte[]> findClass = AgentFileTools.findClass(path);
        if (findClass != null && !findClass.isEmpty()) {
            System.out.println("hot agent path {"+path+"} , class : {"+findClass.keySet()+"}");
            reload(inst, findClass);
        }else {
            System.out.println("err ==> " +"hot agent class fail ,path is empty : {"+path+"} ");
        }
    }


    public static void reload(Instrumentation inst, Map<String, byte[]> bytesMap) {
        List<String> redefineModel = new ArrayList<>();
        List<ClassDefinition> definitions = new ArrayList<>();
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (bytesMap.containsKey(clazz.getName())) {

                definitions.add(new ClassDefinition(clazz, bytesMap.get(clazz.getName())));
                redefineModel.add(clazz.getName());
                System.out.println("Try redefine class name: {"+clazz.getName()+"}, ClassLoader: {"+clazz.getClassLoader()+"}");
            }
        }

        if (definitions.isEmpty()) {
            System.out.println("err ==> " +"These classes are not found in the JVM and may not be loaded: {"+bytesMap.keySet()+"}");
            return;
        }

        if (redefineModel.size() != bytesMap.size()) {
            for (String s : redefineModel) {
                bytesMap.remove(s);
            }
            System.out.println("err ==> " +"These classes are not found in the JVM and may not be loaded: {"+bytesMap.keySet()+"}");
            return;
        }

        try {
            inst.redefineClasses(definitions.toArray(new ClassDefinition[0]));
            System.out.println("redefine class finish: {"+redefineModel+"}");
        } catch (Throwable e) {
            String message = "redefine error! " + e;
            System.out.println("err ==> " + message + e.toString());
        }
    }
}
