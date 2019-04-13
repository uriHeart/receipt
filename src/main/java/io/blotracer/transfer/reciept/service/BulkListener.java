package io.blotracer.transfer.reciept.service;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BulkListener {

    public BulkProcessor.Listener makeBulkListener(){
        return new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                int numberOfActions = request.numberOfActions();
                log.info("Executing bulk [{}] with {} requests",
                        executionId, numberOfActions);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {
                if (response.hasFailures()) {
                    log.info("Bulk [{}] executed with failures", executionId);
                } else {
                    log.info("Bulk [{}] completed in {} milliseconds",
                            executionId, response.getTook().getMillis());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  Throwable failure) {
                log.error("Failed to execute bulk", failure);
            }
        };
    }
}
