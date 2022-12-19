package kz.kcell.flow.dismantleReplace;

import kz.kcell.flow.dismantleReplace.dto.StatisticsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dismantle-replace")
@RequiredArgsConstructor
public class DismantleReplaceController {
    private final StatisticsService statisticsService;

    @RequestMapping(value = "/statistics", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource> getStatistics(@RequestBody List<StatisticsRequest> request) throws Exception {
        ByteArrayResource res = new ByteArrayResource(statisticsService.generateStatistics(request));
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(res);
    }
}
