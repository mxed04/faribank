package ir.ac.kntu.ui;

import ir.ac.kntu.domain.account.Transaction;
import ir.ac.kntu.domain.account.TransferReceipt;
import ir.ac.kntu.domain.contact.Contact;
import ir.ac.kntu.domain.ticket.Ticket;
import ir.ac.kntu.domain.ticket.TicketSection;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.exception.FaribankException;

import java.util.List;
import java.util.Objects;

/**
 * Console user interface managing customer banking workflows.
 */
public class CustomerCli {
    private final BankServices services;
    private final ConsoleIo console;

    public CustomerCli(BankServices services, ConsoleIo console) {
        this.services = Objects.requireNonNull(services);
        this.console = Objects.requireNonNull(console);
    }

    public void runCustomerMenu(Customer customer) {
        while (true) {
            console.printTitle("Faribank - Customer Portal (" + customer.getFullName() + ")");
            console.printMenu("1", "Account Management");
            console.printMenu("2", "Contacts Book");
            console.printMenu("3", "Fund Transfer");
            console.printMenu("4", "Support Inquiries");
            console.printMenu("5", "Account Settings");
            console.printMenu("back", "Logout & Return");

            String choice = console.readLine("Select Option");
            if ("back".equalsIgnoreCase(choice) || "quit".equalsIgnoreCase(choice)) {
                break;
            }

            try {
                handleCustomerChoice(choice, customer);
            } catch (FaribankException ex) {
                console.printError(ex.getMessage());
            }
        }
    }

    private void handleCustomerChoice(String choice, Customer customer) {
        switch (choice) {
            case "1" -> handleAccount(customer);
            case "2" -> handleContacts(customer);
            case "3" -> handleTransfer(customer);
            case "4" -> handleTickets(customer);
            case "5" -> handleSettings(customer);
            default -> console.printError("Invalid action selected. Please try again.");
        }
    }

    private void handleAccount(Customer customer) {
        console.printTitle("Account Management");
        console.printMenu("1", "View Balance");
        console.printMenu("2", "Charge Account");
        console.printMenu("3", "Transaction History");
        console.printMenu("back", "Back");

        String choice = console.readLine("Action");
        if ("1".equals(choice)) {
            double balance = services.getAccountService().getBalance(customer.getPhoneNumber());
            console.printSuccess("Current Balance: " + balance + " IRR");
        } else if ("2".equals(choice)) {
            depositFunds(customer);
        } else if ("3".equals(choice)) {
            showTransactions(customer);
        }
    }

    private void depositFunds(Customer customer) {
        double amount = console.readDouble("Enter Deposit Amount");
        if (amount > 0) {
            Transaction chargeTx = services.getAccountService().chargeAccount(customer.getPhoneNumber(), amount);
            console.printSuccess("Deposit successful. Tracking ID: " + chargeTx.getTrackingNumber());
        }
    }

    private void showTransactions(Customer customer) {
        List<Transaction> list = services.getAccountService().getTransactions(customer.getPhoneNumber());
        if (list.isEmpty()) {
            console.printInfo("No transactions recorded yet.");
            return;
        }
        for (Transaction record : list) {
            console.printInfo("[" + record.getType() + "] " + record.getTrackingNumber()
                    + " | Amount: " + record.getAmount() + " | Fee: " + record.getFee()
                    + " | Time: " + record.getTimestamp());
        }
    }

    private void handleContacts(Customer customer) {
        console.printTitle("Contacts Book");
        console.printMenu("1", "List Contacts");
        console.printMenu("2", "Add Contact");
        console.printMenu("back", "Back");

        String choice = console.readLine("Action");
        if ("1".equals(choice)) {
            listContacts(customer);
        } else if ("2".equals(choice)) {
            addContact(customer);
        }
    }

    private void listContacts(Customer customer) {
        List<Contact> list = services.getContactService().getContacts(customer.getPhoneNumber());
        if (list.isEmpty()) {
            console.printInfo("Address book is empty.");
            return;
        }
        for (Contact contact : list) {
            console.printInfo("- " + contact.getFullName());
        }
    }

    private void addContact(Customer customer) {
        String first = console.readLine("Contact First Name");
        String last = console.readLine("Contact Last Name");
        String phone = console.readLine("Contact Phone Number (09XXXXXXXXX)");
        Contact contact = new Contact(first, last, phone);
        services.getContactService().addContact(customer.getPhoneNumber(), contact);
        console.printSuccess("Contact added successfully: " + contact.getFullName());
    }

    private void handleTransfer(Customer customer) {
        console.printTitle("Fund Transfer (0.5% Fee)");
        console.printMenu("1", "Transfer by Account Number");
        console.printMenu("2", "Transfer to Contact");
        console.printMenu("back", "Back");

        String choice = console.readLine("Action");
        if ("1".equals(choice)) {
            transferToAccount(customer);
        } else if ("2".equals(choice)) {
            transferToContact(customer);
        }
    }

    private void transferToAccount(Customer customer) {
        String destAcc = console.readLine("Destination Account Number");
        double amount = console.readDouble("Transfer Amount");
        if (amount <= 0) {
            return;
        }

        String confirm = console.readLine("Confirm transfer of " + amount + " IRR (Y/N)");
        if ("Y".equalsIgnoreCase(confirm)) {
            TransferReceipt receipt = services.getTransferService().transferByAccount(
                    customer.getPhoneNumber(), destAcc, amount);
            printReceipt(receipt);
        }
    }

    private void transferToContact(Customer customer) {
        String contactPhone = console.readLine("Contact Phone Number");
        double amount = console.readDouble("Transfer Amount");
        if (amount <= 0) {
            return;
        }

        String confirm = console.readLine("Confirm transfer of " + amount + " IRR (Y/N)");
        if ("Y".equalsIgnoreCase(confirm)) {
            TransferReceipt receipt = services.getTransferService().transferByContact(
                    customer.getPhoneNumber(), contactPhone, amount);
            printReceipt(receipt);
        }
    }

    private void printReceipt(TransferReceipt receipt) {
        console.printTitle("Transfer Receipt");
        console.printInfo("Tracking Number: " + receipt.getTrackingNumber());
        console.printInfo("Source Account:  " + receipt.getSourceAccount());
        console.printInfo("Dest Account:    " + receipt.getDestAccount());
        console.printInfo("Recipient Name:  " + receipt.getDestOwnerName());
        console.printInfo("Transfer Amount: " + receipt.getAmount() + " IRR");
        console.printInfo("Calculated Fee:  " + receipt.getFee() + " IRR");
        console.printInfo("Total Deduction: " + receipt.getTotalDeduction() + " IRR");
        console.printInfo("Timestamp:       " + receipt.getTimestamp());
        console.printSuccess("Transaction processed successfully.");
    }

    private void handleTickets(Customer customer) {
        console.printTitle("Support Inquiries");
        console.printMenu("1", "Submit Support Ticket");
        console.printMenu("2", "View Open Tickets");
        console.printMenu("back", "Back");

        String choice = console.readLine("Action");
        if ("1".equals(choice)) {
            submitTicket(customer);
        } else if ("2".equals(choice)) {
            viewOpenTickets(customer);
        }
    }

    private void submitTicket(Customer customer) {
        console.printInfo("Sections: 1) CONTACTS  2) TRANSFER  3) SETTINGS");
        String secChoice = console.readLine("Select Section");
        TicketSection section = switch (secChoice) {
            case "1" -> TicketSection.CONTACTS;
            case "2" -> TicketSection.TRANSFER;
            default -> TicketSection.SETTINGS;
        };
        String text = console.readLine("Describe your issue");
        Ticket ticket = services.getTicketService().createTicket(customer.getPhoneNumber(), section, text);
        console.printSuccess("Ticket registered. ID: " + ticket.getTicketId());
    }

    private void viewOpenTickets(Customer customer) {
        List<Ticket> openTickets = services.getTicketService().getOpenTickets(customer.getPhoneNumber());
        if (openTickets.isEmpty()) {
            console.printInfo("No active support tickets.");
            return;
        }
        for (Ticket ticket : openTickets) {
            console.printInfo("[" + ticket.getStatus() + "] (" + ticket.getSection() + ") ID: " + ticket.getTicketId());
            console.printInfo("Text: " + ticket.getText());
            if (!ticket.getSupportReply().isEmpty()) {
                console.printInfo("Reply: " + ticket.getSupportReply());
            }
        }
    }

    private void handleSettings(Customer customer) {
        console.printTitle("Account Settings");
        console.printMenu("1", "Change Account Password");
        console.printMenu("2", "Set 4-Digit Card PIN");
        console.printMenu("3", "Toggle Contacts Feature");
        console.printMenu("back", "Back");

        String choice = console.readLine("Action");
        if ("1".equals(choice)) {
            String currPass = console.readLine("Current Password");
            String newPass = console.readLine("New Strong Password");
            services.getSettingsService().changePassword(customer.getPhoneNumber(), currPass, newPass);
            console.printSuccess("Password updated successfully.");
        } else if ("2".equals(choice)) {
            String pin = console.readLine("Enter 4-digit PIN");
            services.getSettingsService().setCardPin(customer.getPhoneNumber(), pin);
            console.printSuccess("Credit card PIN configured successfully.");
        } else if ("3".equals(choice)) {
            boolean current = customer.isContactsEnabled();
            services.getSettingsService().toggleContacts(customer.getPhoneNumber(), !current);
            console.printSuccess("Contacts feature toggled to: " + (!current ? "ENABLED" : "DISABLED"));
        }
    }
}