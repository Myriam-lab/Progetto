
public class Main {
    public static void main(String[] args) {
        Volo_arrivo aereo = new Volo_arrivo();
        System.out.println("Il volo d'andata parte da " + aereo.origine);
        aereo.setDestinazione("Londra");
        System.out.println("Il volo arriva a " + aereo.destinazione + " con codice " + aereo.getCodice());


        Volo_partenza aereo2 = new Volo_partenza();


        aereo2.setOrigine("Londra");
        System.out.println("Il volo d'andata parte da " + aereo2.origine);
        aereo2.setCodice(50601);
        aereo2.setStato(Stato_del_volo.in_ritardo);
        System.out.println("Il volo arriva a " + aereo2.destinazione + " con codice " + aereo2.getCodice());

        Volo_arrivo aereo3 = new Volo_arrivo();
        System.out.println("Il volo d'andata parte da " + aereo3.origine);
        aereo3.setDestinazione("Milano Malpensa");
        System.out.println("Il volo arriva a " + aereo3.destinazione + " con codice " + aereo3.getCodice());


        System.out.println("Lo stato del volo con codice: " + aereo.getCodice() + " è: " + aereo.getStato());
        System.out.println("Lo stato del volo con codice: " + aereo2.getCodice() + " è: " + aereo2.getStato());
    }
}