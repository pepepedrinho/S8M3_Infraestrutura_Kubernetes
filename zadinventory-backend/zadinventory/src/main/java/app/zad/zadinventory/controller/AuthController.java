package app.zad.zadinventory.controller;

import app.zad.zadinventory.config.JwtService;
import app.zad.zadinventory.controller.dto.LoginRequest;
import app.zad.zadinventory.controller.dto.LoginResponse;
import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.service.CustomUserDetailsService;
import app.zad.zadinventory.model.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService; // Use CustomUserDetailsService
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        System.out.println("📍 Entrou no método login()");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.senha()
                )
        );

        // CORREÇÃO: Use userDetailsService em vez de usuarioService
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        UsuarioEntity usuario = usuarioService.buscarPorEmail(request.email());
        String jwtToken = jwtService.generateToken(userDetails);

        System.out.println("Usuario: "+usuario.getEmail());
        System.out.println("Senha: "+usuario.getSenha());
        System.out.println("Token: "+jwtToken);

        return ResponseEntity.ok(new LoginResponse(
                jwtToken,
                usuario.getEmail(),
                usuario.getTipoUsuario().name()
        ));
    }
}