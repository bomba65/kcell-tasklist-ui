package kz.kcell.flow.assets;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase_request")
@Log
public class PurchaseRequestController {

    //TODO: implement asset REST api calls

    private final String assetsUrl;

    private PurchaseRequestController(@Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUrl) {
        this.assetsUrl = assetsUrl;
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getByPrId(@PathVariable("id") Long id) throws Exception {
        if (id == 1) {
            return ResponseEntity.ok(String.format("{ \"id\": %s, \"pr_number\": \"12345678\" }", id));
        } else {
            throw new RuntimeException(assetsUrl + "purchase request not found by id=" + id);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> createPr(@RequestBody String body) throws Exception {
        return ResponseEntity.ok("");
    }
}
