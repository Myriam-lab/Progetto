package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

/**
 * Rappresenta un utente con privilegi di amministratore,
 * in grado di creare e modificare i voli.
 */
public class Amministratore_sistema extends Utente {

    /**
     * Costruttore per un nuovo Amministratore_sistema.
     * @param login l'username per l'accesso.
     * @param password la password per l'accesso.
     */
    public Amministratore_sistema(String login, String password) {
        super(login, password);
    }

    /**
     * Crea un nuovo volo in partenza con i dettagli specificati.
     * @param gate numero del gate.
     * @param hour ora di partenza.
     * @param minute minuto di partenza.
     * @param year anno di partenza.
     * @param month mese di partenza.
     * @param day giorno di partenza.
     * @param destinazione destinazione del volo.
     * @param compagnia compagnia aerea.
     * @return un oggetto Volo_partenza.
     */
    public Volo_partenza creaVoloPartenza(int gate, int hour, int minute, int year, int month, int day,
                                          String destinazione, String compagnia) {

        Volo_partenza volo = new Volo_partenza(destinazione, compagnia);
        volo.gate.setGate(gate);
        volo.setCodice(volo.getCodice());
        volo.setOrarioPrevisto(LocalTime.of(hour,minute));
        volo.setData(LocalDate.of(year,month,day));

        return volo;
    }

    /**
     * Permette di modificare i dettagli di un volo in partenza tramite input da console.
     * @param volo il volo da modificare.
     */
    public void modificaVolo(Volo_partenza volo) {
        Scanner sc = new Scanner(System.in);
        int scelta;

        do{
            System.out.println("cosa si desidera modificare?\n(1) Gate\n(2) OrarioPrevisto\n(3) Data\n(4) Ritardo\n(5) Stato\n(6) Nulla");
            scelta = sc.nextInt();

            switch (scelta) {
                case 1: {
                    System.out.print("Nuovo gate:");
                    int gate = sc.nextInt();
                    sc.nextLine();

                    volo.gate.setGate(gate);
                    break;
                }
                case 2: {
                    System.out.print("ora: ");
                    int hour = sc.nextInt();
                    sc.nextLine();
                    System.out.print("minuti: ");
                    int minute = sc.nextInt();
                    sc.nextLine();

                    volo.setOrarioPrevisto(LocalTime.of(hour, minute));
                    break;
                }
                case 3: {
                    System.out.print("Anno:");
                    int year = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Mese: ");
                    int month = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Giorno: ");
                    int day = sc.nextInt();
                    sc.nextLine();

                    volo.setData(LocalDate.of(year, month, day));
                    break;
                }
                case 4: {
                    System.out.print("Ritardo: ");
                    int rit = sc.nextInt();
                    sc.nextLine();

                    volo.setRitardo(rit);
                    break;
                }
                case 5: {
                    System.out.println("(1) Cancellato (2) Rinviato (3) in ritardo (4) in orario (5) Atterrato");
                    int scelta2 = sc.nextInt();
                    sc.nextLine(); // <-- Consuma il newline rimasto nel buffer

                    switch (scelta2) {
                        case 1 -> volo.setStato(Stato_del_volo.cancellato);
                        case 2 -> volo.setStato(Stato_del_volo.rinviato);
                        case 3 -> volo.setStato(Stato_del_volo.in_ritardo);
                        case 4 -> volo.setStato(Stato_del_volo.in_orario);
                        case 5 -> volo.setStato(Stato_del_volo.atterrato);
                        default -> System.out.println("Scelta non valida.");
                    }
                    break;
                }

            }
        }while (scelta < 6);
    }

    /**
     * Crea un nuovo volo in arrivo con i dettagli specificati.
     * @param origine origine del volo.
     * @param compagnia compagnia aerea.
     * @param hour ora di arrivo.
     * @param minute minuto di arrivo.
     * @param year anno di arrivo.
     * @param month mese di arrivo.
     * @param day giorno di arrivo.
     * @return un oggetto Volo_arrivo.
     */
    public Volo_arrivo creaVoloArrivo(String origine, String compagnia,int hour, int minute, int year, int month, int day){

        Volo_arrivo volo = new Volo_arrivo(origine,compagnia);
        volo.setOrarioPrevisto(LocalTime.of(hour,minute));
        volo.setData(LocalDate.of(year,month,day));
        volo.setCodice(volo.getCodice()+1);

        return volo;
    }

    /**
     * Permette di modificare i dettagli di un volo in arrivo tramite input da console.
     * @param volo il volo da modificare.
     */
    public void modificaVolo(Volo_arrivo volo) {
        Scanner sc = new Scanner(System.in);
        int scelta;

        do {
            System.out.println("cosa si desidera modificare?\n(1) OrarioPrevisto\n(2) Data\n(3) Ritardo\n(4) Stato\n(5) Nulla");
            scelta = sc.nextInt();

            switch (scelta) {
                case 1: {
                    System.out.print("ora: ");
                    int hour = sc.nextInt();
                    sc.nextLine();
                    System.out.print("minuti: ");
                    int minute = sc.nextInt();
                    sc.nextLine();

                    volo.setOrarioPrevisto(LocalTime.of(hour, minute));
                    break;
                }
                case 2: {
                    System.out.print("Anno:");
                    int year = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Mese: ");
                    int month = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Giorno: ");
                    int day = sc.nextInt();
                    sc.nextLine();

                    volo.setData(LocalDate.of(year, month, day));
                    break;
                }
                case 3: {
                    System.out.print("Ritardo: ");
                    int rit = sc.nextInt();
                    sc.nextLine();

                    volo.setRitardo(rit);
                    break;
                }
                case 4: {
                    System.out.println("(1) Cancellato (2) Rinviato (3) in ritardo (4) in orario (5) Atterrato");
                    int scelta2 = sc.nextInt();
                    sc.nextLine();

                    switch (scelta2) {
                        case 1 -> volo.setStato(Stato_del_volo.cancellato);
                        case 2 -> volo.setStato(Stato_del_volo.rinviato);
                        case 3 -> volo.setStato(Stato_del_volo.in_ritardo);
                        case 4 -> volo.setStato(Stato_del_volo.in_orario);
                        case 5 -> volo.setStato(Stato_del_volo.atterrato);
                        default -> System.out.println("Scelta non valida.");
                    }
                    break;
                }

            }
        } while (scelta < 5);
    }
}
