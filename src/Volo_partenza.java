public class Volo_partenza extends Volo{
    Gate gate;

    public Volo_partenza() {
        destinazione = " Napoli";
        gate=new Gate(02);
    }
    public void setOrigine(String origine) {
        this.origine = origine;
    }
    public String getOrigine() {
        return origine;
    }


}

