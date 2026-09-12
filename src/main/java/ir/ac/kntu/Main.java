package ir.ac.kntu;

import ir.ac.kntu.repository.*;
import ir.ac.kntu.service.*;
import ir.ac.kntu.ui.BankServices;
import ir.ac.kntu.ui.ConsoleIo;
import ir.ac.kntu.ui.FaribankCli;

/**
 * Main application entrypoint bootstrapping repositories, services, and CLI loop.
 */
public class Main {
    public static void main(String[] args) {
        UserRepository userRepo = new UserRepository();
        AccountRepository accountRepo = new AccountRepository();
        ContactRepository contactRepo = new ContactRepository();
        TicketRepository ticketRepo = new TicketRepository();

        AuthService auth = new AuthService(userRepo, accountRepo);
        AccountService acc = new AccountService(accountRepo, userRepo);
        TransferService trans = new TransferService(accountRepo, userRepo, contactRepo);
        ContactService cont = new ContactService(contactRepo, userRepo);
        SettingsService sett = new SettingsService(userRepo);
        TicketService tick = new TicketService(ticketRepo, userRepo);
        SupportService supp = new SupportService(userRepo, accountRepo);

        BankServices services = new BankServices(auth, acc, trans, cont, sett, tick, supp);
        ConsoleIo console = new ConsoleIo();
        FaribankCli cli = new FaribankCli(services, console);

        cli.start();
    }
}