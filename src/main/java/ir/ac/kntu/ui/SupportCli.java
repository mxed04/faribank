package ir.ac.kntu.ui;

import ir.ac.kntu.domain.ticket.Ticket;
import ir.ac.kntu.domain.ticket.TicketStatus;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.CustomerSummary;
import ir.ac.kntu.exception.FaribankException;

import java.util.List;
import java.util.Objects;

/**
 * Console user interface managing support operator privileges.
 */
public class SupportCli {
    private final BankServices services;
    private final ConsoleIo console;

    public SupportCli(BankServices services, ConsoleIo console) {
        this.services = Objects.requireNonNull(services);
        this.console = Objects.requireNonNull(console);
    }

    public void runSupportMenu() {
        while (true) {
            console.printTitle("Faribank - Operator Portal");
            console.printMenu("1", "KYC Identity Verifications");
            console.printMenu("2", "Manage Customer Tickets");
            console.printMenu("3", "Search & Inspect Users");
            console.printMenu("back", "Logout & Return");

            String choice = console.readLine("Select Option");
            if ("back".equalsIgnoreCase(choice) || "quit".equalsIgnoreCase(choice)) {
                break;
            }

            try {
                handleSupportChoice(choice);
            } catch (FaribankException ex) {
                console.printError(ex.getMessage());
            }
        }
    }

    private void handleSupportChoice(String choice) {
        switch (choice) {
            case "1" -> handleKyc();
            case "2" -> handleTickets();
            case "3" -> handleUserSearch();
            default -> console.printError("Invalid option. Please try again.");
        }
    }

    private void handleKyc() {
        console.printTitle("Pending KYC Verifications");
        List<Customer> pending = services.getAuthService().getPendingKycRequests();
        if (pending.isEmpty()) {
            console.printInfo("No pending KYC verification requests.");
            return;
        }

        for (Customer cust : pending) {
            console.printInfo("User: " + cust.getFullName() + " | Phone: " + cust.getPhoneNumber()
                    + " | National Code: " + cust.getNationalCode());
        }

        String phone = console.readLine("Enter phone to inspect or 'back'");
        if ("back".equalsIgnoreCase(phone)) {
            return;
        }

        console.printMenu("A", "Approve Application");
        console.printMenu("R", "Reject Application");
        String decision = console.readLine("Decision");

        if ("A".equalsIgnoreCase(decision)) {
            services.getAuthService().approveKyc(phone);
            console.printSuccess("KYC Approved. Account & card generated for: " + phone);
        } else if ("R".equalsIgnoreCase(decision)) {
            String reason = console.readLine("Rejection Reason");
            services.getAuthService().rejectKyc(phone, reason);
            console.printWarning("KYC Rejected for user: " + phone);
        }
    }

    private void handleTickets() {
        console.printTitle("Customer Tickets");
        List<Ticket> list = services.getTicketService().filterTickets(null, null, null);
        if (list.isEmpty()) {
            console.printInfo("No support tickets found.");
            return;
        }

        for (Ticket tckt : list) {
            console.printInfo("[" + tckt.getStatus() + "] ID: " + tckt.getTicketId()
                    + " | Phone: " + tckt.getUserPhoneNumber() + " | Text: " + tckt.getText());
        }

        String ticketId = console.readLine("Enter ticket ID to reply or 'back'");
        if ("back".equalsIgnoreCase(ticketId)) {
            return;
        }

        String reply = console.readLine("Enter Reply Message");
        console.printMenu("1", "Mark IN_PROGRESS");
        console.printMenu("2", "Mark CLOSED");
        String statusChoice = console.readLine("Status Choice");
        TicketStatus status = "2".equals(statusChoice) ? TicketStatus.CLOSED : TicketStatus.IN_PROGRESS;

        services.getTicketService().replyTicket(ticketId, reply, status);
        console.printSuccess("Ticket reply saved and updated successfully.");
    }

    private void handleUserSearch() {
        console.printTitle("User Directory Inspection");
        String query = console.readLine("Enter Phone or Name query");
        List<CustomerSummary> results = services.getSupportService().searchCustomers(query, query, query);

        if (results.isEmpty()) {
            console.printInfo("No users found matching query: " + query);
            return;
        }

        for (CustomerSummary summary : results) {
            console.printInfo("Name: " + summary.getFullName() + " | Phone: " + summary.getPhone()
                    + " | Acc: " + summary.getAccountNum() + " | Total Tx: " + summary.getTransactions().size());
        }
    }
}