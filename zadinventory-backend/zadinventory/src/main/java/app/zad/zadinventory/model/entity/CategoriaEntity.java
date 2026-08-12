package app.zad.zadinventory.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categorias")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "produtos"}) // Adicionado "produtos" aqui
public class CategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "O campo nome é obrigatório")
    private String nome;

    @Column
    private String descricao;

    @OneToMany(
            mappedBy = "categoria",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<ProdutoEntity> produtos = new ArrayList<>();

    public void adicionarProduto(ProdutoEntity produto) {
        produtos.add(produto);
        produto.setCategoria(this);
    }

    public void removerProduto(ProdutoEntity produto) {
        produtos.remove(produto);
        produto.setCategoria(null);
    }
}