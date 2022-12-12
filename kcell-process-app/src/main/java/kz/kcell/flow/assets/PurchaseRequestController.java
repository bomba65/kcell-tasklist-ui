package kz.kcell.flow.assets;

import feign.FeignException;
import kz.kcell.flow.assets.client.AssetsClient;
import kz.kcell.flow.assets.dto.PurchaseRequestDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.map.SingletonMap;
import org.apache.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/purchase_request")
@RequiredArgsConstructor
public class PurchaseRequestController {

    private final AssetsClient assetsClient;

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<PurchaseRequestDto> getByPrId(@RequestHeader(value = HttpHeaders.REFERER, required = false) String referer,
                                                        @PathVariable("id") Long id) throws Exception {
        try {
            PurchaseRequestDto content = assetsClient.getPurchaseRequestByPrId(new SingletonMap<>(HttpHeaders.REFERER, referer), id);
            return ResponseEntity.ok(content);
        } catch (FeignException e) {
            if (e.status() == 404) {
                return ResponseEntity.status(e.status()).build();
            } else {
                throw e;
            }
        }
    }
}
