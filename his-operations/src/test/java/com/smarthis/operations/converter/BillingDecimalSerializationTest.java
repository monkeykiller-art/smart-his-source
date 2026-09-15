package com.smarthis.operations.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthis.operations.dto.response.BillItemVo;
import com.smarthis.operations.dto.response.BillTransactionVo;
import com.smarthis.operations.entity.Bill;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class BillingDecimalSerializationTest {
    @Test
    void sendsExactDecimalStringsAndBusinessSourceToBrowser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Bill bill = new Bill();
        bill.setSourceType("REGISTRATION");
        bill.setSourceId(81L);
        bill.setPayableAmount(new BigDecimal("99999999999999.9999"));
        bill.setPaidAmount(new BigDecimal("0.0001"));
        var json = mapper.readTree(mapper.writeValueAsString(BillConverter.toVo(bill)));
        assertTrue(json.get("payableAmount").isTextual());
        assertEquals("99999999999999.9999", json.get("payableAmount").asText());
        assertEquals("REGISTRATION", json.get("sourceType").asText());
        assertEquals(81L, json.get("sourceId").asLong());
        BillItemVo item = new BillItemVo();
        item.setUnitPrice(new BigDecimal("0.0001"));
        item.setQuantity(new BigDecimal("1.0000"));
        assertTrue(mapper.readTree(mapper.writeValueAsString(item)).get("unitPrice").isTextual());
        BillTransactionVo transaction = new BillTransactionVo();
        transaction.setAmount(new BigDecimal("12.3456"));
        assertEquals("12.3456", mapper.readTree(mapper.writeValueAsString(transaction)).get("amount").asText());
    }
}
