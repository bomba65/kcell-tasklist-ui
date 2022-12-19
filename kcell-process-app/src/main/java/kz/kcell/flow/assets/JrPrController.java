package kz.kcell.flow.assets;

import feign.FeignException;
import kz.kcell.flow.assets.client.AssetsClient;
import kz.kcell.flow.assets.dto.JrPrDto;
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

import java.util.Objects;

@RestController
@RequestMapping("/jr_pr")
@RequiredArgsConstructor
public class JrPrController {

    private final AssetsClient assetsClient;

    @RequestMapping(value = "/jr_id/{jr_id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<JrPrDto> getByJrId(@RequestHeader(value = HttpHeaders.REFERER, required = false) String referer,
                                             @PathVariable("jr_id") Long jrId) {
        try {
            JrPrDto content = assetsClient.getJrPrByJrId(new SingletonMap<>(HttpHeaders.REFERER, referer), jrId)
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
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
