package cl.duoc.junta.admin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import cl.duoc.junta.admin.error.ApiException;
import cl.duoc.junta.admin.service.Validaciones;

class ValidacionesTest {

    @Test
    void rutValidoSeNormaliza() {
        assertEquals("123456785", Validaciones.rut("12.345.678-5"));
    }

    @Test
    void rutConDigitoIncorrectoSeRechaza() {
        assertThrows(ApiException.class, () -> Validaciones.rut("12.345.678-9"));
    }

    @Test
    void claveDebeTenerMayusculaMinusculaYNumero() {
        assertDoesNotThrow(() -> Validaciones.clave("Demo1234"));
        assertThrows(ApiException.class, () -> Validaciones.clave("demo1234"));
        assertThrows(ApiException.class, () -> Validaciones.clave("Demo"));
    }

    @Test
    void emailSeNormalizaAMinusculas() {
        assertEquals("a@b.cl", Validaciones.email("  A@B.cl "));
        assertThrows(ApiException.class, () -> Validaciones.email("sin-arroba"));
    }

    @Test
    void telefonoOpcionalYDeNueveDigitos() {
        assertNull(Validaciones.telefono(""));
        assertEquals("912345678", Validaciones.telefono("9 1234 5678"));
        assertThrows(ApiException.class, () -> Validaciones.telefono("812345678"));
    }
}
