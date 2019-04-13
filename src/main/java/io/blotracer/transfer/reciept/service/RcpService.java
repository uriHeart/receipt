package io.blotracer.transfer.reciept.service;

import com.google.gson.Gson;
import io.blotracer.transfer.reciept.dto.RpcReqDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class RcpService {

    @Value("${block.tracer.geth.rpc.host}")
    private String rpcHost;

    @Value("${block.tracer.geth.rpc.port}")
    private String rpcPort;

    private HttpPost request;

    @Autowired
    RcpService(HttpPost request){this.request = request;};

    public <T> T callParityRpc(RpcReqDto params, Class<T> classOfT) throws IOException {
        T result = null;
        CloseableHttpResponse httpResponse =null;
        try{
            StringEntity rpcParams =new StringEntity(new Gson().toJson(params));

            request.setHeader("Content-type", "application/json");
            request.setEntity(rpcParams);

            //log.info(String.valueOf(request.hashCode()));

            CloseableHttpClient httpClient = HttpClients.createDefault();
            httpResponse = httpClient.execute(request);

            String rpcResult = EntityUtils.toString(httpResponse.getEntity()).replace("\n","");

            result =  new Gson().fromJson(rpcResult,classOfT);
        }catch(Exception e){
            log.info(e.getMessage());

        }


        return result;
    }
}
