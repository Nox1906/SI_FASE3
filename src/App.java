import java.sql.*;
import java.util.HashMap;
import java.util.Scanner;

public class App {
    interface DbWorker
    {
        void doWork() throws SQLException;
    }
    Connection con=null;
    Statement stmt = null;
    ResultSet rs = null;
    private static App __instance = null;
    private String __connectionString;
    private HashMap<Option,DbWorker> __dbMethods;
    private static final String SELECT_CMD = "select nome L3NG_1.COLONO";
    private enum Option
    {
        Unknown,
        Exit,
        ListStudent,
        ListCourse,
        RegisterStudent,
        EnrolStudent
    }
    private App()
    {
        __dbMethods = new HashMap<Option,DbWorker>();
        __dbMethods.put(Option.ListStudent, new DbWorker() {public void doWork() throws SQLException {ListColono();}});
        //__dbMethods.put(Option.ListCourse, new DbWorker() {public void doWork() {ListCourse();}});
        //__dbMethods.put(Option.RegisterStudent, new DbWorker() {public void doWork() {ARegisterStudent();}});
        //__dbMethods.put(Option.EnrolStudent, new DbWorker() {public void doWork() {EnrolStudent();}});

    }
    public static App getInstance()
    {
        if(__instance == null)
        {
            __instance = new App();
        }
        return __instance;
    }
    private void Login() throws java.sql.SQLException
    {

        con= DriverManager.getConnection(getConnectionString());
        if(con != null)
            con.close();

    }
    private final static void clearConsole() throws Exception
    {
        for (int y = 0; y < 25; y++) //console is 80 columns and 25 lines
            System.out.println("\n");

    }
    public void Run() throws Exception
    {
        Login ();
        Option userInput = Option.Unknown;
        do
        {
            clearConsole();
            userInput = DisplayMenu();
            clearConsole();
            try
            {
                __dbMethods.get(userInput).doWork();
                System.in.read();

            }
            catch(NullPointerException ex)
            {
                //Nothing to do. The option was not a valid one. Read another.
            }

        }while(userInput!=Option.Exit);
    }

    public String getConnectionString()
    {
        return __connectionString;
    }
    public void setConnectionString(String s)
    {
        __connectionString = s;
    }

    private Option DisplayMenu()
    {
        Option option=Option.Unknown;
        try
        {
            System.out.println();
            System.out.println("1. Exit");
            System.out.println("2. List Colonos");
            System.out.print(">");
            Scanner s = new Scanner(System.in);
            int result = s.nextInt();
            option = Option.values()[result];
        }
        catch(RuntimeException ex)
        {
            //nothing to do.
        }

        return option;

    }
    private void ListColono() throws SQLException {

        try
        {

            //executar o comando
            stmt = con.createStatement();
            rs=stmt.executeQuery(SELECT_CMD);

            //iterar no resultado
            while(rs.next())
                System.out.print(rs.getString("nome"));
            System.out.println();
        }
        catch(SQLException sqlex)
        {
            System.out.println("Erro:"+sqlex.getMessage());
        }
        finally
        {
            //libertar os recursos do ResultSet
            if(rs != null)
                rs.close();
            //libertar os recursos do Statement
            if(stmt != null)
                stmt.close();
            //fechar a ligação
            if(con != null)
                con.close();
        }
        System.out.println("ListStudent()");
    }
    public static void main(String[] args) throws SQLException,Exception
    {
        //Source of the JDBC and the SQLServer, here:
        //	http://technet.microsoft.com/en-us/library/ms378988.aspx
        //Source of SQL Server:
        //	- the SQL Browser service must be running: net start SqlBrowser
        //	- The TCP-IP protocol must be connected in SQLServer
        //      - to use integrated security you must specify the following command
        // from the java virtual machine: -Djava.library.path=<directoria para sqljdbc_auth.dll>
        String url = "jdbc:sqlserver://10.62.73.87:1433;user=L3NG_1;password=L3NG_1;databaseName=L3NG_1"; //(1)
        //String url = "jdbc:sqlserver://localhost:1433;user=jdbcuser;password=jdbcuser;databaseName=aula05";//(2)

        App.getInstance().setConnectionString(url);
        App.getInstance().Run();
    }
}
