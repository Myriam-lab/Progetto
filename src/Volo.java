import java.sql.Time;
import java.util.Date;

public class Volo {
    protected int codice;
    protected String compagniaAerea;
    protected Date data;
    //protected Time orarioPrevisto; DA VEDERE COME FUNZIONA IL TIME
    protected int ritardo;
    protected String origine;
    protected String destinazione;
    protected Stato_del_volo stato;

    public Volo() {
        codice = 7890;
        compagniaAerea = "";
        data = new Date();
        //orarioPrevisto=new Time();
        ritardo = 0;
        stato=Stato_del_volo.in_orario;
    }

    public int getCodice() {
        return codice;
    }

    public void setCodice(int codice) {
        this.codice = codice;
    }

    public void setStato(Stato_del_volo stato) {
        this.stato = stato;
    }

    public Stato_del_volo getStato() {
        return stato;
    }

    public String getCompagniaAerea() {
        return compagniaAerea;
    }
    public void setCompagniaAerea(String compagniaAerea) {
        this.compagniaAerea = compagniaAerea;
    }

    /*public Date getData() {
        return data;
    }*/

    /*public void setData(Date data) {
        this.data = data;
    }*/

    /*public Time getOrarioPrevisto() {
        return orarioPrevisto;
    }
    public void setOrarioPrevisto(Time orarioPrevisto) {
        this.orarioPrevisto = orarioPrevisto;
    }*/

    public int getRitardo() {
        return ritardo;
    }

    public void setRitardo(int ritardo) {
        this.ritardo = ritardo;
    }

}