package app.zad.zadinventory.model.entity;

import app.zad.zadinventory.model.enums.Situacao;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "operacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OperacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id")
    @JsonIgnoreProperties({"operacoes", "categoria", "usuario", "tags", "hibernateLazyInitializer", "handler"})
    @NotNull(message = "Produto é obrigatório")
    private ProdutoEntity produto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({"produtos", "operacoes", "hibernateLazyInitializer", "handler"})
    @NotNull(message = "Usuário é obrigatório")
    private UsuarioEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Situação é obrigatória")
    private Situacao situacao;

    private LocalDate diaOperacao;

    @NotNull(message = "Quantidade obrigatoria!")
    private Integer quantidade;

    private BigDecimal valorTotal;
}