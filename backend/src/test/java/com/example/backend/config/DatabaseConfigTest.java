package com.example.backend.config;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

class DatabaseConfigTest {

    private DatabaseConfig databaseConfig;
    private EntityManagerFactoryBuilder builder;

    @BeforeEach
    void setUp() {
        databaseConfig = new DatabaseConfig();
        builder = new EntityManagerFactoryBuilder(mock(JpaVendorAdapter.class), Map.of(), null);
    }

    @Test
    void sqlServer1DataSource_is_created() {
        assertNotNull(databaseConfig.sqlServer1DataSource());
    }

    @Test
    void sqlServer2DataSource_is_created() {
        assertNotNull(databaseConfig.sqlServer2DataSource());
    }

    @Test
    void sqlServer1EntityManagerFactory_is_configured() {
        DataSource dataSource = mock(DataSource.class);

        LocalContainerEntityManagerFactoryBean factory = databaseConfig
            .sqlServer1EntityManagerFactory(builder, dataSource);

        assertNotNull(factory);
        assertSame(dataSource, factory.getDataSource());
        assertEquals("sqlserver1", factory.getPersistenceUnitName());
        assertEquals("org.hibernate.dialect.SQLServerDialect", factory.getJpaPropertyMap().get("hibernate.dialect"));
        assertEquals("validate", factory.getJpaPropertyMap().get("hibernate.hbm2ddl.auto"));
        assertEquals(true, factory.getJpaPropertyMap().get("hibernate.show_sql"));
    }

    @Test
    void sqlServer2EntityManagerFactory_is_configured() {
        DataSource dataSource = mock(DataSource.class);

        LocalContainerEntityManagerFactoryBean factory = databaseConfig
            .sqlServer2EntityManagerFactory(builder, dataSource);

        assertNotNull(factory);
        assertSame(dataSource, factory.getDataSource());
        assertEquals("sqlserver2", factory.getPersistenceUnitName());
        assertEquals("org.hibernate.dialect.SQLServerDialect", factory.getJpaPropertyMap().get("hibernate.dialect"));
        assertEquals("validate", factory.getJpaPropertyMap().get("hibernate.hbm2ddl.auto"));
        assertEquals(true, factory.getJpaPropertyMap().get("hibernate.show_sql"));
    }

    @Test
    void sqlServer1TransactionManager_is_created_for_entityManagerFactory() {
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);

        PlatformTransactionManager transactionManager = databaseConfig
            .sqlServer1TransactionManager(entityManagerFactory);

        assertInstanceOf(JpaTransactionManager.class, transactionManager);
        assertSame(entityManagerFactory, ((JpaTransactionManager) transactionManager).getEntityManagerFactory());
    }

    @Test
    void sqlServer2TransactionManager_is_created_for_entityManagerFactory() {
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);

        PlatformTransactionManager transactionManager = databaseConfig
            .sqlServer2TransactionManager(entityManagerFactory);

        assertInstanceOf(JpaTransactionManager.class, transactionManager);
        assertSame(entityManagerFactory, ((JpaTransactionManager) transactionManager).getEntityManagerFactory());
    }

    @Test
    void databaseConfig_has_expected_annotations() {
        assertNotNull(DatabaseConfig.class.getAnnotation(Configuration.class));
        assertArrayEquals(new String[] {"!test"}, DatabaseConfig.class.getAnnotation(Profile.class).value());
        assertNotNull(DatabaseConfig.class.getAnnotation(org.springframework.transaction.annotation.EnableTransactionManagement.class));
    }

    @Test
    void dataSources_have_expected_bean_annotations() throws NoSuchMethodException {
        assertNotNull(DatabaseConfig.class.getMethod("sqlServer1DataSource").getAnnotation(Primary.class));
        assertNotNull(DatabaseConfig.class.getMethod("sqlServer1DataSource")
            .getAnnotation(org.springframework.boot.context.properties.ConfigurationProperties.class));
        assertNotNull(DatabaseConfig.class.getMethod("sqlServer2DataSource")
            .getAnnotation(org.springframework.boot.context.properties.ConfigurationProperties.class));
    }

    @Test
    void entityManagerFactory_methods_have_primary_only_on_server1() throws NoSuchMethodException {
        assertNotNull(DatabaseConfig.class.getMethod("sqlServer1EntityManagerFactory", EntityManagerFactoryBuilder.class,
            DataSource.class).getAnnotation(Primary.class));
        assertEquals(null, DatabaseConfig.class.getMethod("sqlServer2EntityManagerFactory", EntityManagerFactoryBuilder.class,
            DataSource.class).getAnnotation(Primary.class));
    }

    @Test
    void repository_configs_have_expected_packages_and_references() {
        EnableJpaRepositories server1 = SQLServer1RepositoryConfig.class.getAnnotation(EnableJpaRepositories.class);
        EnableJpaRepositories server2 = SQLServer2RepositoryConfig.class.getAnnotation(EnableJpaRepositories.class);

        assertEquals("com.example.backend.sqlserver1.repository", server1.basePackages()[0]);
        assertEquals("sqlServer1EntityManagerFactory", server1.entityManagerFactoryRef());
        assertEquals("sqlServer1TransactionManager", server1.transactionManagerRef());
        assertEquals("com.example.backend.sqlserver2.repository", server2.basePackages()[0]);
        assertEquals("sqlServer2EntityManagerFactory", server2.entityManagerFactoryRef());
        assertEquals("sqlServer2TransactionManager", server2.transactionManagerRef());
    }
}