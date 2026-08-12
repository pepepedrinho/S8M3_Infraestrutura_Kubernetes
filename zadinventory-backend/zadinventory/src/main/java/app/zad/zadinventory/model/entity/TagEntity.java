package app.zad.zadinventory.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "produtos"}) // Adicionado "produtos" aqui
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Nome da tag é obrigatório")
    private String nome;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnoreProperties({"tags", "hibernateLazyInitializer", "handler"})
    private List<ProdutoEntity> produtos = new ArrayList<>();
}