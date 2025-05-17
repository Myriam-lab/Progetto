public class Volo_arrivo extends Volo{

    public Volo_arrivo(String origine, String compagnia) {
        destinazione = "Napoli";
        this.origine = origine;
        this.compagniaAerea = compagnia;
    }

    public void setOrigine(String origine) {
        this.origine = origine;
    }

    public String getOrigine() {
        return origine;
    }
}
