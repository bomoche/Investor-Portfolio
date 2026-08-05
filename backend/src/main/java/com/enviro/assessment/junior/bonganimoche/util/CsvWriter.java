package com.enviro.assessment.junior.bonganimoche.util;

import java.util.List;

/**
 * Minimal RFC 4180 CSV writer.
 *
 * Written by hand rather than pulling in OpenCSV or Apache Commons CSV: the
 * output is a handful of flat columns, and the escaping rules that actually
 * matter here fit in one method. A dependency would be the right call for
 * anything more involved.
 */
public final class CsvWriter {

    private CsvWriter() {
    }

    private static final char DELIMITER = ',';
    private static final String LINE_ENDING = "\r\n";  // RFC 4180 specifies CRLF

    /** Characters that lead spreadsheets to evaluate a cell as a formula. */
    private static final String FORMULA_TRIGGERS = "=+-@";

    public static String toRow(List<String> values) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                row.append(DELIMITER);
            }
            row.append(escape(values.get(i)));
        }
        return row.append(LINE_ENDING).toString();
    }

    /**
     * Escapes one field.
     *
     * A field is wrapped in quotes if it contains a delimiter, a quote or a line
     * break, and any embedded quote is doubled. Without this, a product name
     * containing a comma would silently shift every subsequent column.
     *
     * Fields starting with =, +, - or @ are prefixed with a single quote. Excel
     * and LibreOffice evaluate such cells as formulas, which is a known
     * injection vector when exported data is later opened by a user.
     */
    private static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        String sanitised = value;
        if (FORMULA_TRIGGERS.indexOf(sanitised.charAt(0)) >= 0) {
            sanitised = "'" + sanitised;
        }

        boolean needsQuoting = sanitised.indexOf(DELIMITER) >= 0
                || sanitised.indexOf('"') >= 0
                || sanitised.indexOf('\n') >= 0
                || sanitised.indexOf('\r') >= 0;

        if (!needsQuoting) {
            return sanitised;
        }
        return '"' + sanitised.replace("\"", "\"\"") + '"';
    }
}