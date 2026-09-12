package ir.ac.kntu.ui.report;

import ir.ac.kntu.domain.account.Account;
import ir.ac.kntu.domain.account.Transaction;
import ir.ac.kntu.domain.account.TransactionType;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.exception.ValidationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Generates structured financial transaction reports and CSS charts in HTML format.
 */
public class HtmlReportGenerator {

    public String generateReportHtml(Customer customer) {
        if (customer == null) {
            throw new ValidationException("Customer cannot be null for report generation.");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        builder.append("<meta charset=\"UTF-8\">\n<title>Faribank Financial Statement</title>\n");
        builder.append(buildCssStyles());
        builder.append("</head>\n<body>\n<div class=\"container\">\n");
        builder.append(buildHeaderSection(customer));
        builder.append(buildMetricsCards(customer));

        List<Transaction> records = getTransactions(customer);
        builder.append(buildChartSection(records));
        builder.append(buildTableSection(records));
        builder.append("</div>\n</body>\n</html>");

        return builder.toString();
    }

    public void exportToFile(Customer customer, File targetFile) throws IOException {
        Objects.requireNonNull(targetFile, "Target file cannot be null");
        String htmlContent = generateReportHtml(customer);

        try (FileWriter writer = new FileWriter(targetFile, StandardCharsets.UTF_8)) {
            writer.write(htmlContent);
        }
    }

    private List<Transaction> getTransactions(Customer customer) {
        Account account = customer.getAccount();
        if (account == null || account.getTransactions() == null) {
            return List.of();
        }
        return account.getTransactions();
    }

    private String buildCssStyles() {
        return "<style>\n"
                + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; "
                + "background: #f4f7f6; margin: 0; padding: 25px; color: #333; }\n"
                + ".container { max-width: 900px; margin: 0 auto; background: #fff; padding: 30px; "
                + "border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }\n"
                + ".header { border-bottom: 2px solid #0052cc; padding-bottom: 15px; margin-bottom: 25px; }\n"
                + ".title { margin: 0; color: #0052cc; font-size: 26px; }\n"
                + ".cards { display: flex; gap: 15px; margin-bottom: 30px; }\n"
                + ".card { flex: 1; padding: 15px; background: #ebf2fa; border-radius: 8px; }\n"
                + ".card-title { font-size: 13px; color: #555; text-transform: uppercase; }\n"
                + ".card-value { font-size: 20px; font-weight: bold; margin-top: 5px; color: #0747a6; }\n"
                + ".chart-box { background: #fafbfc; border: 1px solid #e1e4e8; border-radius: 8px; "
                + "padding: 20px; margin-bottom: 30px; }\n"
                + ".bar-track { background: #e0e0e0; border-radius: 6px; height: 26px; display: flex; "
                + "overflow: hidden; margin-top: 10px; }\n"
                + ".bar-in { background: #36b37e; color: #fff; text-align: center; font-size: 12px; "
                + "line-height: 26px; font-weight: bold; }\n"
                + ".bar-out { background: #ff5630; color: #fff; text-align: center; font-size: 12px; "
                + "line-height: 26px; font-weight: bold; }\n"
                + "table { width: 100%; border-collapse: collapse; margin-top: 15px; }\n"
                + "th, td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }\n"
                + "th { background: #fafbfc; color: #444; }\n"
                + ".badge-charge { color: #00875a; font-weight: bold; }\n"
                + ".badge-out { color: #de350b; font-weight: bold; }\n"
                + "</style>\n";
    }

    private String buildHeaderSection(Customer customer) {
        return "<div class=\"header\">\n"
                + "<h1 class=\"title\">Faribank Account Statement</h1>\n"
                + "<p>Holder: <strong>" + customer.getFullName() + "</strong> | Phone: "
                + customer.getPhoneNumber() + " | National ID: " + customer.getNationalCode() + "</p>\n"
                + "</div>\n";
    }

    private String buildMetricsCards(Customer customer) {
        String accNumber = customer.getAccount() != null ? customer.getAccount().getAccountNumber() : "N/A";
        double balance = customer.getAccount() != null ? customer.getAccount().getBalance() : 0.0;

        return "<div class=\"cards\">\n"
                + "<div class=\"card\"><div class=\"card-title\">Account Number</div>"
                + "<div class=\"card-value\">" + accNumber + "</div></div>\n"
                + "<div class=\"card\"><div class=\"card-title\">Current Balance</div>"
                + "<div class=\"card-value\">" + balance + " IRR</div></div>\n"
                + "</div>\n";
    }

    private String buildChartSection(List<Transaction> records) {
        double totalInflow = 0.0;
        double totalOutflow = 0.0;

        for (Transaction item : records) {
            boolean isInflow = item.getType() == TransactionType.CHARGE
                    || "TRANSFER_IN".equals(item.getType().name());
            if (isInflow) {
                totalInflow += item.getAmount();
            } else {
                totalOutflow += (item.getAmount() + item.getFee());
            }
        }

        double totalVolume = totalInflow + totalOutflow;
        int inPercent = totalVolume > 0 ? (int) Math.round((totalInflow / totalVolume) * 100) : 50;
        int outPercent = totalVolume > 0 ? (100 - inPercent) : 50;

        return "<div class=\"chart-box\">\n"
                + "<h3>Financial Cash Flow Chart</h3>\n"
                + "<p>Inflow: " + totalInflow + " IRR (" + inPercent + "%) | Outflow: "
                + totalOutflow + " IRR (" + outPercent + "%)</p>\n"
                + "<div class=\"bar-track\">\n"
                + "<div class=\"bar-in\" style=\"width: " + inPercent + "%;\">+" + inPercent + "% Inflow</div>\n"
                + "<div class=\"bar-out\" style=\"width: " + outPercent + "%;\">-" + outPercent + "% Outflow</div>\n"
                + "</div>\n"
                + "</div>\n";
    }

    private String buildTableSection(List<Transaction> records) {
        StringBuilder builder = new StringBuilder();
        builder.append("<h3>Detailed Transactions</h3>\n");
        builder.append("<table>\n<thead><tr>\n");
        builder.append("<th>Tracking #</th><th>Type</th><th>Amount</th><th>Fee</th><th>Timestamp</th>\n");
        builder.append("</tr></thead>\n<tbody>\n");

        if (records.isEmpty()) {
            builder.append("<tr><td colspan=\"5\" style=\"text-align:center;\">No recorded transactions.</td></tr>\n");
        } else {
            for (Transaction item : records) {
                boolean isInflow = item.getType() == TransactionType.CHARGE
                        || "TRANSFER_IN".equals(item.getType().name());
                String badgeClass = isInflow ? "badge-charge" : "badge-out";
                builder.append("<tr>\n");
                builder.append("<td>").append(item.getTrackingNumber()).append("</td>\n");
                builder.append("<td class=\"").append(badgeClass).append("\">").append(item.getType()).append("</td>\n");
                builder.append("<td>").append(item.getAmount()).append(" IRR</td>\n");
                builder.append("<td>").append(item.getFee()).append(" IRR</td>\n");
                builder.append("<td>").append(item.getTimestamp()).append("</td>\n");
                builder.append("</tr>\n");
            }
        }

        builder.append("</tbody>\n</table>\n");
        return builder.toString();
    }
}