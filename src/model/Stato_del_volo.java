package model;

/**
 * Enumera i possibili stati di un volo.
 */
public enum Stato_del_volo {
    /**
     * Il volo è stato cancellato.
     */
    cancellato,
    /**
     * Il volo è stato rinviato a una data/orario successivo.
     */
    rinviato,
    /**
     * Il volo è in ritardo rispetto all'orario previsto.
     */
    in_ritardo,
    /**
     * Il volo è in orario.
     */
    in_orario,
    /**
     * Il volo è atterrato a destinazione.
     */
    atterrato;
}
