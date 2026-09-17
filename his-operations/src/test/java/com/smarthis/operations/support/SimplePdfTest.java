package com.smarthis.operations.support;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplePdfTest {
    @Test void createsPdfWithHeaderAndCrossReferenceTable() {
        String pdf = new String(SimplePdf.create(List.of("Smart HIS report", "Revenue: 12.34")), StandardCharsets.ISO_8859_1);
        assertTrue(pdf.startsWith("%PDF-1.4"));
        assertTrue(pdf.contains("xref"));
        assertTrue(pdf.endsWith("%%EOF"));
    }
}
