package io.blotracer.transfer.reciept.config;

import org.apache.http.client.methods.HttpPost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsConfig {

    @Value("${block.tracer.geth.rpc.host}")
    private String rpcHost;

    @Value("${block.tracer.geth.rpc.port}")
    private String rpcPort;


    @Bean
    public RestHighLevelClient client(EsProperties properties) {
        return new RestHighLevelClient(
                RestClient.builder(properties.hosts())
        );
    }

    @Bean
    public HttpPost Rcp(EsProperties properties) {
        HttpPost request = new HttpPost("http://"+rpcHost+":"+rpcPort);
        request.setHeader("Content-type", "application/json");

        return request;
    }

}
