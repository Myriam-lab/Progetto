package dao;

import model.Prenotazione;
import java.util.List;

public interface PrenotazioneDAO {
    boolean creaPrenotazione(Prenotazione prenotazione);
    List<Prenotazione> getPrenotazioniPerUtente(String nomeUtente);
    List<Prenotazione> getPrenotazioniFiltrateAdmin(String nomeFilter, String cognomeFilter);
    List<String> getPostiOccupatiPerVolo(String codiceVolo);
}