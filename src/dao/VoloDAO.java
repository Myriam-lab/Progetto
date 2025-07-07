package dao;

import model.Volo;
import java.util.List;

public interface VoloDAO {
    List<Volo> getVoliInArrivo();
    List<Volo> getVoliInPartenza();
    Volo findVoloByCodice(String codiceVolo);
    boolean creaNuovoVolo(Volo volo);
}