package kz.kcell.flow.assets;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job_request")
@Log
public class JobRequestController {

    //TODO: implement asset REST api calls
    private final String assetsUrl;

    private JobRequestController(@Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUrl) {
        this.assetsUrl = assetsUrl;
    }

    @RequestMapping(value = "/jr_number/{jr_number}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getByJrNumber(@PathVariable("jr_number") String jrNumber) throws Exception {
        if (jrNumber.equals("Alm-LSE-P&O-22-0017")) {
            return ResponseEntity.ok(String.format("{ \"id\": 1, \"jr_number\": \"%s\" }", jrNumber));
        } else {
            throw new RuntimeException(assetsUrl + " job request not found by number=" + jrNumber);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> createJr(@RequestBody String body) throws Exception {
        return ResponseEntity.ok("");
    }
}
