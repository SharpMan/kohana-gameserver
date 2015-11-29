package koh.game.app;

import com.google.inject.Inject;
import koh.patterns.services.api.Service;
import koh.game.utils.Settings;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;

public class Loggers implements Service {

    @Inject
    private Settings settings;

    @Override
    public void start() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        if(settings.getBoolElement("Logging.Debug")) {
            config.getRootLogger().removeAppender("Console");
            config.getRootLogger().addAppender(config.getAppender("Console"),
                    Level.DEBUG, null);

            config.getLoggerConfig("GameServer").addAppender(config.getAppender("Console"),
                    Level.DEBUG, null);
        }

        ctx.updateLoggers(config);
    }

    @Override
    public void stop() {
        Configurator.shutdown((LoggerContext)LogManager.getContext(false));
    }
}
