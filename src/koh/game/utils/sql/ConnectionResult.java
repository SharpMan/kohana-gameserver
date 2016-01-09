package koh.game.utils.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnectionResult extends ConnectionStatement<Statement>{

    private final ResultSet result;

    public ConnectionResult(Connection connection, Statement statement, ResultSet result) {
        super(connection, statement);
        this.result = result;
    }

    public ResultSet getResult() {
        return result;
    }

}
