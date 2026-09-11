package ir.ac.kntu.ui;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleIoTest {

    @Test
    void testAnsiColorCodesConstants() {
        assertNotNull(AnsiColor.RESET);
        assertNotNull(AnsiColor.GREEN);
        assertNotNull(AnsiColor.RED);
        assertNotNull(AnsiColor.CYAN);
    }

    @Test
    void testConsoleIoOutputsColoredMessages() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ConsoleIo console = new ConsoleIo(new ByteArrayInputStream("quit\n".getBytes()), new PrintStream(outContent));

        console.printSuccess("Operation Successful");
        console.printError("Operation Failed");

        String output = outContent.toString();
        assertTrue(output.contains("[SUCCESS]"));
        assertTrue(output.contains("[ERROR]"));
    }

    @Test
    void testConsoleIoReadsInputAndParsesDouble() {
        String mockInput = "1500.50\n";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ConsoleIo console = new ConsoleIo(new ByteArrayInputStream(mockInput.getBytes()), new PrintStream(outContent));

        double value = console.readDouble("Enter Amount");
        assertEquals(1500.50, value, 0.001);
    }
}