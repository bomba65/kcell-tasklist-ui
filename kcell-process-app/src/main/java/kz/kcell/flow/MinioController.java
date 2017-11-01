package kz.kcell.flow;

import io.minio.MinioClient;
import io.minio.errors.*;
import kz.kcell.flow.minio.Minio;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/uploads")
public class MinioController {

    private static final Logger log = LoggerFactory.getLogger(MinioController.class);
    private final String minioAccessKey;
    private final String minioSecretKey;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private Minio minio;

    @Autowired
    public MinioController(@Value("${minio.access.key:AKIAIOSFODNN7EXAMPLE}") String minioAccessKey,
                 @Value("${minio.secret.key:wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY}") String minioSecretKey)
        throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException,
        InsufficientDataException, NoResponseException, InvalidBucketNameException, XmlPullParserException, InternalException,
        RegionConflictException, ErrorResponseException {

        this.minioAccessKey = minioAccessKey;
        this.minioSecretKey = minioSecretKey;
    }

    @RequestMapping(value = "/get/{processId}/{taskId}/{fileName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getPresignedGetObjectUrl(@PathVariable("processId") String processId, @PathVariable("taskId") String taskId, @PathVariable("fileName") String fileName, HttpServletRequest request) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

        if (processId == null || taskId == null || fileName == null) {
            log.debug("No Process Instance, Task or File specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Process Instance, Task or File specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.debug("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        MinioClient minioClient = new MinioClient(getLocation(request), this.minioAccessKey, this.minioSecretKey, "us-east-1");

        String url = minioClient.presignedGetObject(minio.getBucketName(), processId + "/" + taskId + "/" + fileName, 60 * 60 * 1); // 1 hour

        return ResponseEntity.ok(url);
    }

    @RequestMapping(value = "/process/get/{processId}/{fileName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getTempPresignedGetObjectUrl(@PathVariable("processId") String processId, @PathVariable("fileName") String fileName, HttpServletRequest request) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

        if (processId == null || fileName == null) {
            log.debug("No processId or File specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No processId or File specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.debug("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        MinioClient minioClient = new MinioClient(getLocation(request), this.minioAccessKey, this.minioSecretKey, "us-east-1");

        String url = minioClient.presignedGetObject(minio.getBucketName(), processId + "/" + fileName, 60 * 60 * 1); // 1 hour

        return ResponseEntity.ok(url);
    }

    @RequestMapping(value = "/put/{processId}/{taskId}/{fileName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getPresignedPutObjectUrl(@PathVariable("processId") String processId, @PathVariable("taskId") String taskId, @PathVariable("fileName") String fileName, HttpServletRequest request) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

        if (processId == null || taskId == null || fileName == null) {
            log.debug("No Process Instance, Task or File specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Process Instance, Task or File specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.debug("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        boolean taskBelongsToProcessInstance = taskService.createTaskQuery()
                .taskId(taskId)
                .processInstanceId(processId)
                .count() > 0;

        if (!taskBelongsToProcessInstance) {
            log.debug("The Task not found or does not belong to the Process Instance");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The Task not found or does not belong to the Process Instance");
        }

        boolean taskAssignedToCurrentUser = taskService.getIdentityLinksForTask(taskId)
                .stream()
                .filter(link -> link.getType().equals("assignee"))
                .filter(link -> link.getUserId().equals(identityService.getCurrentAuthentication().getUserId()))
                .count() > 0;

        if (!taskAssignedToCurrentUser) {
            log.debug("The User is not authorized to upload Files to the Task");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("The User is not authorized to upload Files to the Task");
        }

        MinioClient minioClient = new MinioClient(getLocation(request), this.minioAccessKey, this.minioSecretKey, "us-east-1");

        String url = minioClient.presignedPutObject(minio.getBucketName(), processId + "/" + taskId + "/" + fileName, 60 * 60 * 1);

        return ResponseEntity.ok(url);
    }

    @RequestMapping(value = "/tmp/put/{uuid}/{fileName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getTempPresignedPutObjectUrl(@PathVariable("uuid") String uuid, @PathVariable("fileName") String fileName, HttpServletRequest request) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

        if (uuid == null || fileName == null) {
            log.debug("No Uuid or File specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Uuid or File specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.debug("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        MinioClient minioClient = new MinioClient(getLocation(request), this.minioAccessKey, this.minioSecretKey, "us-east-1");

        String url = minioClient.presignedPutObject(minio.getTempBucketName(), uuid + "/" + fileName, 60 * 60 * 1);

        return ResponseEntity.ok(url);
    }

    private String getLocation(HttpServletRequest request){
        
        String location = request.getRequestURL().toString().replace(request.getRequestURI(), "");

        if("https".equals(request.getHeader("X-Forwarded-Proto")) && !location.contains("https://")){
            location = location.replace("http://", "https://");
        }

        return location;
    }
}
