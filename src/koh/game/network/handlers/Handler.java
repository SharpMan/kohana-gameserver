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

    public static Set<Method> Methods;
    public static Set<Class<?>> Classes;
    public static Map<Integer, Method> Handlers = new HashMap<>();
    public static Map<Integer, Class<?>> Messages = new HashMap<>();

    public static int Initialize() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("koh.game.network.handlers"))
                .setScanners(new MethodAnnotationsScanner()));
        Methods = reflections.getMethodsAnnotatedWith(HandlerAttribute.class);
        Methods.stream().forEach((method) -> {
            Handlers.put(method.getDeclaredAnnotation(HandlerAttribute.class).ID(), method);
        });
        Methods.clear();
        return Handlers.size();
    }

    public static Method getMethodByMessage(Integer id) {
        //if (Handlers.containsKey(id)) {
           return Handlers.get(id);
        /*}
        return null;*/
    }

    public static int InitializeMessage() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("koh.protocol.messages"))
                .setScanners(new TypeAnnotationsScanner()));
        Classes = reflections.getTypesAnnotatedWith(MessageAttribute.class);

        for (Class<?> aClass : Classes) {
            Messages.put(((Message) aClass.newInstance()).getMessageId(), aClass);
        }
        Classes.clear();
        return Messages.size();
    }

}
