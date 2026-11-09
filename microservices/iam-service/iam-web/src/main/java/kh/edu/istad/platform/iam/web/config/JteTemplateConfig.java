package kh.edu.istad.platform.iam.web.config;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.springframework.boot.autoconfigure.JteProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(JteProperties.class)
public class JteTemplateConfig {

    @Bean
    public TemplateEngine jteTemplateEngine(JteProperties properties) {
        if (properties.isUsePrecompiledTemplates()) {
            return TemplateEngine.createPrecompiled(ContentType.Html);
        }

        var templates = Path.of(properties.getTemplateLocation()).toAbsolutePath().normalize();
        var engine = TemplateEngine.create(
                new DirectoryCodeResolver(templates),
                Path.of(properties.getClassDirectory()),
                ContentType.Html,
                getClass().getClassLoader()
        );
        engine.setTrimControlStructures(properties.isTrimControlStructures());
        return engine;
    }
}
