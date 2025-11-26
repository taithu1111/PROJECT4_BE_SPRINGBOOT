package phamiz.ecommerce.backend;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner testDataSource(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("DB connection OK: " + conn.getMetaData().getURL());

                // FIX: Alter table user to ensure ID is AUTO_INCREMENT
                // Commented out to prevent blocking application startup
                // try (java.sql.Statement stmt = conn.createStatement()) {
                // System.out.println("Attempting to fix user table schema...");
                // stmt.execute("ALTER TABLE user MODIFY id BIGINT AUTO_INCREMENT");
                // System.out.println("✓ Successfully added AUTO_INCREMENT to user.id");
                // } catch (Exception e) {
                // System.out.println("⚠ Schema fix warning (might already be fixed): " +
                // e.getMessage());
                // }

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
