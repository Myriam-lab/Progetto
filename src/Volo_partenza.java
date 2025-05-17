public class Volo_partenza extends Volo{
    Gate gate;

    public Volo_partenza(String destinazione, String compagnia) {
        origine = " Napoli";
        gate=new Gate();
        this.destinazione=destinazione;
        this.compagniaAerea = compagnia;
    }
    public void setDestinazione(String destinazione) {
        this.destinazione = destinazione;
    }
    public String getDestinazione() {
        return destinazione;
    }


}

