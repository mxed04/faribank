package ir.ac.kntu.ui;

import ir.ac.kntu.service.*;

import java.util.Objects;

/**
 * Service container bundling core domain services for UI consumers.
 */
public class BankServices {
    private final AuthService authService;
    private final AccountService accountService;
    private final TransferService transferService;
    private final ContactService contactService;
    private final SettingsService settingsService;
    private final TicketService ticketService;
    private final SupportService supportService;

    public BankServices(AuthService auth, AccountService acc, TransferService trans,
                        ContactService cont, SettingsService sett, TicketService tick, SupportService supp) {
        this.authService = Objects.requireNonNull(auth);
        this.accountService = Objects.requireNonNull(acc);
        this.transferService = Objects.requireNonNull(trans);
        this.contactService = Objects.requireNonNull(cont);
        this.settingsService = Objects.requireNonNull(sett);
        this.ticketService = Objects.requireNonNull(tick);
        this.supportService = Objects.requireNonNull(supp);
    }

    public AuthService getAuthService() {
        return authService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public TransferService getTransferService() {
        return transferService;
    }

    public ContactService getContactService() {
        return contactService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public TicketService getTicketService() {
        return ticketService;
    }

    public SupportService getSupportService() {
        return supportService;
    }
}