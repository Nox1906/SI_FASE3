import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Colono {

    final public int numero, escolaridade, eeducacao, equipa, cutente, idade;
    final public String nome, contacto, ccidadao;
    final public LocalDate dtnascimento;

    public Colono(int numero, String nome, LocalDate dtnascimento, String contacto, int escolaridade, String ccidadao, int cutente, int eeducacao, int equipa){
        this.numero = numero;
        this.nome = nome;
        this.dtnascimento = dtnascimento;
        this.idade = (int)ChronoUnit.YEARS.between(dtnascimento, LocalDate.now());
        this.contacto = contacto;
        this.escolaridade = escolaridade;
        this.ccidadao = ccidadao;
        this.cutente = cutente;
        this.eeducacao = eeducacao;
        this.equipa = equipa;
    }
}
