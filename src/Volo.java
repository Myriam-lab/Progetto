public class Volo {
    protected int codice;
    protected String origine;
    protected String destinazione;

    public Volo() {
        codice = 7890;
    }

    public int getCodice() {
        return codice;
    }

    public void setCodice(int codice) {
        this.codice = codice;
    }
}