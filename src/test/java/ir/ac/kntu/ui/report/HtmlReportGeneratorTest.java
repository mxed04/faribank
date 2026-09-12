package ir.ac.kntu.ui.report;

import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.UserRepository;
import ir.ac.kntu.service.AccountService;
import ir.ac.kntu.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HtmlReportGeneratorTest {
    private HtmlReportGenerator generator;
    private Customer customer;

    @BeforeEach
    void setUp() {
        generator = new HtmlReportGenerator();
        UserRepository userRepo = new UserRepository();
        AccountRepository accountRepo = new AccountRepository();
        AuthService authService = new AuthService(userRepo, accountRepo);
        AccountService accountService = new AccountService(accountRepo, userRepo);

        customer = new Customer("Kian", "Afshar", "09129994433", "0022334455", "Kian@2024");
        authService.registerCustomer(customer);
        authService.approveKyc(customer.getPhoneNumber());

        accountService.chargeAccount(customer.getPhoneNumber(), 75000.0);
    }

    @Test
    void testHtmlReportContainsMetadataAndCss() {
        String html = generator.generateReportHtml(customer);
        assertNotNull(html);
        assertTrue(html.contains("Faribank Account Statement"));
        assertTrue(html.contains("Kian Afshar"));
        assertTrue(html.contains("09129994433"));
        assertTrue(html.contains("<style>"));
        assertTrue(html.contains("CHARGE"));
        assertTrue(html.contains("75000.0 IRR"));
    }

    @Test
    void testHtmlReportContainsChartCashFlow() {
        String html = generator.generateReportHtml(customer);
        assertTrue(html.contains("Financial Cash Flow Chart"));
        assertTrue(html.contains("bar-track"));
        assertTrue(html.contains("Inflow"));
    }

    @Test
    void testExportReportToFileSuccessful(@TempDir Path tempDir) throws IOException {
        File reportFile = tempDir.resolve("financial_statement.html").toFile();
        generator.exportToFile(customer, reportFile);

        assertTrue(reportFile.exists());
        assertTrue(reportFile.length() > 0);

        String fileContent = Files.readString(reportFile.toPath());
        assertTrue(fileContent.contains("</html>"));
        assertTrue(fileContent.contains("Faribank"));
    }

    @Test
    void testGenerateReportThrowsOnNullCustomer() {
        assertThrows(ValidationException.class, () -> generator.generateReportHtml(null));
    }
}