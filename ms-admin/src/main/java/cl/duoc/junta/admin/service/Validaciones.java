package cl.duoc.junta.admin.service;

import java.util.regex.Pattern;

import cl.duoc.junta.admin.error.ApiException;

/** Reglas de validación del servidor (espejo de las que aplica la app móvil). */
public final class Validaciones {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validaciones() {}

    public static String nombre(String valor) {
        String v = valor == null ? "" : valor.trim().replaceAll("\\s+", " ");
        if (v.length() < 3 || v.length() > 60) {
            throw ApiException.badRequest("El nombre debe tener entre 3 y 60 caracteres");
        }
        if (!v.contains(" ")) {
            throw ApiException.badRequest("Ingresa nombre y apellido");
        }
        return v;
    }

    public static String email(String valor) {
        String v = valor == null ? "" : valor.trim().toLowerCase();
        if (!EMAIL.matcher(v).matches()) {
            throw ApiException.badRequest("Formato de correo inválido");
        }
        return v;
    }

    /** Deja solo dígitos y K en mayúscula. */
    public static String normalizarRut(String valor) {
        if (valor == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : valor.toCharArray()) {
            if ((c >= '0' && c <= '9') || c == 'k' || c == 'K') sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }

    public static char calcularDv(String cuerpo) {
        int suma = 0;
        int mult = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * mult;
            mult = mult == 7 ? 2 : mult + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) return '0';
        if (resto == 10) return 'K';
        return (char) ('0' + resto);
    }

    public static String rut(String valor) {
        String r = normalizarRut(valor);
        if (r.length() < 7 || r.length() > 9) {
            throw ApiException.badRequest("El RUT no es válido");
        }
        String cuerpo = r.substring(0, r.length() - 1);
        if (!cuerpo.chars().allMatch(Character::isDigit) || calcularDv(cuerpo) != r.charAt(r.length() - 1)) {
            throw ApiException.badRequest("El dígito verificador del RUT no corresponde");
        }
        return r;
    }

    public static void clave(String valor) {
        boolean ok = valor != null && valor.length() >= 8
            && valor.chars().anyMatch(Character::isUpperCase)
            && valor.chars().anyMatch(Character::isLowerCase)
            && valor.chars().anyMatch(Character::isDigit);
        if (!ok) {
            throw ApiException.badRequest("La contraseña debe tener 8 caracteres, una mayúscula, una minúscula y un número");
        }
    }

    /** Celular chileno de 9 dígitos que parte con 9 (opcional: vacío devuelve null). */
    public static String telefono(String valor) {
        if (valor == null) return null;
        String d = valor.replaceAll("\\D", "");
        if (d.isEmpty()) return null;
        if (d.length() != 9 || d.charAt(0) != '9') {
            throw ApiException.badRequest("El celular debe tener 9 dígitos y comenzar con 9");
        }
        return d;
    }

    public static String textoOpcional(String valor, int max, String etiqueta) {
        if (valor == null || valor.isBlank()) return null;
        String v = valor.trim();
        if (v.length() > max) throw ApiException.badRequest(etiqueta + " admite máximo " + max + " caracteres");
        return v;
    }
}
