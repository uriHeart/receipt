package io.blotracer.transfer.reciept.service;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class EsService {

    private RestHighLevelClient restHighLevelClient;

    @Autowired
    public EsService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Autowired
    RcpService rcpService;

    @Autowired
    BulkListener bulkListener;

    public Integer getMaxBlockNumber() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MaxAggregationBuilder aggregation = AggregationBuilders.max("max_block").field("success_block");
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.size(0);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("chk_receipt");
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Max max_block = searchResponse.getAggregations().get("max_block");

        return (int)max_block.getValue();
    }

    public void makeReceiptCheckByBulk(List<Integer> blockNumberList) throws IOException, InterruptedException {

        BulkProcessor.Listener listener = bulkListener.makeBulkListener();

        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
                (request, bulkListener) ->
                        restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
        BulkProcessor bulkProcessor =
                BulkProcessor.builder(bulkConsumer, listener).build();


        for(int blockNumber : blockNumberList){
            IndexRequest req = new IndexRequest("chk_receipt","check",String.valueOf(blockNumber))
                    .source(XContentType.JSON,"success_block",blockNumber);
            bulkProcessor.add(req);
        }

        bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);

    }
}
