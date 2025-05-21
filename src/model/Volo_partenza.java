package model;

public class Volo_partenza extends Volo{
    public Gate gate;

    public Volo_partenza(String destinazione, String compagnia) {
        origine = " Napoli";
        gate=new Gate();
        this.destinazione=destinazione;
        this.compagniaAerea = compagnia;
    }



}

