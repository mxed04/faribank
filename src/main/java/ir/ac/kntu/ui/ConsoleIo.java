package ir.ac.kntu.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

/**
 * Handles colored terminal printing and user input scanning.
 */
public class ConsoleIo {
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleIo(InputStream input, PrintStream out) {
        this.scanner = new Scanner(Objects.requireNonNull(input, "Input cannot be null"));
        this.out = Objects.requireNonNull(out, "Out cannot be null");
    }

    public ConsoleIo() {
        this(System.in, System.out);
    }

    public void printTitle(String text) {
        out.println(AnsiColor.CYAN + AnsiColor.BOLD + "\n=== " + text + " ===" + AnsiColor.RESET);
    }

    public void printSuccess(String text) {
        out.println(AnsiColor.GREEN + "[SUCCESS] " + text + AnsiColor.RESET);
    }

    public void printError(String text) {
        out.println(AnsiColor.RED + "[ERROR] " + text + AnsiColor.RESET);
    }

    public void printWarning(String text) {
        out.println(AnsiColor.YELLOW + "[NOTICE] " + text + AnsiColor.RESET);
    }

    public void printInfo(String text) {
        out.println(AnsiColor.BLUE + text + AnsiColor.RESET);
    }

    public void printMenu(String option, String description) {
        out.println("  " + AnsiColor.CYAN + option + AnsiColor.RESET + ") " + description);
    }

    public String readLine(String prompt) {
        out.print(AnsiColor.BOLD + prompt + ": " + AnsiColor.RESET);
        if (!scanner.hasNextLine()) {
            return "quit";
        }
        return scanner.nextLine().trim();
    }

    public double readDouble(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if ("back".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                return -1.0;
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException ex) {
                printError("Invalid decimal number. Please try again.");
            }
        }
    }
}