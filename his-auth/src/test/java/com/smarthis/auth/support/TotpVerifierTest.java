package com.smarthis.auth.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TotpVerifierTest {
    @Test void generatesRfc6238Sha1Vector() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
        // RFC 6238 test timestamp 59 seconds maps to counter 1.
        assertEquals("287082", TotpVerifier.generateCode(secret, 59L / 30));
    }
}
