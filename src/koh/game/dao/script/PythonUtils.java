package koh.game.dao.script;

import lombok.extern.log4j.Log4j2;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * Created by Melancholia on 1/9/16.
 */
@Log4j2
public class PythonUtils {

    public static <T> T getJythonObject(Class<T> interfaceType,
                                        String pathToJythonModule) {
        Object javaInt = null;

        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.execfile(pathToJythonModule);
        String tempName = pathToJythonModule.substring(pathToJythonModule.lastIndexOf("/") + 1);
        tempName = tempName.substring(0, tempName.indexOf("."));
        String instanceName = tempName.toLowerCase();
        String javaClassName = tempName.substring(0, 1).toUpperCase() + tempName.substring(1);
        String objectDef = "=" + javaClassName + "()";
        interpreter.exec(instanceName + objectDef);

        javaInt = interpreter.get(instanceName).__tojava__(interfaceType);


        return (T) javaInt;
    }

    public static PyObject getPyClass(String moduleName, String clsName) {
        PyObject pyObject = null;
        PythonInterpreter interpreter = new PythonInterpreter();

        try {
            interpreter.exec("from " + moduleName + " import " + clsName);
            pyObject = interpreter.get(clsName);
        } catch (Exception e) {
            log.error("The Python module '" + moduleName + "' is not found: " + compactWhitespace(e.toString()));
        }
        return pyObject;
    }


    @SuppressWarnings("unchecked")
    public static <T> T createObject(Class<T> interfaceType, PyObject pyClass) {

        Object javaObj = null;

        PyObject newObj = pyClass.__call__();

        javaObj = newObj.__tojava__(interfaceType);

        return (T) javaObj;
    }


    public static Object createObject(Object interfaceType, String moduleName, String clsName) {

        PyObject pyObject = getPyClass(moduleName, clsName);

        Object javaObj = null;
        try {

            PyObject newObj = pyObject.__call__();

            javaObj = newObj.__tojava__(Class.forName(interfaceType.toString().substring(
                    interfaceType.toString().indexOf(" ") + 1, interfaceType.toString().length())));
        } catch (Exception ex) {
            log.error("Unable to create Python object: " + compactWhitespace(ex.toString()));
        }

        return javaObj;
    }


    public static String getModuleName(String s) {
        if (s == null) {
            return null;
        }
        int i = s.lastIndexOf('.');
        if (i < 0) {
            return s;
        }
        return s.substring(0, i);
    }


    public static String getClassName(String s) {
        if (s == null) {
            return null;
        }
        int i = s.lastIndexOf('.');
        if (i < 0) {
            return s;
        }
        return s.substring(i + 1, s.length());
    }

    public static String compactWhitespace(String str) {
        if (str == null) {
            return null;
        }
        boolean prevWS = false;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!prevWS) {
                    builder.append(' ');
                }
                prevWS = true;
            } else {
                prevWS = false;
                builder.append(c);
            }
        }
        return builder.toString().trim();
    }
}