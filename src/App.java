import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class App {
    interface DbWorker {
        void doWork() throws SQLException;
    }

    private static App __instance = null;
    private static Database __db = null;
    private HashMap<Option, DbWorker> __dbMethods;

    enum Option {
        UNKNOWN,
        EXIT,
        ADD_COLONO,
        CHANGE_COLONO_TEAM,
        CANCEL_ACTIVITY,
        REMOVE_MONITOR,
        CHANGE_TEAM_MONITOR,
        LIST_ACTIVITIES_PM,
        LIST_ACTIVITIES_GN,
        LIST_NAME_EE,
        LIST_NAME_ADDRESS_EE,
        LIST_ACTIVITY_PERIOD,
        CHANGE_CHECK
    }

    ;

    private App() {
        __db = new Database("jdbc:sqlserver://10.62.73.87:1433;user=L3NG_1;password=L3NG_1;databaseName=L3NG_1");
        __dbMethods = new HashMap<Option, DbWorker>();

        __dbMethods.put(Option.ADD_COLONO, this::addColono);
        __dbMethods.put(Option.CHANGE_COLONO_TEAM, this::changeColonoTeam);
        __dbMethods.put(Option.CANCEL_ACTIVITY, this::cancelActivity);
        __dbMethods.put(Option.REMOVE_MONITOR, this::removeMonitor);
        __dbMethods.put(Option.CHANGE_TEAM_MONITOR, this::changeTeamMonitor);
        __dbMethods.put(Option.LIST_ACTIVITIES_PM, this::listActivityPM);
        __dbMethods.put(Option.LIST_ACTIVITIES_GN, this::listActivityGN);
        __dbMethods.put(Option.LIST_NAME_EE, this::listNameEE);
        __dbMethods.put(Option.LIST_NAME_ADDRESS_EE, this::listNameAdressEE);
        __dbMethods.put(Option.LIST_ACTIVITY_PERIOD, this::listActivityPeriod);
        __dbMethods.put(Option.CHANGE_CHECK, this::changeCheck);

    }

    private void cancelActivity() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nNumero da actividade: ");
        int num_actividade = s.nextInt();
        s.nextLine();

        try {
            if(__db.cancelActivity(num_actividade))
                System.out.println("Actividade cancelada com sucesso.");
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }

    private void removeMonitor() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nNumero do monitor: ");
        int num_monitor = s.nextInt();
        s.nextLine();

        try {
            if(__db.removeMonitor(num_monitor))
                System.out.println("Monitor removido com sucesso.");
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }

    private void changeTeamMonitor() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nNumero do monitor: ");
        int num_monitor = s.nextInt();
        s.nextLine();

        System.out.print("\nNumero da equipa: ");
        int num_team = s.nextInt();
        s.nextLine();

        try {
            if(__db.changeTeamMonitor(num_team, num_monitor))
                System.out.println("Monitor de equipa mudado com sucesso.");
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }

    private void listActivityPM() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nObrigatoriedade (opcional|obrigatorio): ");
        String participation = s.nextLine();
        if(!(participation.equals("opcional") || participation.equals("obrigatorio"))){
            System.out.println("Opcao invalida. (opcional|obrigatorio)");
            return;
        }
        if (participation.equals("obrigatorio")){
            participation = "obrigatório";
        }

        System.out.print("\nNumero de participantes: ");
        int participants = s.nextInt();
        s.nextLine();

        try {
            ArrayList<Activity> activities = __db.getActivities(participation, participants);
            for (Activity act: activities) {
                System.out.printf("\n%d | %s | %s | %d | %s", act.referencia, act.designacao, act.descricao, act.duracao, act.participacao);
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }

    private void listActivityGN() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nNome do grupo (sem acentos): ");
        String grupo_nome = s.nextLine();
        try {
            ArrayList<Activity> activities = __db.getActivities(grupo_nome);
            for (Activity act: activities) {
                System.out.printf("\n%d | %s | %s | %d | %s", act.referencia, act.designacao, act.descricao, act.duracao, act.participacao);
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }

    private void listNameEE() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nNumero de colonos: ");
        int numero_colonos = s.nextInt();
        s.nextLine();
        try {
            ArrayList<Pessoa> Pessoas = __db.getEEducaocao(numero_colonos);
            for (Pessoa pessoa: Pessoas) {
                System.out.printf("\n%s", pessoa.nome);
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }

    private void listNameAdressEE() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nNumero de colonos: ");
        int numero_colonos = s.nextInt();
        s.nextLine();
        try {
            ArrayList<Pessoa> Pessoas = __db.getEEducaocao(numero_colonos);
            for (Pessoa pessoa: Pessoas) {
                System.out.printf("\nNome: %s | Morada: %s ", pessoa.nome, pessoa.endereco);
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }

    private void listActivityPeriod() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nHora inicial (HH:MM:SS): ");
        String horainicial = s.nextLine();

        System.out.print("\nHora final (HH:MM:SS): ");
        String horafinal = s.nextLine();
        try {
            ArrayList<Activity> activities = __db.getActivities(horainicial, horafinal);
            for (Activity act: activities) {
                System.out.printf("\n%d | %s | %s | %d | %s | %s | %s", act.referencia, act.designacao, act.descricao, act.duracao, act.participacao);
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }

    private void changeCheck() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nNova duracao: ");
        int new_duration = s.nextInt();
        s.nextLine();
        try {
            if(__db.alterActivityRestriction(new_duration));
                System.out.println("Nova duracao mudada com sucesso.");
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }
    //Trocar equipa do colono
    private void changeColonoTeam() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nNumero colono: ");
        int num_colono = s.nextInt();
        s.nextLine();

        System.out.print("\nNumero equipa: ");
        int num_equipa = s.nextInt();
        s.nextLine();
        try {
            if(__db.changeEquipa(num_colono, num_equipa))
                System.out.println("Colono mudado de equipa com sucesso.");
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }
    //Regista todos os valores inseridos pelo utilizador para criar um objecto do tipo Colono
    private void addColono() {
        Scanner s = new Scanner(System.in);
        System.out.print("\nNumero: ");
        int numero = s.nextInt();
        s.nextLine();

        System.out.print("\nNome: ");
        String nome = s.nextLine();

        System.out.print("\nData de nascimento (dd-mm-aaaa): ");
        String dtnascimento = s.nextLine();
        if (!dtnascimento.matches("\\d{2}-\\d{2}-\\d{4}")){
            System.out.println("Formato da data nascimento invalido");
            return;
        }
        int day = Integer.parseInt(dtnascimento.substring(0,2));
        int month = Integer.parseInt(dtnascimento.substring(3,5));
        int year = Integer.parseInt(dtnascimento.substring(6,10));

        System.out.print("\nContacto (+351xxxxxxxxx): ");
        String contacto = s.nextLine();
        if (!contacto.matches("\\+351\\d{9}")){
            System.out.println("\nFormato da data nascimento invalido");
            return;
        }
        System.out.print("\nEscolaridade: ");
        int escolaridade = s.nextInt();
        s.nextLine();

        System.out.print("\nCartao de cidadao: ");
        String ccidadao = s.nextLine();

        System.out.print("\nCartao de utente (xxxxxxxxx): ");
        int cutente = s.nextInt();
        s.nextLine();

        System.out.print("\nNumero de Encarregado de Educacao: ");
        int eeducacao = s.nextInt();
        s.nextLine();

        System.out.print("\nEquipa: ");
        int equipa = s.nextInt();
        s.nextLine();
        //Cria objecto com valores introduzidos
         Colono colono = new Colono(
                numero,
                nome,
                LocalDate.of(year,month, day),
                contacto,
                escolaridade,
                ccidadao,
                cutente,
                eeducacao,
                equipa
        );
        try {
            if(__db.addColono(colono))
                System.out.println("Colono adicionado com sucesso.");
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }

    public static App getInstance() {
        if (__instance == null) {
            __instance = new App();
        }
        return __instance;
    }

    public void Run() throws Exception {
        try {
            __db.open_connection();
            Option userInput = Option.UNKNOWN;
            clearConsole();
            do {
                userInput = displayMenu();
                clearConsole();
                try {
                    __dbMethods.get(userInput).doWork();
                    System.in.read();
                } catch (NullPointerException ex) {
                    //Nothing to do. The option was not a valid one. Read another.
                }

            } while (userInput != Option.EXIT);
        }finally {
            __db.close_connection();
        }
    }

    private final static void clearConsole() throws Exception {
        for (int y = 0; y < 25; y++) //console is 80 columns and 25 lines
            System.out.println("\n");
    }


    private static Option displayMenu() {
        Option option = Option.UNKNOWN;
        try {
            System.out.println();
            System.out.println("1. Exit.");
            System.out.println("2. Adicionar Colono.");
            System.out.println("3. Mudar equipa do Colono.");
            System.out.println("4. Cancelar Actividade.");
            System.out.println("5. Remover Monitor.");
            System.out.println("6. Mudar Monitor da Equipa.");
            System.out.println("7. Lista activdades pela obrigatoriedade e numero de participantes");
            System.out.println("8. Lista actividade pelo nome do grupo.");
            System.out.println("9. Lista nomes de Encarregado de Educacao com mais de X colonos.");
            System.out.println("10. Lista nomes e moradas de Encarregado de Educacao com mais de X colonos.");
            System.out.println("11. Lista de actividades que nao ocorreram num determinado periodo");
            System.out.println("12. Mudar o numero maximo de horas por actividade.");
            System.out.print(">");
            Scanner s = new Scanner(System.in);
            int result = s.nextInt();
            option = Option.values()[result];
        } catch (RuntimeException ex) {
            //nothing to do.
        }

        return option;

    }

    public static void main(String[] args) throws SQLException, Exception {
        App.getInstance().Run();
    }
}
