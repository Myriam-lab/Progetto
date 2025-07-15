package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Classe base che rappresenta un volo, con le sue informazioni essenziali.
 * Contiene i dati comuni sia ai voli in arrivo che a quelli in partenza.
 */
public class Volo {
    protected String codice;
    protected String compagniaAerea;
    protected LocalDate data;
    protected LocalTime orarioPrevisto;
    protected int ritardo;
    protected String origine;
    protected String destinazione;
    protected Stato_del_volo stato;

    /**
     * Costruttore di default per un Volo.
     * Inizializza i campi con valori predefiniti.
     */
    public Volo() {
        codice = "AA000";
        compagniaAerea = "";
        data = null;
        orarioPrevisto = null;
        ritardo = 0;
        stato=Stato_del_volo.in_orario;
    }

    /**
     * Restituisce il codice del volo.
     * @return il codice del volo.
     */
    public String getCodice() {
        return codice;
    }

    /**
     * Imposta il codice del volo.
     * @param codice il nuovo codice del volo.
     */
    public void setCodice(String codice) {
        this.codice = codice;
    }

    /**
     * Imposta lo stato del volo.
     * @param stato il nuovo stato del volo.
     */
    public void setStato(Stato_del_volo stato) {
        this.stato = stato;
    }

    /**
     * Restituisce lo stato attuale del volo.
     * @return lo stato del volo.
     */
    public Stato_del_volo getStato() {
        return stato;
    }

    /**
     * Restituisce la compagnia aerea.
     * @return la compagnia aerea.
     */
    public String getCompagniaAerea() {
        return compagniaAerea;
    }

    /**
     * Imposta la compagnia aerea del volo.
     * @param compagniaAerea la nuova compagnia aerea.
     */
    public void setCompagniaAerea(String compagniaAerea) {
        this.compagniaAerea = compagniaAerea;
    }

    /**
     * Restituisce la data del volo.
     * @return la data del volo.
     */
    public LocalDate getData() {
        return data;
    }

    /**
     * Imposta la data del volo.
     * @param data la nuova data del volo.
     */
    public void setData(LocalDate data) {
        this.data = data;
    }

    /**
     * Restituisce l'orario previsto, tenendo conto dell'eventuale ritardo.
     * @return l'orario previsto aggiornato.
     */
    public LocalTime getOrarioPrevisto() {
        return orarioPrevisto.plusMinutes(ritardo);
    }

    /**
     * Imposta l'orario previsto del volo.
     * @param orarioPrevisto il nuovo orario previsto.
     */
    public void setOrarioPrevisto(LocalTime orarioPrevisto) {
        this.orarioPrevisto = orarioPrevisto;
    }

    /**
     * Restituisce i minuti di ritardo del volo.
     * @return i minuti di ritardo.
     */
    public int getRitardo() {
        return ritardo;
    }

    /**
     * Imposta il ritardo del volo in minuti.
     * @param ritardo il nuovo ritardo in minuti.
     */
    public void setRitardo(int ritardo) {
        this.ritardo = ritardo;
    }

    /**
     * Imposta l'origine del volo.
     * @param origine la nuova origine del volo.
     */
    public void setOrigine(String origine) {
        this.origine = origine;
    }

    /**
     * Restituisce l'origine del volo.
     * @return l'origine del volo.
     */
    public String getOrigine() {
        return origine;
    }

    /**
     * Imposta la destinazione del volo.
     * @param destinazione la nuova destinazione del volo.
     */
    public void setDestinazione(String destinazione) {
        this.destinazione = destinazione;
    }

    /**
     * Restituisce la destinazione del volo.
     * @return la destinazione del volo.
     */
    public String getDestinazione() {
        return destinazione;
    }
}