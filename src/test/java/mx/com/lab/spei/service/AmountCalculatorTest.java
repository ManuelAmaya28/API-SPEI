package mx.com.lab.spei.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AmountCalculatorTest {

    private final AmountCalculator calc = new AmountCalculator();

    @Test
    void calculateSpread_1000x0005_returns5_00() {
        assertEquals(new BigDecimal("5.00"),
            calc.calculateSpread(new BigDecimal("1000.00"), new BigDecimal("0.005")));
    }

    @Test
    void calculateSpread_result_hasTwoDecimalPlaces() {
        BigDecimal result = calc.calculateSpread(new BigDecimal("333.33"), new BigDecimal("0.005"));
        assertEquals(2, result.scale());
    }

    @Test
    void calculatePrincipal_returnsSameAmount() {
        assertEquals(new BigDecimal("1000.00"),
            calc.calculatePrincipal(new BigDecimal("1000.00")));
    }

    @Test
    void calculatePrincipal_scalesTo2Decimals() {
        assertEquals(new BigDecimal("500.00"),
            calc.calculatePrincipal(new BigDecimal("500")));
    }

    @Test
    void calculateCommission_returns8_00() {
        assertEquals(new BigDecimal("8.00"),
            calc.calculateCommission(new BigDecimal("8.00")));
    }

    @Test
    void calculateCommission_scalesTo2Decimals() {
        assertEquals(new BigDecimal("8.00"),
            calc.calculateCommission(new BigDecimal("8")));
    }
}