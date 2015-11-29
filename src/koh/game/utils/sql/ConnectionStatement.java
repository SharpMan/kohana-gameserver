package koh.game.utils.sql;

import java.sql.Connection;
import java.sql.Statement;

public class ConnectionStatement<T extends Statement> implements AutoCloseable {

    private final Connection connection;

    private final T statement;

    public ConnectionStatement(Connection connection, T statement) {
        this.connection = connection;
        this.statement = statement;
    }

    public T getStatement() {
        return statement;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        try {
            if(statement != null)
                statement.close();
        }finally {
            if(connection != null)
                connection.close();
        }
    }
}
