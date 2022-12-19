package kz.kcell.flow.assets.client;


import feign.HeaderMap;
import feign.Param;
import feign.RequestLine;
import kz.kcell.flow.assets.dto.JobRequestDto;
import kz.kcell.flow.assets.dto.JrPrDto;
import kz.kcell.flow.assets.dto.PurchaseRequestDto;

import java.util.List;
import java.util.Map;

public interface AssetsClient {

    @RequestLine("GET /job_request/jr_number/{jr_number}")
    JobRequestDto getJobRequestByJrNumber(@HeaderMap Map<String, String> headerMap,
                                          @Param("jr_number") String jrNumber);

    @RequestLine("POST /job_request")
    JobRequestDto createJobRequest(@HeaderMap Map<String, String> headerMap,
                                   JobRequestDto jobRequestDto);

    @RequestLine("GET /purchase_request/id/{id}")
    PurchaseRequestDto getPurchaseRequestByPrId(@HeaderMap Map<String, String> headerMap,
                                                @Param("id") Long id);

    @RequestLine("POST /purchase_request")
    PurchaseRequestDto createPurchaseRequest(@HeaderMap Map<String, String> headerMap,
                                             PurchaseRequestDto purchaseRequestDto);

    @RequestLine("GET /jr_pr/jr_id/{jr_id}")
    List<JrPrDto> getJrPrByJrId(@HeaderMap Map<String, String> headerMap,
                                @Param("jr_id") Long jrId);

    @RequestLine("POST /jr_pr")
    JrPrDto createJrPr(@HeaderMap Map<String, String> headerMap,
                       JrPrDto jrPrDto);

    @RequestLine("PUT /jr_pr/id/{id}")
    JrPrDto putJrPr(@HeaderMap Map<String, String> headerMap,
                    @Param("id") long id,
                    JrPrDto jrPrDto);
}
