package app.zad.zadinventory.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Adiciona o suporte ao Hibernate ao ObjectMapper do Spring Boot.
     *
     * <p>Antes esta classe declarava um {@code @Bean ObjectMapper} próprio. Isso fazia
     * o Boot desistir do {@code JacksonAutoConfiguration} e, na prática, <b>todas</b> as
     * chaves {@code spring.jackson.*} do application.properties viravam configuração
     * morta (o {@code default-property-inclusion=non_null} não tinha efeito nenhum).
     * Customizando o builder, o autoconfig continua ativo e o properties volta a valer.
     *
     * <p>JavaTimeModule e a desativação de WRITE_DATES_AS_TIMESTAMPS já vêm do próprio
     * Spring Boot, por isso não são registrados aqui.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer hibernateJacksonCustomizer() {
        return builder -> builder.modulesToInstall(
                new Hibernate6Module()
                        .configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, true)
                        .configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true)
        );
    }
}
