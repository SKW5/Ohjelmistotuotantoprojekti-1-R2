import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TemperatureConverterTest {

    private final TemperatureConverter converter = new TemperatureConverter();

    @Test
    void fahrenheitToCelsius_convertsMultipleValues() {
        assertEquals(-17.7778, converter.fahrenheitToCelsius(0), 0.0001);
        assertEquals(0.0, converter.fahrenheitToCelsius(32), 0.0001);
        assertEquals(100.0, converter.fahrenheitToCelsius(212), 0.0001);
        assertEquals(37.0, converter.fahrenheitToCelsius(98.6), 0.0001);
    }

    @Test
    void celsiusToFahrenheit_convertsMultipleValues() {
        assertEquals(32.0, converter.celsiusToFahrenheit(0), 0.0001);
        assertEquals(212.0, converter.celsiusToFahrenheit(100), 0.0001);
        assertEquals(98.6, converter.celsiusToFahrenheit(37), 0.0001);
        assertEquals(-4.0, converter.celsiusToFahrenheit(-20), 0.0001);
    }

    @Test
    void isExtremeTemperature_detectsEdgeCases() {
        assertTrue(converter.isExtremeTemperature(-41));
        assertTrue(converter.isExtremeTemperature(51));
        assertFalse(converter.isExtremeTemperature(-40));
        assertFalse(converter.isExtremeTemperature(50));
        assertFalse(converter.isExtremeTemperature(20));
    }
}
