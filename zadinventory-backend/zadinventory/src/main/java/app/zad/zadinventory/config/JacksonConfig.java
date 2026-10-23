package app.zad.zadinventory.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.datatype.hibernate7.Hibernate7Module;

@Configuration
public class JacksonConfig {

    /**
     * Mantem a serializacao das entidades Hibernate sem substituir o JsonMapper
     * configurado automaticamente pelo Spring Boot (incluindo spring.jackson.*).
     */
    @Bean
    public JsonMapperBuilderCustomizer hibernateJacksonCustomizer() {
        return builder -> builder.addModule(
                new Hibernate7Module()
                        .configure(Hibernate7Module.Feature.FORCE_LAZY_LOADING, true)
                        .configure(Hibernate7Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true)
        );
    }
}
