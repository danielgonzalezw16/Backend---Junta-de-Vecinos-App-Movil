package cl.duoc.junta.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import cl.duoc.junta.admin.dto.Dtos.*;
import cl.duoc.junta.admin.service.UsuarioService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody RegistroRequest request) {
        return service.registrar(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return service.login(request);
    }
}
