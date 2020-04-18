package com.example;

import com.informix.stream.api.IfmxStreamRecord;
import com.informix.stream.cdc.IfxCDCEngine;
import com.informix.stream.cdc.records.IfxCDCOperationRecord;
import com.informix.stream.impl.IfxStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class TableWatcher {

    private final Logger logger = LoggerFactory.getLogger(TableWatcher.class);
    private final DataSource dataSource;
    private final EventPublisher eventPublisher;
    private boolean active;

    public TableWatcher(DataSource dataSource, EventPublisher eventPublisher) {
        this.dataSource = dataSource;
        this.eventPublisher = eventPublisher;
    }

    public void activate() {
        Executors.newSingleThreadExecutor().execute(() -> {
            IfxCDCEngine.Builder builder = new IfxCDCEngine.Builder(dataSource)
                    .watchTable("test_database:informix.test_table", "some_name", "some_value")
                    .timeout(5)
                    .sequenceId(0);

            try (IfxCDCEngine engine = builder.build()) {
                engine.init();
                active = true;
                IfmxStreamRecord record;
                while ((record = engine.getRecord()) != null && active) {
                    logger.info("{}", record);
                    if (record.hasOperationData()) {
                        eventPublisher.publish(new CdcEvent(record.getSequenceId(), record.getTransactionId()));
                        logger.info("{}", ((IfxCDCOperationRecord) record).getData());
                    }
                }
            } catch (SQLException | IfxStreamException throwables) {
                logger.error(throwables.getMessage());
            }
        });

    }

    public void deactivate() {
        logger.info("Watcher deactivated");
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}
