package com.example;

import org.junit.After;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = TestcontainersInformixTest.Initializer.class)
class TestcontainersInformixTest {


    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            Integer mappedPort = ifxContainer.getMappedPort(9088);
            String jdbcUrl = MessageFormat.format("spring.datasource.url=jdbc:informix-sqli://localhost:{0,number,#}/syscdcv1:INFORMIXSERVER=informix", mappedPort);
            TestPropertyValues.of(jdbcUrl).applyTo(applicationContext.getEnvironment());
        }
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TableWatcher tableWatcher;

    @MockBean
    EventPublisher eventPublisher;

    @Container
    private static GenericContainer ifxContainer = new GenericContainer<>("ibmcom/informix-developer-database:latest")
            .withExposedPorts(9088)
            .withEnv("LICENSE", "accept")
            .withEnv("INIT_FILE", "cdcdata-schema.sql")
            .withEnv("RUN_FILE_POST_INIT", "activate_cdc.sh")
            .withCopyFileToContainer(MountableFile.forClasspathResource("cdcdata-schema.sql"), "/opt/ibm/config/")
            .withCopyFileToContainer(MountableFile.forClasspathResource("activate_cdc.sh"), "/opt/ibm/config/")
            .waitingFor(Wait.forLogMessage(".*Maximum server connections 1.*", 1));


    @Test
    @DisplayName("syscdc database should be created by syscdcv1.sql script")
    void syscdcDatabaseShouldExists() {
        assertTrue(cdcIsActive());
    }

    private boolean cdcIsActive() {
        return JdbcTestUtils.countRowsInTable(jdbcTemplate, "syscdcvers") > 0;
    }

    @Test
    public void shouldPublishCdcEventOnTableUpdate(){
        //given
        tableWatcherIsActive();
        //when
        tableIsUpdated();
        //and
        waitedOneSecond();
        //then
        updateEventShouldBePublishedOnce();
    }

    private void waitedOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    void stopWatcher() {
        tableWatcher.deactivate();
    }
    private void updateEventShouldBePublishedOnce() {
        verify(eventPublisher, times(1)).publish(any());
    }

    private void tableWatcherIsActive() {
        tableWatcher.activate();
        await().atMost(5, TimeUnit.SECONDS).until(() -> tableWatcher.isActive());
    }

    private void tableIsUpdated() {
        jdbcTemplate.execute("INSERT INTO test_database:test_table VALUES('test', 1111)");
    }
}