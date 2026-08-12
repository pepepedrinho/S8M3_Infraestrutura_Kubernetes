package app.zad.zadinventory.model.service;

import app.zad.zadinventory.model.entity.UsuarioEntity;
import app.zad.zadinventory.model.enums.TipoUsuario;
import app.zad.zadinventory.model.exception.RegraNegocioException;
import app.zad.zadinventory.model.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public UsuarioEntity salvar(UsuarioEntity usuario) {
        validarUsuario(usuario);

        usuario.setEmail(usuario.getEmail());
        String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);

        return repository.save(usuario);
    }

    public List<UsuarioEntity> buscarTodos() {
        return repository.findAll();
    }

    public UsuarioEntity buscarPorId(Long id) {
        return repository.findComProdutosById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado!"));
    }

    public UsuarioEntity buscarPorEmail(String email) {
        return repository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado!"));
    }

    public List<UsuarioEntity> buscarPorTipo(TipoUsuario tipo) {
        return repository.findByTipoUsuario(tipo);
    }

    public List<UsuarioEntity> buscarPorTipoOrdenado(TipoUsuario tipo) {
        return repository.buscarPorTipoOrdenado(tipo);
    }

    public UsuarioEntity buscarPorEmailESenha(String email, String senha) {
        return repository.findByEmailAndSenha(email, senha)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @Transactional
    public UsuarioEntity atualizar(Long id, UsuarioEntity usuarioAtualizado) {
        UsuarioEntity usuarioExistente = buscarPorId(id);
        usuarioExistente.setEmail(usuarioAtualizado.getEmail().toLowerCase());
        usuarioExistente.setTipoUsuario(usuarioAtualizado.getTipoUsuario());

        if (usuarioAtualizado.getSenha() != null && !usuarioAtualizado.getSenha().isBlank()) {
            // Por enquanto, não criptografa
            usuarioExistente.setSenha(usuarioAtualizado.getSenha());
        }

        return repository.save(usuarioExistente);
    }

    @Transactional
    public void remover(Long id) {
        repository.deleteById(id);
    }

    private void validarUsuario(UsuarioEntity usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new RegraNegocioException("Email é obrigatório!");
        }
        if (usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
            throw new RegraNegocioException("Senha é obrigatória!");
        }
        if (usuario.getTipoUsuario() == null) {
            throw new RegraNegocioException("Tipo de usuário é obrigatório!");
        }

        if (repository.existsByEmail(usuario.getEmail().toLowerCase())) {
            throw new RegraNegocioException("Email já está em uso!");
        }
    }
}