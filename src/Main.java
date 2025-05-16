import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Volo_arrivo aereo = new Volo_arrivo();
        System.out.println("Il volo d'andata parte da " + aereo.origine);
        aereo.setDestinazione("Londra");
        System.out.println("Il volo arriva a " + aereo.destinazione + " con codice " + aereo.getCodice());

        //prova aereo in partenza
        Volo_partenza aereo2 = new Volo_partenza();
        aereo2.setOrigine("Londra");
        System.out.println("Il volo d'andata parte da " + aereo2.origine);
        aereo2.setCodice(50601);
        aereo2.setStato(Stato_del_volo.in_ritardo);
        System.out.println("Il volo arriva a " + aereo2.destinazione + " con codice " + aereo2.getCodice()+" al gate:"+aereo2.gate.getGate());

        //prova aereo in arrivo
        Volo_arrivo aereo3 = new Volo_arrivo();
        System.out.println("Il volo d'andata parte da " + aereo3.origine);
        aereo3.setDestinazione("Milano Malpensa");
        System.out.println("Il volo arriva a " + aereo3.destinazione + " con codice " + aereo3.getCodice());

        System.out.println("Lo stato del volo con codice: " + aereo.getCodice() + " è: " + aereo.getStato());
        System.out.println("Lo stato del volo con codice: " + aereo2.getCodice() + " è: " + aereo2.getStato());

        //prova utente
        String nome;
        String password;
        Scanner input = new Scanner(System.in);

        System.out.println("Inserisci nome utente: ");
        nome = input.nextLine();

        System.out.println("Inserisci password utente: ");
        password = input.nextLine();

        Utente a=new Utente(nome,password);
        System.out.println("Nome utente: "+a.getLogin()+" password: "+a.getPassword());

        //prova delle funzioni nuove di compagnia
        System.out.println("inserisci la compagnia aerea: ");
        aereo.setCompagniaAerea(input.nextLine());
        System.out.println("la compagnia aerea: "+aereo.getCompagniaAerea());

        //prova prenotazione
        Prenotazione b= new Prenotazione();
        System.out.println("inserisci pnr:" );
        String pnr = input.nextLine();
        b.setPnr(pnr);

        System.out.println("Inserisci il numero prenotazione: ");
        String num = input.nextLine();
        b.setNum_biglietto(num);

        b.setStato(Stato_prenotazione.in_attesa);
        System.out.println("lo stato della prenotazione è: "+b.getStato());

        b.updateBagaglio();
        System.out.println("ha il bagaglio: "+b.isBagaglio());

        b.updateAssicurazione();
        System.out.println("ha l'assicurazione: "+b.isAssicurazione());

        //provala passeggero
        Passeggero p=new Passeggero();
        System.out.println("Inserisci il Nome del passeggero: ");
        String nome1 = input.nextLine();
        p.setNome(nome1);

        System.out.println("Inserisci il cognome del passeggero: ");
        String cognome1 = input.nextLine();
        p.setCognome(cognome1);

        System.out.println("Inserisci l'email del passeggero");
        String email1 = input.nextLine();
        p.setEmail(email1);

        System.out.println("Inserisci l'SSN del passeggero: ");
        String ssn1 = input.nextLine();
        p.setSsn(ssn1);

        System.out.println("Inserisci il posto del passeggero: ");
        int posto1 = input.nextInt();
        p.setPosto(posto1);

        System.out.println("Inserisci il telefono del passeggero: ");
        String telefono1 = input.nextLine();
        p.setTelefono(telefono1);

        System.out.println("il passeggero ha le seguenti generalità:\n"+p.getNome()+" "+p.getCognome()+"\n"
                +p.getEmail()+"\n"+p.getSsn()+"\n"+p.getPosto()+"\n"+p.getTelefono());

        System.out.println("ciao prova");
        System.out.println("ciao more");

    }
}