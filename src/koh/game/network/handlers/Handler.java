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

    public static Map<Integer, Method> handlers = new HashMap<>();
    public static Map<Integer, Class<? extends Message>> messages = new HashMap<>();

    public static final int initialize() {
        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("koh.game.network.handlers"))
                .setScanners(new MethodAnnotationsScanner()));

        reflections.getMethodsAnnotatedWith(HandlerAttribute.class).forEach((method) ->
                handlers.put(method.getDeclaredAnnotation(HandlerAttribute.class).ID(), method)
        );
        return handlers.size();
    }

    public static Method getMethodByMessage(Integer id) {
        return handlers.get(id);
    }

    public static final int initializeMessage() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("koh.protocol.messages"))
                .setScanners(new TypeAnnotationsScanner()));

        reflections.getTypesAnnotatedWith(MessageAttribute.class).forEach(aClass -> messages.put(aClass.getDeclaredAnnotation(MessageAttribute.class).ID(), (Class<? extends Message>) aClass));

        return messages.size();
    }

}
