public class Equipa { //?????????
    public int numero, monitor;
    public String grupo, designacao;
    public int numero_elementos;

    public Equipa(int numero, int monitor, String grupo, String designacao) {
        this.numero = numero;
        this.monitor = monitor;
        this.grupo = grupo;
        this.designacao = designacao;
    }

    public Equipa(int numero, int monitor, String grupo, String designacao, int numero_elementos) {
        this.numero = numero;
        this.monitor = monitor;
        this.grupo = grupo;
        this.designacao = designacao;
        this.numero_elementos = numero_elementos;
    }

}
