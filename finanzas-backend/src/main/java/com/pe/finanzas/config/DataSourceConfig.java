package com.pe.finanzas.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/clinica_db}")
    private String defaultUrl;

    @Value("${spring.datasource.username:postgres}")
    private String defaultUsername;

    @Value("${spring.datasource.password:123456eli}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        String customUrl = System.getenv("SPRING_DATASOURCE_URL");

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);

        if (customUrl != null && !customUrl.isBlank()) {
            log.info("Configurando DataSource desde SPRING_DATASOURCE_URL");
            config.setJdbcUrl(customUrl);
            config.setUsername(System.getenv("SPRING_DATASOURCE_USERNAME") != null 
                    ? System.getenv("SPRING_DATASOURCE_USERNAME") : defaultUsername);
            config.setPassword(System.getenv("SPRING_DATASOURCE_PASSWORD") != null 
                    ? System.getenv("SPRING_DATASOURCE_PASSWORD") : defaultPassword);
        } else if (databaseUrl != null && !databaseUrl.isBlank() 
                && (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://"))) {
            log.info("Configurando DataSource automáticamente desde DATABASE_URL (Render)");
            try {
                URI uri = new URI(databaseUrl);
                String userInfo = uri.getUserInfo();
                String username = defaultUsername;
                String password = defaultPassword;
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String host = uri.getHost();
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                    jdbcUrl += "?" + uri.getQuery();
                }
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                log.info("Conectando a base de datos PostgreSQL en host: {}, puerto: {}", host, port);
            } catch (Exception e) {
                log.error("Error al procesar DATABASE_URL, recurriendo a configuración por defecto", e);
                config.setJdbcUrl(defaultUrl);
                config.setUsername(defaultUsername);
                config.setPassword(defaultPassword);
            }
        } else {
            log.info("Usando configuración de DataSource local/por defecto: {}", defaultUrl);
            config.setJdbcUrl(defaultUrl);
            config.setUsername(defaultUsername);
            config.setPassword(defaultPassword);
        }

        // Ajustes de resiliencia HikariCP para entornos cloud (robusto ante picos concurrentes)
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 segundos
        config.setIdleTimeout(300000);      // 5 minutos
        config.setMaxLifetime(600000);      // 10 minutos
        config.setLeakDetectionThreshold(10000); // Alerta si una conexion tarda mas de 10s

        return new HikariDataSource(config);
    }
}

