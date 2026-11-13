package kh.edu.istad.platform.iam.web.config;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.resolve.ResourceCodeResolver;
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

        String location = properties.getTemplateLocation();
        String classpathPrefix = "classpath:";
        CodeResolver resolver = location.startsWith(classpathPrefix)
                ? new ResourceCodeResolver(location.substring(classpathPrefix.length()), getClass().getClassLoader())
                : new DirectoryCodeResolver(Path.of(location).toAbsolutePath().normalize());
        var engine = TemplateEngine.create(
                resolver,
                Path.of(properties.getClassDirectory()),
                ContentType.Html,
                getClass().getClassLoader()
        );
        engine.setTrimControlStructures(properties.isTrimControlStructures());
        return engine;
    }
}
