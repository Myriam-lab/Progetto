import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String nome;
        String password;
        Scanner input = new Scanner(System.in);

        System.out.println("Inserisci nome utente: ");
        nome = input.nextLine();

        System.out.println("Inserisci password utente: ");
        password = input.nextLine();

        if(nome.equals("admin") && password.equals("admin")) {
            Amministratore_sistema a=new Amministratore_sistema(nome,password);

            boolean scelta=true;
            do{
                System.out.println("Si vuole inserire un nuovo aereo? ");
                scelta = input.nextBoolean();

                if(scelta) {
                    System.out.println("Che tipo di aereo si vuole inserire? \n(1)in partenza da Napoli \n(2) in arrivo a Napoli");
                    int tipo = input.nextInt();
                    input.nextLine(); //nextint non prende l'invio dunque devo pulire il buffer

                    switch(tipo) {
                        case 1:{
                            System.out.print("inserisci la destrinazione:");
                            String destinazione= input.nextLine();

                            System.out.print("inserisci la compagnia:");
                            String compagnia= input.nextLine();

                            System.out.print("inserisci gate: ");
                            int gate= input.nextInt();

                            System.out.print("inserisci l'ora: ");
                            int hour= input.nextInt();

                            System.out.print("inserisci i minuti: ");
                            int minute= input.nextInt();

                            System.out.print("inserisci l'anno: ");
                            int year= input.nextInt();

                            System.out.print("inserisci il mese: ");
                            int month= input.nextInt();

                            System.out.print("inserisci il giorno: ");
                            int day= input.nextInt();

                            Volo_partenza aereo1 = a.creaVoloPartenza(gate,hour,minute,year,month,day,destinazione,compagnia);

                            System.out.println("Aereo codice:"+aereo1.getCodice()+"\ndella comagnia aerea: "+aereo1.getCompagniaAerea()+
                                    "\nin data: "+aereo1.getData()+"\nprevisto per l'ora: "+aereo1.getOrarioPrevisto()+
                                    "\ncon ritardo: "+aereo1.getRitardo()+"\nal gate: "+aereo1.gate.getGate()+"\ncon destinazione: "+aereo1.getDestinazione()+
                                    "\nstato: "+aereo1.getStato());

                            System.out.print("Vuoi modificare qualcosa del volo in partenza da napoli?: ");
                            boolean scelta2=input.nextBoolean();

                            if(scelta2) {
                                a.modificaVolo(aereo1);
                                System.out.println("Aereo codice:"+aereo1.getCodice()+"\ndella comagnia aerea: "+aereo1.getCompagniaAerea()+
                                        "\nin data: "+aereo1.getData()+"\nprevisto per l'ora: "+aereo1.getOrarioPrevisto()+
                                        "\ncon ritardo: "+aereo1.getRitardo()+"\nal gate: "+aereo1.gate.getGate()+"\ncon destinazione: "+aereo1.getDestinazione()+
                                        "\nstato: "+aereo1.getStato());
                            }

                            break;
                        }
                        case 2:{
                            System.out.print("inserisci la origine:");
                            String origine= input.nextLine();

                            System.out.print("inserisci la compagnia:");
                            String compagnia= input.nextLine();

                            System.out.print("inserisci l'ora: ");
                            int hour= input.nextInt();

                            System.out.print("inserisci i minuti: ");
                            int minute= input.nextInt();

                            System.out.print("inserisci l'anno: ");
                            int year= input.nextInt();

                            System.out.print("inserisci il mese: ");
                            int month= input.nextInt();

                            System.out.print("inserisci il giorno: ");
                            int day= input.nextInt();

                            Volo_arrivo aereo2= a.creaVoloArrivo(origine,compagnia,hour,minute,year,month,day);
                            System.out.println("Aereo codice:"+aereo2.getCodice()+"\ndella comagnia aerea: "+aereo2.getCompagniaAerea()+
                                    "\nin data: "+aereo2.getData()+"\nprevisto per l'ora: "+aereo2.getOrarioPrevisto()+
                                    "\ncon ritardo: "+aereo2.getRitardo()+"\ncon origine: "+aereo2.getOrigine()+
                                    "\nstato: "+aereo2.getStato());

                            System.out.print("Vuoi modificare qualcosa del volo in arrivo a napoli?: ");
                            boolean scelta2=input.nextBoolean();

                            if(scelta2) {
                                a.modificaVolo(aereo2);
                                System.out.println("Aereo codice:"+aereo2.getCodice()+"\ndella comagnia aerea: "+aereo2.getCompagniaAerea()+
                                        "\nin data: "+aereo2.getData()+"\nprevisto per l'ora: "+aereo2.getOrarioPrevisto()+
                                        "\ncon ritardo: "+aereo2.getRitardo()+"\ncon origine: "+aereo2.getOrigine()+
                                        "\nstato: "+aereo2.getStato());
                            }

                            break;
                        }
                    }
                }

            }while (scelta);

        }else{
            Utente_generico a=new Utente_generico(nome,password);
            System.out.println("Inserisci il Nome del passeggero: ");
            String nome1 = input.nextLine();

            System.out.println("Inserisci il cognome del passeggero: ");
            String cognome1 = input.nextLine();

            System.out.println("Inserisci il telefono del passeggero: ");
            String telefono1 = input.nextLine();

            System.out.println("Inserisci l'email del passeggero");
            String email1 = input.nextLine();

            System.out.println("Inserisci l'SSN del passeggero: ");
            String ssn1 = input.nextLine();

            System.out.println("Inserisci il posto del passeggero: ");
            int posto1 = input.nextInt();

            Prenotazione p;
            p = a.creaPrenotazione(nome1, cognome1, telefono1, email1, posto1, ssn1);

            System.out.println("il passeggero ha le seguenti generalità:\n "+p.passeggero.getNome()+" "+p.passeggero.getCognome()+"\n Email:"
                    +p.passeggero.getEmail()+"\n SSN: "+p.passeggero.getSsn()+"\n POsto n°: "+p.passeggero.getPosto()+"\n Telefono: "+p.passeggero.getTelefono());

            System.out.println(" PNR: "+p.getPnr()+"\n Num biglietto: "+p.getNum_biglietto()+"\nè assicurato? "+p.isAssicurazione()
            +"\nHa il bagaglio? "+p.isBagaglio());

            System.out.println("vuoi modificare la prennotazione? ");
            boolean vuoi = input.nextBoolean();
            if(vuoi) {
                a.modificaPrenotazione(p);
            }
        }

    }
}