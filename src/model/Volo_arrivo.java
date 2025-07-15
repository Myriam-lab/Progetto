package model;

/**
 * Rappresenta un volo in arrivo, specializzazione della classe Volo.
 */
public class Volo_arrivo extends Volo{

    /**
     * Costruttore per un Volo_arrivo.
     * Imposta la destinazione a "Napoli" e definisce origine e compagnia.
     * @param origine l'origine del volo.
     * @param compagnia la compagnia aerea.
     */
    public Volo_arrivo(String origine, String compagnia) {
        destinazione = "Napoli";
        this.origine = origine;
        this.compagniaAerea = compagnia;
    }


}
