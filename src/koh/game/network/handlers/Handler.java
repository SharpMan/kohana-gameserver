package koh.game.network.handlers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import java.util.Set;
import koh.protocol.MessageAttribute;
import koh.protocol.client.Message;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 *
 * @author Neo-Craft
 */
public class Handler {

    public static Set<Method> methods;
    public static Set<Class<?>> classes;
    public static Map<Integer, Method> handlers = new HashMap<>();
    public static Map<Integer, Class<? extends Message>> messages = new HashMap<>();

    public static int initialize() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("koh.game.network.handlers"))
                .setScanners(new MethodAnnotationsScanner()));

        methods = reflections.getMethodsAnnotatedWith(HandlerAttribute.class);
        methods.stream().forEach((method) ->
                handlers.put(method.getDeclaredAnnotation(HandlerAttribute.class).ID(), method)
        );
        methods.clear();
        return handlers.size();
    }

    public static Method getMethodByMessage(Integer id) {
        //if (HANDLERS.containsKey(id)) {
           return handlers.get(id);
        /*}
        return null;*/
    }

    public static int initializeMessage() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("koh.protocol.messages"))
                .setScanners(new TypeAnnotationsScanner()));
        classes = reflections.getTypesAnnotatedWith(MessageAttribute.class);

        for (Class<?> aClass : classes)
            messages.put(aClass.getDeclaredAnnotation(MessageAttribute.class).ID(), (Class<? extends Message>) aClass);

        classes.clear();
        return messages.size();
    }

}
