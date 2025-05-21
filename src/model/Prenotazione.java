package model;

public class Prenotazione {
    private String pnr;
    private String num_biglietto;
    private Stato_prenotazione stato;
    private boolean assicurazione;
    private boolean bagaglio;
    protected Passeggero passeggero;
    private String codiceVolo;

    public Prenotazione() {
        pnr = "AA000";
        num_biglietto = "BB000";
        stato = Stato_prenotazione.confermata;
        assicurazione = false;
        bagaglio = false;
        passeggero = new Passeggero();
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getNum_biglietto() {
        return num_biglietto;
    }

    public void setNum_biglietto(String num_biglietto) {
        this.num_biglietto = num_biglietto;
    }

    public Stato_prenotazione getStato() {
        return stato;
    }

    public void setStato(Stato_prenotazione stato) {
        this.stato = stato;
    }

    public boolean isAssicurazione() {
        return assicurazione;
    }

    public void updateAssicurazione() {
        this.assicurazione = !assicurazione;
    }

    public boolean isBagaglio() {
        return bagaglio;
    }

    public void updateBagaglio() {
        this.bagaglio = !bagaglio;
    }

    public Passeggero getPasseggero() {
        return passeggero;
    }

    public void setPasseggero(Passeggero passeggero) {
        this.passeggero = passeggero;
    }

    public String getCodiceVolo() {
        return codiceVolo;
    }

    public void setCodiceVolo(String codiceVolo) {
        this.codiceVolo = codiceVolo;
    }

}
