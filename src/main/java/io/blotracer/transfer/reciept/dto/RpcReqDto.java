package io.blotracer.transfer.reciept.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RpcReqDto {

    String jsonrpc ="2.0";
    String id ="1";
    String method;
    Object[] params;
    String result;


}
