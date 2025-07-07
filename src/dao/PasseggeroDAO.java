package dao;

import model.Passeggero;
import java.util.Optional;

public interface PasseggeroDAO {
    Optional<Passeggero> findBySsn(String ssn);
    void save(Passeggero passeggero);
}