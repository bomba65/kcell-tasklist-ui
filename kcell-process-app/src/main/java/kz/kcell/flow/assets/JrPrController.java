package kz.kcell.flow.assets;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jr_pr")
@Log
public class JrPrController {

    //TODO: implement asset REST api calls
    private final String assetsUrl;

    private JrPrController(@Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUrl) {
        this.assetsUrl = assetsUrl;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> createJrPr(@RequestBody String jrPr) throws Exception {
        return ResponseEntity.ok("");
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<String> putJrPr(@RequestBody String jrPr) throws Exception {
        return ResponseEntity.ok(jrPr);
    }

    @RequestMapping(value = "/jr_id/{jr_id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getByJrId(@PathVariable("jr_id") String jrId) throws Exception {
        return ResponseEntity.ok(String.format("{ \"id\": 1,\"jr_id\": %s, \"pr_id\": 1 }", jrId));
    }
}
