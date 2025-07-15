package model;

/**
 * Rappresenta un gate di imbarco dell'aeroporto.
 */
public class Gate {
    private int numero;

    /**
     * Costruttore di default per un Gate.
     * Inizializza il numero del gate a 0.
     */
    public Gate() {
        this.numero = 0;
    }

    /**
     * Restituisce il numero del gate.
     * @return il numero del gate.
     */
    public int getGate(){
        return numero;
    }

    /**
     * Imposta il numero del gate.
     * @param gate il nuovo numero del gate.
     */
    public void setGate(int gate){
        numero = gate;
    }
}
