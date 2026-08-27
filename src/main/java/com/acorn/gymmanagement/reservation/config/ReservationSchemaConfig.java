package com.acorn.gymmanagement.reservation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class ReservationSchemaConfig implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        if (!tableExists("users")
                || !tableExists("members")
                || !tableExists("trainers")) {
            return;
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("db/reservation-schema.sql")
        );
        populator.execute(dataSource);
    }

    private boolean tableExists(String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM " + tableName + " WHERE 1 = 0"
             )) {
            statement.executeQuery();
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }
}
