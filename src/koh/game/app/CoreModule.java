package koh.game.app;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import koh.game.utils.Settings;

public class CoreModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    Settings provideConfiguration() {
        return new Settings("/Settings.ini");
    }

}
