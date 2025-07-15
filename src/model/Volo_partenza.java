package model;

/**
 * Rappresenta un volo in partenza, specializzazione della classe Volo.
 * Aggiunge la gestione del gate di imbarco.
 */
public class Volo_partenza extends Volo{
    public Gate gate;

    /**
     * Costruttore per un Volo_partenza.
     * Imposta l'origine a "Napoli", crea un nuovo Gate e definisce destinazione e compagnia.
     * @param destinazione la destinazione del volo.
     * @param compagnia la compagnia aerea.
     */
    public Volo_partenza(String destinazione, String compagnia) {
        origine = " Napoli";
        gate=new Gate();
        this.destinazione=destinazione;
        this.compagniaAerea = compagnia;
    }

}
