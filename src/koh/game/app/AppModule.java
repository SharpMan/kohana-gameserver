package koh.game.app;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import koh.patterns.services.ServicesProvider;
import koh.patterns.services.api.Service;
import org.reflections.Reflections;

public class AppModule extends AbstractModule {

    static {
        Reflections.log = null;
    }

    private Injector app;

    public AppModule() {
        this.app = Guice.createInjector(this);
    }

    public final ServicesProvider create(Service... services) {
        ServicesProvider provider = new ServicesProvider("GameServices", services);
        app = app.createChildInjector(provider);
        return provider;
    }

    public Injector resolver() {
        return app;
    }

    @Override
    protected void configure() {
        install(new CoreModule());
    }

}
