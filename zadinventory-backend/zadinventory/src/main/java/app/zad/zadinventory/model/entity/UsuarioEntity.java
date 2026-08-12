package app.zad.zadinventory.model.entity;

import app.zad.zadinventory.model.enums.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email é obrigatório")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Tipo de usuário é obrigatório")
    private TipoUsuario tipoUsuario;

    @OneToMany(mappedBy = "usuario")
    @JsonIgnoreProperties({"usuario", "hibernateLazyInitializer", "handler"})
    private List<ProdutoEntity> produtos;

    private String nome;
}