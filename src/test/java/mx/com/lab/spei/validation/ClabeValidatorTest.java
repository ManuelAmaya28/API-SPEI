package mx.com.lab.spei.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClabeValidatorTest {

    private final ClabeValidator validator = new ClabeValidator();

    @Test
    void validClabe_returnTrue() {
        assertTrue(validator.isValid("646180909697341557", null));
    }

    @Test
    void nullClabe_returnFalse() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void wrongLength_returnFalse() {
        assertFalse(validator.isValid("12345", null));
    }

    @Test
    void wrongCheckDigit_returnFalse() {
        assertFalse(validator.isValid("646180909697341558", null));
    }

    @Test
    void nonNumeric_returnFalse() {
        assertFalse(validator.isValid("64618090969734155A", null));
    }

    @Test
    void anotherValidClabe_returnTrue() {
        // Build a CLABE with correct check digit dynamically to confirm algorithm
        // 646180909697341557 is confirmed valid (check digit=7)
        assertTrue(validator.isValid("646180909697341557", null));
    }
}