package koh.game.dao;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import koh.game.dao.api.*;
import koh.game.dao.mysql.*;
import koh.game.dao.script.PlayerCommandDAOImpl;
import koh.game.dao.sqlite.*;
import koh.patterns.services.api.DependsOn;
import koh.patterns.services.api.Service;
import koh.game.app.Loggers;
import koh.game.app.MemoryService;
import koh.game.utils.Settings;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Neo-Craft
 */
@Log4j2
@DependsOn({Loggers.class, MemoryService.class})
public class DatabaseSource implements Service {

    @Override
    public void inject(Injector injector){
    }

    @Override
    public void configure(Binder binder){
        binder.bind(AccountDataDAO.class).to(AccountDataDAOImpl.class).asEagerSingleton();
        binder.bind(AreaDAO.class).to(AreaDAOImpl.class).asEagerSingleton();
        binder.bind(D2oDAO.class).to(D2oDAOImpl.class).asEagerSingleton();
        binder.bind(ExpDAO.class).to(ExpDAOImpl.class).asEagerSingleton();
        binder.bind(GuildDAO.class).to(GuildDAOImpl.class).asEagerSingleton();
        binder.bind(GuildEmblemDAO.class).to(GuildEmblemDAOImpl.class).asEagerSingleton();
        binder.bind(GuildMemberDAO.class).to(GuildMemberDAOImpl.class).asEagerSingleton();
        binder.bind(ItemDAO.class).to(ItemDAOImpl.class).asEagerSingleton();
        binder.bind(ItemTemplateDAO.class).to(ItemTemplateDAOImpl.class).asEagerSingleton();
        binder.bind(JobDAO.class).to(JobDAOImpl.class).asEagerSingleton();
        binder.bind(MapDAO.class).to(MapDAOImpl.class).asEagerSingleton();
        binder.bind(MonsterDAO.class).to(MonsterDAOImpl.class).asEagerSingleton();
        binder.bind(MountDAO.class).to(MountDAOImpl.class).asEagerSingleton();
        binder.bind(MountInventoryDAO.class).to(MountInventoryDAOImpl.class).asEagerSingleton();
        binder.bind(NpcDAO.class).to(NpcDAOImpl.class).asEagerSingleton();
        binder.bind(PaddockDAO.class).to(PaddockDAOImpl.class).asEagerSingleton();
        binder.bind(PetInventoryDAO.class).to(PetInventoryDAOImpl.class).asEagerSingleton();
        binder.bind(PlayerCommandDAO.class).to(PlayerCommandDAOImpl.class).asEagerSingleton();
        binder.bind(PlayerDAO.class).to(PlayerDAOImpl.class).asEagerSingleton();
        binder.bind(SpellDAO.class).to(SpellDAOImpl.class).asEagerSingleton();
        binder.requestStaticInjection(DAO.class);
    }

    @Inject private Settings settings;

    private static HikariDataSource dataSource;

    @Override
    public void start() {
        if(dataSource != null && !dataSource.isClosed())
            dataSource.close();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + settings.getStringElement("Database.Host") + "/" + settings.getStringElement("Database.Name"));
        config.setUsername(settings.getStringElement("Database.User"));
        config.setPassword(settings.getStringElement("Database.Password"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(50);

        this.dataSource = new HikariDataSource(config);

        Field[] fields = DAO.class.getDeclaredFields();
        for(Field field : fields){
           if(Modifier.isStatic(field.getModifiers()) && Service.class.isAssignableFrom(field.getType())){
               try {
                   field.setAccessible(true);
                   ((Service)field.get(null)).start();
               } catch (Exception e) {
                   log.error(e);
                   log.warn(e.getMessage());
               }
           }
        }
    }

    @Override
    public void stop() {

        Field[] fields = DAO.class.getDeclaredFields();
        for(Field field : fields){
            if(Modifier.isStatic(field.getModifiers()) && Service.class.isAssignableFrom(field.getType())){
                try {
                    field.setAccessible(true);
                    ((Service)field.get(null)).stop();
                } catch (Exception e) {
                    log.error(e);
                    log.warn(e.getMessage());
                }
            }
        }

        if(dataSource != null)
            dataSource.close();

    }

    public Connection getConnectionOfPool() throws SQLException {
        return dataSource.getConnection();
    }

    public ConnectionStatement<Statement> createStatement() throws SQLException {
        Connection connection = this.getConnectionOfPool();
        return new ConnectionStatement<>(connection, connection.createStatement());
    }

    public ConnectionStatement<PreparedStatement> prepareStatement(String query) throws SQLException {
        Connection connection = this.getConnectionOfPool();
        return new ConnectionStatement<>(connection, connection.prepareStatement(query));
    }

    public ConnectionStatement<PreparedStatement> prepareStatement(String query, boolean autoGeneratedKeys) throws SQLException {
        Connection connection = this.getConnectionOfPool();
        PreparedStatement statement = connection.prepareStatement(query,
                autoGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
        return new ConnectionStatement<>(connection, statement);
    }

    public ConnectionResult executeQuery(String query) throws SQLException {
        Connection connection = this.getConnectionOfPool();
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(300);
        return new ConnectionResult(connection, statement, statement.executeQuery(query));
    }

    public ConnectionResult executeQuery(String query, int secsTimeout) throws SQLException {
        Connection connection = this.getConnectionOfPool();
        Statement statement = connection.createStatement();
        if(secsTimeout > 0)
            statement.setQueryTimeout(secsTimeout);
        return new ConnectionResult(connection, statement, statement.executeQuery(query));
    }

}
