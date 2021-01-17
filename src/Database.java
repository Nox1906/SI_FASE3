import java.sql.DriverManager;
import java.sql.*;
import java.util.ArrayList;

public class Database {
    //URL de ligação
    private String connectionString;
    //Objeto de ligação à BD
    private Connection connection = null;

    //Mensagens de erro
    private enum FRIENDLY_MESSAGES {
        TEAM_FULL("Equipa cheia."),
        AGE_RESTRICTED("Fora da idade do grupo."),
        FAILED_TO_ADD_COLONO("Erro a adicionar o colono"),
        FAILED_TO_CHANGE_TEAMS("Erro a mudar a equipa."),
        FAILED_TO_CANCEL_ACTIVITY("Erro a cancelar actividade."),
        FAILED_TO_REMOVE_MONITOR("Erro a remover o monitor."),
        FAILED_TO_CHANGE_TEAM_MONITOR("Erro a mudar o monitor da equipa."),
        FAILED_TO_RETRIEVE_ACTIVITIES("Erro a buscar as atividades."),
        TEAM_NOT_FOUND("Equipa  não encontrada."),
        NEW_TEAM_NOT_FOUND("Equipa nova não encontrada."),
        OLD_TEAM_NOT_FOUND("Equipa antiga não encontrada."),
        COLONO_NOT_FOUND("Colono não encontrado."),
        FAILED_ALTER_RESTRICTION("Erro a encontrar a restriçao."),
        FAILED_MODIFY_ALTER_RESTRICTION("Erro a modificar a restriçao.");
        public final String message;
        FRIENDLY_MESSAGES(String message) {
            this.message = message;
        }
    }

    //Construtor de Database
    public Database(String connectionString){
        this.connectionString = connectionString;
    }

    //Estabelece ligação à fonte de BD
    public void open_connection() throws SQLException {
        if (connection == null)
            connection = DriverManager.getConnection(getConnectionString());
    }
    //Fecha ligação à BD
    public void close_connection() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
    //Devolve URL de ligação à BD
    public String getConnectionString(){
        return connectionString;
    }
    //Atribui URL de ligação à BD
    public void setConnectionString(String s){
        connectionString = s;
    }

    //Verifica se idade do colono cumpre restrição do grupo
    private boolean isColonoInGrupoAge(Colono colono, Grupo grupo){
        return grupo.idademaxima >= colono.idade && colono.idade >= grupo.idademinima;
    }
    //Verifica se numero de elementos cumpre com maximo permitido no grupo
    private boolean isEquipaFull(String grupo, int numero_elementos){
        return switch (grupo.toLowerCase()) {
            case "iniciados" -> numero_elementos >= 6;
            case "juniores" -> numero_elementos >= 8;
            case "seniores" -> numero_elementos >= 10;
            default -> true;
        };
    }
    //Cria novo Colono a partir de ResultSet recebido como parametro
    private Colono createColonoWithResultSet(ResultSet resultSet) throws SQLException {
        return new Colono(
                resultSet.getInt("numero"),
                resultSet.getString("nome"),
                resultSet.getDate("dtnascimento").toLocalDate(),
                resultSet.getString("contacto"),
                resultSet.getInt("escolaridade"),
                resultSet.getString("ccidadao"),
                resultSet.getInt("cutente"),
                resultSet.getInt("eeducacao"),
                resultSet.getInt("equipa")
        );
    }
    //Cria nova Pessoa a partir de ResultSet recebido como parametro
    private Pessoa createPessoaWithResultSet(ResultSet resultSet) throws SQLException {
        return new Pessoa(
                resultSet.getInt("numero"),
                resultSet.getString("nome"),
                resultSet.getString("endereço"),
                resultSet.getString("ntelefone"),
                resultSet.getString("email")
        );
    }
    //Cria nova actividade a partir de ResultSet recebido como parametro
    private Activity createActivityWithResultSet(ResultSet resultSet) throws SQLException{
        return new Activity(
                resultSet.getInt("referencia"),
                resultSet.getInt("duracao"),
                resultSet.getString("designacao"),
                resultSet.getString("descricao"),
                resultSet.getString("participacao"));
    }
    //Adiciona novo colono
    public boolean addColono(Colono colono) throws SQLException{
        try {
            open_connection();
            connection.setAutoCommit(false);
            //Numero de colonos por equipa e limite de idades do respetivo grupo a que pertence
            String query = "SELECT EQUIPA.grupo, COUNT(equipa) AS Count_Colonos, GRUPO.idademinima, GRUPO.idademaxima FROM EQUIPA" +
                           "INNER JOIN GRUPO on EQUIPA.grupo = GRUPO.nome " +
                           "LEFT JOIN COLONO on EQUIPA.numero = COLONO.equipa " +
                           "WHERE EQUIPA.numero = ? " +
                           "GROUP BY COLONO.equipa, EQUIPA.grupo, GRUPO.idademinima, GRUPO.idademaxima";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, colono.equipa);
            ResultSet result = preparedStatement.executeQuery();
            //valida se a equipa existe
            if(!result.next()){
                System.out.println(FRIENDLY_MESSAGES.TEAM_NOT_FOUND.message);
                connection.rollback();
                return false;
            }
            //valida se a equipa está cheia
            if (isEquipaFull(
                    result.getString("grupo"),
                    result.getInt("Count_Colonos"))) {
                System.out.println(FRIENDLY_MESSAGES.TEAM_FULL.message);
                connection.rollback();
                return false;
            }
            //valida se a idade do colono respeita a restição do grupo
            if(!isColonoInGrupoAge(colono, new Grupo(
                                result.getString("grupo"),
                                result.getInt("idademinima"),
                                result.getInt("idademaxima")))){
                System.out.println(FRIENDLY_MESSAGES.AGE_RESTRICTED.message);
                connection.rollback();
                return false;
            }
            //Insere novo colono
            query = "INSERT INTO COLONO VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, colono.numero);
            preparedStatement.setString(2, colono.nome);
            preparedStatement.setString(3, colono.dtnascimento.toString());
            preparedStatement.setString(4, colono.contacto);
            preparedStatement.setInt(5, colono.escolaridade);
            preparedStatement.setString(6, colono.ccidadao);
            preparedStatement.setInt(7, colono.cutente);
            preparedStatement.setInt(8, colono.eeducacao);
            preparedStatement.setInt(9, colono.equipa);
            if (preparedStatement.executeUpdate() == 0){
                System.out.println(FRIENDLY_MESSAGES.FAILED_TO_ADD_COLONO.message);
                connection.rollback();
                return false;
            }
            connection.commit(); //confirma transação
            return true;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_TO_ADD_COLONO.message);
            connection.rollback(); //reverte modificações
        }
        return false;
    }
    //Troca um colono de equipa
    public boolean changeEquipa(int num_colono, int num_equipa) throws SQLException {
        try {
            open_connection();
            connection.setAutoCommit(false);
            //Numero de colonos por equipa e limite de idades do respetivo grupo a que pertence
            String query = "SELECT EQUIPA.grupo, COUNT(equipa) AS Count_Colonos, GRUPO.idademinima, GRUPO.idademaxima FROM EQUIPA " +
                           "INNER JOIN GRUPO on EQUIPA.grupo = GRUPO.nome " +
                           "LEFT JOIN COLONO on EQUIPA.numero = COLONO.equipa " +
                           "WHERE EQUIPA.numero = ? " +
                           "GROUP BY COLONO.equipa, EQUIPA.grupo, GRUPO.idademinima, GRUPO.idademaxima";
            PreparedStatement prepareStatement = connection.prepareStatement(query);
            prepareStatement.setInt(1, num_equipa);
            ResultSet result = prepareStatement.executeQuery();
            //valida se a nova equipa existe
            if(!result.next()){
                System.out.println(FRIENDLY_MESSAGES.NEW_TEAM_NOT_FOUND.message);
                connection.rollback();
                return false;
            }
            //valida se a nova equipa está cheia
            if (isEquipaFull(
                    result.getString("grupo"),
                    result.getInt("Count_Colonos"))) {
                System.out.println(FRIENDLY_MESSAGES.TEAM_FULL.message);
                connection.rollback();
                return false;
            }
            // Informação de colono
            query = "SELECT * FROM COLONO WHERE numero = ?";
            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setInt(1, num_colono);
            ResultSet res = prepareStatement.executeQuery();
            //valida se o colono existe
            if(!res.next()){
                System.out.println(FRIENDLY_MESSAGES.COLONO_NOT_FOUND.message);
                connection.rollback();
                return false;
            }
            Colono colono = createColonoWithResultSet(res);
            //valida se a idade do colono respeita a restrição
            if(!isColonoInGrupoAge(colono, new Grupo(
                    result.getString("grupo"),
                    result.getInt("idademinima"),
                    result.getInt("idademaxima")))){
                System.out.println(FRIENDLY_MESSAGES.AGE_RESTRICTED.message);
                connection.rollback();
                return false;
            }
            //Conta número de colonos da equipa atual
            query = "SELECT COUNT(equipa) AS Count_Colonos FROM COLONO WHERE equipa = ? GROUP BY equipa";
            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setInt(1, colono.equipa);
            res = prepareStatement.executeQuery();
            //Valida se a equipa do colono existe
            if(!res.next()){
                System.out.println(FRIENDLY_MESSAGES.OLD_TEAM_NOT_FOUND.message);
                connection.rollback();
                return false;
            }
            //Cancela todas as atividades se equipa atual do colono ficar sem elementos
            if (res.getInt("Count_Colonos") == 1){
                query = "SELECT referencia FROM ACTIVIDADE_EQUIPA WHERE equipa = ?";
                prepareStatement = connection.prepareStatement(query);
                prepareStatement.setInt(1, colono.equipa);
                res = prepareStatement.executeQuery();
                while(res.next()){
                    cancelActivity(res.getInt("referencia"));
                }
            }
            //Actualiza equipa do colono
            query = "UPDATE COLONO SET equipa = ? WHERE numero = ?";
            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setInt(1, num_equipa);
            prepareStatement.setInt(2, colono.numero);
            if (prepareStatement.executeUpdate() == 0){
                System.out.println(FRIENDLY_MESSAGES.FAILED_TO_CHANGE_TEAMS.message);
                connection.rollback();
                return false;
            }
            prepareStatement.close();
            connection.commit();
            return true;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_TO_CHANGE_TEAMS.message);
            connection.rollback();
        }
        return false;
    }
    //Cancela uma actividade atraves da sua referencia percorrendo todas as relações por ordem de dependência
    public boolean cancelActivity(int num_activity) throws SQLException{
        try {
            open_connection();
            connection.setAutoCommit(false);
            //Elimina actividade de ACTIVIDADE_MONITOR
            String query = "DELETE FROM ACTIVIDADE_MONITOR WHERE referencia = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, num_activity);
            preparedStatement.executeUpdate();
            //Elimina actividade de ACTIVIDADE_EQUIPA
            query = "DELETE FROM ACTIVIDADE_EQUIPA WHERE referencia = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, num_activity);
            preparedStatement.executeUpdate();
            //Elimina actividade de ACTIVIDADEDESPORTIVA
            query = "DELETE FROM ACTIVIDADEDESPORTIVA WHERE referencia = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, num_activity);
            preparedStatement.executeUpdate();

            connection.commit();
            return true;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_TO_CANCEL_ACTIVITY.message);
            connection.rollback();
        }
        return false;
    }
    //Remove um monitor
    public boolean removeMonitor(int num_monitor) throws SQLException {
        try {
            open_connection();
            connection.setAutoCommit(false);
            // Actualiza associação do monitor como co-monitor
            String query = "UPDATE MONITOR SET comonitor = null WHERE comonitor = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, num_monitor);
            preparedStatement.executeUpdate();

            // Remove monitor das respetivas actividades
            query = "DELETE FROM ACTIVIDADE_MONITOR WHERE monitor = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, num_monitor);
            preparedStatement.executeUpdate();

            // Remove monitor das respetivas equipas
            query = "UPDATE EQUIPA SET monitor = null WHERE monitor = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, num_monitor);
            preparedStatement.executeUpdate();

            // Remove monitor da tabela MONITOR
            query = "DELETE FROM MONITOR WHERE numero = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, num_monitor);
            preparedStatement.executeUpdate();

            connection.commit();
            return true;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_TO_REMOVE_MONITOR.message);
            connection.rollback();
        }
        return false;
    }
    //Troca um monitor de equipa
    public boolean changeTeamMonitor(int num_team, int num_monitor) throws SQLException{
        try {
            open_connection();
            connection.setAutoCommit(false);
            //Actualiza monitor de uma equipa para null
            String query = "UPDATE EQUIPA SET monitor = null WHERE monitor = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, num_monitor);
            preparedStatement.executeUpdate();
            //Actualiza monitor de uma equipa
            query = "UPDATE EQUIPA SET monitor = ? WHERE numero = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, num_monitor);
            preparedStatement.setInt(2, num_team);
            preparedStatement.executeUpdate();
            connection.commit();
            return true;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_TO_CHANGE_TEAM_MONITOR.message);
            connection.rollback();
        }
        return false;
    }
    //Devolve lista de actividades consoante nr de participantes e tipo de participatição
    public ArrayList<Activity> getActivities(String participation, int participants) throws SQLException{
        //Array list com resultados
        ArrayList<Activity> result = new ArrayList<>();
        try {
            open_connection();
            connection.setAutoCommit(false);
            String query = "SELECT DISTINCT * " +
                    "FROM ACTIVIDADE " +
                    "INNER JOIN ACTIVIDADEDESPORTIVA on ACTIVIDADE.referencia = ACTIVIDADEDESPORTIVA.referencia " +
                    "WHERE ACTIVIDADE.participacao LIKE ? and ACTIVIDADEDESPORTIVA.participantes >= ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, participation);
            preparedStatement.setInt(2, participants);
            ResultSet res = preparedStatement.executeQuery();
            //Adiciona ao espaço de resultados os dados que respeitam os parametros passados
            while (res.next()){
                result.add(createActivityWithResultSet(res));
            }
            connection.commit();
            return result;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_TO_RETRIEVE_ACTIVITIES.message);
            connection.rollback();
        }
        return result;
    }

    //Devolve lista de todas as actividades de determinado grupo
    public ArrayList<Activity> getActivities(String grupo_nome) throws SQLException{
        //Array list com resultados
        ArrayList<Activity> result = new ArrayList<>();
        try {
            open_connection();
            connection.setAutoCommit(false);
            String query = "SELECT DISTINCT * " +
                           "FROM ACTIVIDADE " +
                           "INNER JOIN ACTIVIDADE_EQUIPA ON ACTIVIDADE_EQUIPA.referencia = ACTIVIDADE.referencia " +
                           "INNER JOIN EQUIPA ON EQUIPA.numero = ACTIVIDADE_EQUIPA.equipa " +
                           "WHERE EQUIPA.grupo LIKE ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, grupo_nome);
            ResultSet res = preparedStatement.executeQuery();
            //Adiciona ao espaço de resultados os dados que respeitam os parametros passados
            while (res.next()){
                result.add(createActivityWithResultSet(res));
            }
            connection.commit();
            return result;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_TO_RETRIEVE_ACTIVITIES.message);
            connection.rollback();
        }
        return result;
    }

    public ArrayList<Colono> getRepresentante(int numberOfColono) throws SQLException{
        ArrayList<Colono> result = new ArrayList<>();
        try {
            open_connection();
            connection.setAutoCommit(false);
            String query = "SELECT * " +
                    "FROM COLONO " +
                    "WHERE numero IN (SELECT colono " +
                    "FROM REPRESENTANTE " +
                    "INNER JOIN COLONO ON COLONO.equipa = REPRESENTANTE.equipa " +
                    "GROUP by REPRESENTANTE.equipa, REPRESENTANTE.colono " +
                    "HAVING COUNT(COLONO.equipa) > ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, numberOfColono);
            ResultSet res = preparedStatement.executeQuery();
            //Adiciona ao espaço de resultados os dados que respeitam os parametros passados
            while (res.next()){
                result.add(createColonoWithResultSet(res));
            }
            connection.commit();
            return result;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_TO_RETRIEVE_ACTIVITIES.message);
            connection.rollback();
        }
        return result;

    }

    //Devolve lista de encarregados de educação com mais do que determinado um determinado numero de dependentes
    public ArrayList<Pessoa> getEEducaocao(int numberOfColono) throws SQLException{
        //Array list com resultados
        ArrayList<Pessoa> result = new ArrayList<>();
        try {
            open_connection();
            connection.setAutoCommit(false);
            String query = "SELECT * " +
                           "FROM PESSOA " +
                           "INNER JOIN (SELECT eeducacao, COUNT (eeducacao) AS Count_eeducacao " +
                           "FROM COLONO " +
                           "GROUP BY eeducacao " +
                           "HAVING COUNT (eeducacao) > ?) ED ON ED.eeducacao = PESSOA.numero " +
                           "order by 1";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, numberOfColono);
            ResultSet res = preparedStatement.executeQuery();
            //Adiciona ao espaço de resultados os dados que respeitam os parametros passados
            while (res.next()){
                result.add(createPessoaWithResultSet(res));
            }
            connection.commit();
            return result;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_TO_RETRIEVE_ACTIVITIES.message);
            connection.rollback();
        }
        return result;
    }



    //Devolve lista de todas as actividades que não se realizaram dentro dum determinado periodo
    public ArrayList<Activity> getActivities(String horainicial, String horafinal) throws SQLException{
        //Array list com resultados
        ArrayList<Activity> result = new ArrayList<>();
        try {
            open_connection();
            connection.setAutoCommit(false);
            String query = "SELECT * " +
                           "FROM ACTIVIDADE " +
                           "INNER JOIN ACTIVIDADE_EQUIPA ON ACTIVIDADE_EQUIPA.referencia = ACTIVIDADE.referencia " +
                           "WHERE horainicial > CAST(? AS TIME) OR horafinal < CAST(? AS TIME)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, horainicial);
            preparedStatement.setString(2, horafinal);
            ResultSet res = preparedStatement.executeQuery();
            //Adiciona ao espaço de resultados os dados que respeitam os parametros passados
            while (res.next()){
                result.add(createActivityWithResultSet(res));
            }
            connection.commit();
            return result;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_TO_RETRIEVE_ACTIVITIES.message);
            connection.rollback();
        }
        return result;
    }

    //Altera restrição do atributo duração da tabela ACTIVIDADE
    public boolean alterActivityRestriction(int new_duration) throws SQLException {
        try {
            open_connection();
            connection.setAutoCommit(false);
            //Nome da restrição do atributo duração
            String query = "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                           "WHERE TABLE_NAME='ACTIVIDADE' AND CONSTRAINT_NAME LIKE '%durac%'";
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);
            if(!result.next()){
                System.out.println(FRIENDLY_MESSAGES.FAILED_ALTER_RESTRICTION.message);
                connection.rollback();
                return false;
            }
            String restriction_name = result.getString("CONSTRAINT_NAME");
            //Elimina restrição
            query = String.format("ALTER TABLE ACTIVIDADE DROP CONSTRAINT %s", restriction_name);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
            //Adiciona restrição com novo valor
            query = "ALTER TABLE ACTIVIDADE ADD CHECK (duracao >= 0 AND duracao <= " + new_duration + ")" ;
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
            connection.commit();
            return true;
        } catch (SQLException throwables) {
            System.out.println(FRIENDLY_MESSAGES.FAILED_MODIFY_ALTER_RESTRICTION.message);
            connection.rollback();
        }
        return false;
    }

}




