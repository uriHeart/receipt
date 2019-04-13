package io.blotracer.transfer.reciept.service;

import lombok.Getter;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Getter
@Component
public class RxWebClient {

    @Value("${block.tracer.geth.rpc.host}")
    private String rpcHost;

    @Value("${block.tracer.geth.rpc.port}")
    private String rpcPort;

    private WebClient webClient;

    @Bean
    public void webClient(){

        HttpClient httpClient = HttpClient.create(ConnectionProvider.newConnection());
        this.webClient =  WebClient.builder()
                .baseUrl("http://"+rpcHost+":"+rpcPort)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
     }
}
