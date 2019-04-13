package io.blotracer.transfer.reciept.service;

import com.google.gson.Gson;
import io.blotracer.transfer.reciept.dto.RpcReqDto;
import io.blotracer.transfer.reciept.util.EthNumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.springframework.http.MediaType.APPLICATION_JSON;



@Slf4j
@RestController
public class ReceiptRest {

    private RestHighLevelClient restHighLevelClient;

    @Autowired
    BulkListener bulkListener;

    @Autowired
    RcpService rcpService;

    @Autowired
    RxWebClient webClient;

    @Autowired
    public ReceiptRest(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    //인덱스 생성 테스트
//    @PostMapping("/test")
//    public Mono<Map> getReceiptTest(@RequestBody Map<String, String> params){
//        RpcReqDto reqDto = new RpcReqDto();
//        reqDto.setMethod("eth_getTransactionReceipt");
//        Object[] param = {"0xafe90c462afb7dcfbe71cb60388110cc43e258cb3ecc0d97c0ac3130fdf69d46"};
//        reqDto.setParams(param);
//
//        Map<String,Object> pp = new HashMap<>();
//
//
//        pp.put("jsonrpc","2.0");
//        pp.put("id","1");
//        pp.put("method","eth_getTransactionReceipt");
//        pp.put("params",param);
//
//        return webClient.getWebClient().post().contentType(MediaType.APPLICATION_JSON).syncBody(pp).retrieve().bodyToMono(Map.class);
//    }

//    인덱스 생성 인서트 단건치리  성능비교를 위해 남겨둠
//    @PostMapping("/test2")
//    public void getReceiptTest2(@RequestBody Map<String, String> params){
//
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.size(100000);
//        searchSourceBuilder.fetchSource(new String[]{"to", "from", "hash", "blockNumber", "value", "timeLong"}, new String[]{""});
//
//
//        RangeQueryBuilder range = QueryBuilders.rangeQuery("blockNumber").gte(params.get("start")).lte(params.get("end"));
//        searchSourceBuilder.query(range);
//
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices("eth");
//        searchRequest.source(searchSourceBuilder);
//
//        ActionListener<SearchResponse> actionListener = new ActionListener<SearchResponse>() {
//            @Override
//            public void onResponse(SearchResponse searchResponse) {
//                log.info("total count : {}", searchResponse.getHits().getTotalHits());
//                log.info(searchRequest.toString());
//
//                int callCount=0;
//                for (SearchHit hit : searchResponse.getHits()) {
//                    callCount++;
//                    Object[] param = {hit.getSourceAsMap().get("hash")};
//                    Map<String, Object> pp = new HashMap<>();
//                    pp.put("jsonrpc", "2.0");
//                    pp.put("id", "1");
//                    pp.put("method", "eth_getTransactionReceipt");
//                    pp.put("params", param);
//                    //sink.next(client.post().contentType(APPLICATION_JSON).syncBody(pp).retrieve().bodyToFlux(HashMap.class));
//                    Mono<LinkedHashMap> data = webClient.getWebClient().post().contentType(APPLICATION_JSON).syncBody(pp).retrieve().bodyToMono(LinkedHashMap.class);
//                    data.flatMap( s -> changeEsData(s,Long.valueOf(hit.getSourceAsMap().get("timeLong").toString())))
//                    .doOnNext(s -> indexCreate(s))
//                    .doOnError(e -> esBeforeError(e,hit.getSourceAsMap().get("hash").toString()))
//                    .subscribe();
//                 }
//                log.info("total : {}",callCount);
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                log.error("elastic transaction data search exception {}",e);
//            }
//        };
//        restHighLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT,actionListener);
//     }
//
//    public void indexCreate(HashMap<String,Object> param){
//
//        IndexRequest request = new IndexRequest("test1","test",param.get("transactionHash").toString()).source(param);
//        request.opType("create");
//
//        ActionListener ac = new ActionListener<IndexResponse>() {
//            @Override
//            public void onResponse(IndexResponse indexResponse) {
//                //log.info(indexResponse.getId()+ ":"+indexResponse.status().toString());
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                ElasticsearchStatusException exception = (ElasticsearchStatusException) e;
//                if("CONFLICT".equals(exception.status().name())){
//                    log.info("Conflict "+exception.getMessage());
//                }else{
//                    log.info("exception: {}",exception);
//                }
//            }
//        };
//
//        restHighLevelClient.indexAsync(request, RequestOptions.DEFAULT,ac);
//     }
//
//    public Mono<HashMap<String,Object>> changeEsData(LinkedHashMap rpcData,Long timeLong){
//         LinkedHashMap rpcResult = (LinkedHashMap) rpcData.get("result");
//         HashMap<String,Object> insData = new HashMap<>();
//            insData.put("blockHash",rpcResult.get("blockHash"));
//            insData.put("blockNumber", EthNumberUtil.hexToNumber(rpcResult.get("blockNumber").toString()));
//            insData.put("contractAddress",rpcResult.get("contractAddress"));
//            insData.put("cumulativeGasUsed",rpcResult.get("cumulativeGasUsed"));
//            insData.put("from",rpcResult.get("from"));
//            insData.put("gasUsed",EthNumberUtil.hexToGasNumber(rpcResult.get("gasUsed").toString()));
//            insData.put("logs",rpcResult.get("logs"));
//            insData.put("logsBloom",rpcResult.get("logsBloom"));
//            insData.put("root",rpcResult.get("root"));
//            insData.put("status",EthNumberUtil.hexToNumber(rpcResult.get("status").toString()));
//            insData.put("to",rpcResult.get("to"));
//            insData.put("transactionHash",rpcResult.get("transactionHash"));
//            insData.put("transactionIndex",EthNumberUtil.hexToNumber(rpcResult.get("transactionIndex").toString()));
//            insData.put("timeLong",timeLong);
//
//        return Mono.just(insData);
//    }


    public void esBeforeError(Throwable t,String hash){
        HashMap<String,Object> insData = new HashMap<>();
        insData.put("hash",hash);
        insData.put("message",t.getMessage());

        IndexRequest request = new IndexRequest("error_receipt","data",hash).source(insData);
        request.opType("create");
        ActionListener ac = new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                log.info(indexResponse.toString());
            }

            @Override
            public void onFailure(Exception e) {
                log.info(e.toString());

            }
        };

        restHighLevelClient.indexAsync(request, RequestOptions.DEFAULT,ac);
     }


    @PostMapping("/make/receipt")
    public Disposable createReceipt(@RequestBody Map<String, String> params){

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(100000);
        searchSourceBuilder.fetchSource(new String[]{"hash","timeLong"}, new String[]{""});

        RangeQueryBuilder range = QueryBuilders.rangeQuery("blockNumber").gte(params.get("start")).lte(params.get("end"));
        searchSourceBuilder.query(range);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("eth");
        searchRequest.source(searchSourceBuilder);

        return Flux.push( synchronousSink -> {
            ActionListener<SearchResponse> actionListener = new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {

                    log.info(searchRequest.toString());
                    for (SearchHit s :searchResponse.getHits()){
                        synchronousSink.next(s.getSourceAsMap());
                    }

                    synchronousSink.complete();
                }

                @Override
                public void onFailure(Exception e) {
                    log.error("elastic transaction data search exception {}",e);
                }
            };
            restHighLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT,actionListener);
        })
        .flatMap( s -> {
            HashMap hit = (HashMap)s;
            Object[] param = {hit.get("hash")};
            Map<String, Object> rpcPram = new HashMap<>();
            rpcPram.put("jsonrpc", "2.0");
            rpcPram.put("id", "1");
            rpcPram.put("method", "eth_getTransactionReceipt");
            rpcPram.put("params", param);
            return webClient.getWebClient().post().contentType(APPLICATION_JSON).syncBody(rpcPram).retrieve().bodyToFlux(LinkedHashMap.class)
                    .flatMap( rpcData ->{
                        LinkedHashMap source = (LinkedHashMap)rpcData.get("result");
                        source.put("blockNumber", EthNumberUtil.hexToNumber((String) source.get("blockNumber")));
                        source.put("gasUsed",EthNumberUtil.hexToGasNumber((String) source.get("gasUsed")));
                        source.put("status",EthNumberUtil.hexToNumber((String) source.get("status")));
                        source.put("transactionIndex",EthNumberUtil.hexToNumber((String) source.get("transactionIndex")));
                        source.put("timeLong",hit.get("timeLong"));
                        return Flux.just(source);
                     })
                    .doOnError(t ->esBeforeError(t,hit.get("hash").toString()));
        })
        .collectList()
        .doOnNext(s ->{
            List<LinkedHashMap> hits = (List<LinkedHashMap>)s;

            BulkProcessor.Listener listener = bulkListener.makeBulkListener();
            BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
                    (request, bulkListener) ->
                            restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);

            BulkProcessor bulkProcessor =
                    BulkProcessor.builder(bulkConsumer, listener).build();

            for(LinkedHashMap hit : hits){
                IndexRequest req = new IndexRequest("receipt", "data", hit.get("transactionHash").toString())
                        .source(new Gson().toJson(hit), XContentType.JSON);
                bulkProcessor.add(req);
            }
            try {
                bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).subscribe();
    }
}
