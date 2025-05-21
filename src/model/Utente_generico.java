package model;

import java.util.Scanner;

public class Utente_generico extends Utente {

    public Utente_generico(String login, String password) {
        super(login, password);
    }

    public Prenotazione creaPrenotazione(String nome, String cognome, String telefono, String email,
                                  String posto, String ssn) {
        Prenotazione prenotazione = new Prenotazione();
        Scanner sc = new Scanner(System.in);

        System.out.println("Vuoi attivare l'assicurazione? ");
        boolean scelta = sc.nextBoolean();
        if(scelta){
            prenotazione.updateAssicurazione();
        }

        System.out.println("Vuoi portare un bagaglio con te? ");
        scelta = sc.nextBoolean();
        if(scelta){
            prenotazione.updateBagaglio();
        }

        prenotazione.setPnr(prenotazione.getPnr()+1);
        prenotazione.setNum_biglietto(prenotazione.getNum_biglietto()+1);

        prenotazione.passeggero.setNome(nome);
        prenotazione.passeggero.setCognome(cognome);
        prenotazione.passeggero.setTelefono(telefono);
        prenotazione.passeggero.setPosto(posto);
        prenotazione.passeggero.setSsn(ssn);
        prenotazione.passeggero.setEmail(email);

        return prenotazione;
    }

    public void modificaPrenotazione(Prenotazione pren) {
        Scanner sc = new Scanner(System.in);
        int scelta1;

        do {
            System.out.println("cosa vuoi modificare? \n(1) Nome\n (2) Cognome\n (3) Telefono\n (4) Posto\n (5) Ssn\n (6) Email\n " +
                    "(7) Assicurazione \n(8) Bagaglio \n (9) Nulla");
            scelta1 = sc.nextInt();
            switch (scelta1) {
                 case 1: {
                     System.out.println("Inserisci il nome");
                     String nome=sc.nextLine();
                     pren.passeggero.setNome(nome);
                     break;
                 }
                 case 2: {
                     System.out.println("Inserisci il cognome");
                     String cognome=sc.nextLine();
                     pren.passeggero.setCognome(cognome);
                     break;
                 }
                 case 3: {
                     System.out.println("Inserisci il telefono");
                     String telefono =sc.nextLine();
                     pren.passeggero.setTelefono(telefono);
                     break;
                 }
                 case 4: {
                     System.out.println("Inserisci il posto");
                     String posto=sc.nextLine();
                     pren.passeggero.setPosto(posto);
                     break;
                 }
                 case 5: {
                     System.out.println("Inserisci l'ssn");
                     String ssn=sc.nextLine();
                     pren.passeggero.setSsn(ssn);
                     break;
                 }
                 case 6: {
                     System.out.println("Inserisci l'email");
                     String email =sc.nextLine();
                     pren.passeggero.setEmail(email);
                     break;
                 }
                 case 7: {
                     if(!(pren.isAssicurazione())){
                         System.out.println("Vuoi attivare l'assicurazione? ");
                         boolean scelta = sc.nextBoolean();
                         if(scelta){
                             pren.updateAssicurazione();
                         }
                     }
                     break;
                 }
                 case 8: {
                     if(!(pren.isBagaglio())){
                         System.out.println("Vuoi portare un bagaglio con te? ");
                         boolean scelta = sc.nextBoolean();
                         if(scelta){
                             pren.updateBagaglio();
                         }
                     }
                     break;
                 }
             }
        }while (scelta1 < 9);
    }
}
