package io.blotracer.transfer.reciept.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@EnableScheduling
@Service
public class SchedulerService{
    static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    EsService esService;

    @Autowired
    ReceiptRest receiptRest;

    @Value("${target.block.count}")
    private Integer trtBlockCount;

    @Scheduled(cron = "0/1 * * * * ?")
    public String makeReceipt() throws IOException, InterruptedException {

         int blockNumber = esService.getMaxBlockNumber();

        log.warn("START BLOCK:" + String.valueOf(blockNumber));
        List<Integer> blockNumberList = new ArrayList<>();

        for(int i=0; i <=trtBlockCount; i++) {
            int trtBlockNumber = blockNumber+i;
            blockNumberList.add(trtBlockNumber);
        }

        HashMap param = new HashMap();
        param.put("start",String.valueOf(blockNumber+1));
        param.put("end",String.valueOf(blockNumber+trtBlockCount));

        receiptRest.createReceipt(param);

        esService.makeReceiptCheckByBulk(blockNumberList);
        return "OK";
    }
}
