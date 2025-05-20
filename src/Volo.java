import java.time.LocalDate;
import java.time.LocalTime;

public class Volo {
    protected String codice;
    protected String compagniaAerea;
    protected LocalDate data;
    protected LocalTime orarioPrevisto;
    protected int ritardo;
    protected String origine;
    protected String destinazione;
    protected Stato_del_volo stato;

    public Volo() {
        codice = "AA000";
        compagniaAerea = "";
        data = null;
        orarioPrevisto = null;
        ritardo = 0;
        stato=Stato_del_volo.in_orario;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
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

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getOrarioPrevisto() {
        return orarioPrevisto.plusMinutes(ritardo);
    }

    public void setOrarioPrevisto(LocalTime orarioPrevisto) {
        this.orarioPrevisto = orarioPrevisto;
    }

    public int getRitardo() {
        return ritardo;
    }

    public void setRitardo(int ritardo) {
        this.ritardo = ritardo;
    }
}