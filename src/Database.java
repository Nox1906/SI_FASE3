import java.sql.DriverManager;
import java.sql.*;

public class Database {
    private String connectionString;
    private Connection connection = null;

    public Database(String connectionString){
        this.connectionString = connectionString;
    }

    private void ListColono() {
    }

    private void open_connection() throws SQLException {
        if (connection == null)
            connection = DriverManager.getConnection(getConnectionString());
    }

    private void close_connection() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public String getConnectionString(){
        return connectionString;
    }

    public void setConnectionString(String s){
        connectionString = s;
    }

    public void addColono(Colono colono){
        try {
            open_connection();
            // TODO Implement adding a Colono

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            try {
                close_connection();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void changeTeams(int num_colono, int num_team){
        try {
            open_connection();
            // TODO Implement changing team

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            try {
                close_connection();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void cancelActivity(int num_activity){
        try {
            open_connection();
            // TODO Implement cancel activity

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            try {
                close_connection();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void removeMonitor(int num_monitor){
        try {
            open_connection();
            // TODO Implement changing team

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            try {
                close_connection();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void changeTeamMonitor(int num_team, int num_monitor){
        try {
            open_connection();
            // TODO Implement changing team

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            try {
                close_connection();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }



}
