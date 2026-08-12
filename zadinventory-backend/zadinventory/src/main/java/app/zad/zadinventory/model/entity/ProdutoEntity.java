package app.zad.zadinventory.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Nome do produto é obrigatório")
    private String nome;

    private String descricao;

    @Min(value = 0, message = "Quantidade não pode ser negativa")
    private Integer quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Preço é obrigatório")
    @Min(value = 0, message = "Preço não pode ser negativo")
    private BigDecimal preco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonIgnoreProperties({"produtos", "hibernateLazyInitializer", "handler"})
    @NotNull(message = "Categoria é obrigatória")
    private CategoriaEntity categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"produtos", "operacoes", "hibernateLazyInitializer", "handler"})
    @NotNull(message = "Usuário é obrigatório")
    private UsuarioEntity usuario;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "produto_tag",
            joinColumns = @JoinColumn(name = "produto_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties({"produtos", "hibernateLazyInitializer", "handler"})
    private List<TagEntity> tags = new ArrayList<>();
}
