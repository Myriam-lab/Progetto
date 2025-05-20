import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class Amministratore_sistema extends Utente {

    public Amministratore_sistema(String login, String password) {
        super(login, password);
    }

    public Volo_partenza creaVoloPartenza(int gate, int hour, int minute, int year, int month, int day,
                                          String destinazione, String compagnia) {

        Volo_partenza volo = new Volo_partenza(destinazione, compagnia);
        volo.gate.setGate(gate);
        volo.setCodice(volo.getCodice());
        volo.setOrarioPrevisto(LocalTime.of(hour,minute));
        volo.setData(LocalDate.of(year,month,day));

        return volo;
    }

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

    public Volo_arrivo creaVoloArrivo(String origine, String compagnia,int hour, int minute, int year, int month, int day){

        Volo_arrivo volo = new Volo_arrivo(origine,compagnia);
        volo.setOrarioPrevisto(LocalTime.of(hour,minute));
        volo.setData(LocalDate.of(year,month,day));
        volo.setCodice(volo.getCodice()+1);

        return volo;
    }

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
        } while (scelta < 5);
    }
}
