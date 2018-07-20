package kz.kcell.flow.files;

import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/uploads")
@Log
public class MinioController {

    private final String minioAccessKey;
    private final String minioSecretKey;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private Minio minio;

    @Autowired
    public MinioController(@Value("${minio.access.key:AKIAIOSFODNN7EXAMPLE}") String minioAccessKey, @Value("${minio.secret.key:wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY}") String minioSecretKey) {
        this.minioAccessKey = minioAccessKey;
        this.minioSecretKey = minioSecretKey;
    }

    @RequestMapping(value = "/get/{processId}/{taskId}/{fileName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getPresignedGetObjectUrl(@PathVariable("processId") String processId, @PathVariable("taskId") String taskId, @PathVariable("fileName") String fileName, HttpServletRequest request) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

        if (processId == null || taskId == null || fileName == null) {
            log.warning("No Process Instance, Task or File specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Process Instance, Task or File specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        MinioClient minioClient = new MinioClient(getLocation(request), this.minioAccessKey, this.minioSecretKey, "us-east-1");

        String url = minioClient.presignedGetObject(minio.getBucketName(), processId + "/" + taskId + "/" + fileName, 60 * 60 * 1); // 1 hour

        return ResponseEntity.ok(url);
    }

    @RequestMapping(value = "/get/{processId}/{fileName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getPresignedGetObjectUrl(@PathVariable("processId") String processId, @PathVariable("fileName") String fileName, HttpServletRequest request) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

        if (processId == null || fileName == null) {
            log.warning("No processId or File specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No processId or File specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        MinioClient minioClient = new MinioClient(getLocation(request), this.minioAccessKey, this.minioSecretKey, "us-east-1");

        String url = minioClient.presignedGetObject(minio.getBucketName(), processId + "/" + fileName, 60 * 60 * 1); // 1 hour

        return ResponseEntity.ok(url);
    }

    @RequestMapping(value = "/tmp/get/{processId}/{fileName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getTempPresignedGetObjectUrl(@PathVariable("processId") String processId, @PathVariable("fileName") String fileName, HttpServletRequest request) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

        if (processId == null || fileName == null) {
            log.warning("No processId or File specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No processId or File specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        MinioClient minioClient = new MinioClient(getLocation(request), this.minioAccessKey, this.minioSecretKey, "us-east-1");

        String url = minioClient.presignedGetObject(minio.getTempBucketName(), processId + "/" + fileName, 60 * 60 * 1); // 1 hour

        return ResponseEntity.ok(url);
    }

    @RequestMapping(value = "/put/{processId}/{taskId}/{fileName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getPresignedPutObjectUrl(@PathVariable("processId") String processId, @PathVariable("taskId") String taskId, @PathVariable("fileName") String fileName, HttpServletRequest request) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

        if (processId == null || taskId == null || fileName == null) {
            log.warning("No Process Instance, Task or File specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Process Instance, Task or File specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        boolean taskBelongsToProcessInstance = taskService.createTaskQuery()
                .taskId(taskId)
                .processInstanceId(processId)
                .count() > 0;

        if (!taskBelongsToProcessInstance) {
            log.warning("The Task not found or does not belong to the Process Instance");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The Task not found or does not belong to the Process Instance");
        }

        boolean taskAssignedToCurrentUser = taskService.getIdentityLinksForTask(taskId)
                .stream()
                .filter(link -> link.getType().equals("assignee"))
                .filter(link -> link.getUserId().equals(identityService.getCurrentAuthentication().getUserId()))
                .count() > 0;

        if (!taskAssignedToCurrentUser) {
            log.warning("The User is not authorized to upload Files to the Task");
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
            log.warning("No Uuid or File specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Uuid or File specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        MinioClient minioClient = new MinioClient(getLocation(request), this.minioAccessKey, this.minioSecretKey, "us-east-1");

        String url = minioClient.presignedPutObject(minio.getTempBucketName(), uuid + "/" + fileName, 60 * 60 * 1);

        return ResponseEntity.ok(url);
    }

    @RequestMapping(value = "/admin/put/{processId}/{taskId}/{fileName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getAdminPresignedPutObjectUrl(@PathVariable("processId") String processId, @PathVariable("taskId") String taskId, @PathVariable("fileName") String fileName, HttpServletRequest request) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

        if (identityService.getCurrentAuthentication() == null || !identityService.getCurrentAuthentication().getUserId().equals("demo")) {
            log.warning("Not demo user or not logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not demo user or not logged in");
        }

        MinioClient minioClient = new MinioClient(getLocation(request), this.minioAccessKey, this.minioSecretKey, "us-east-1");

        String url = minioClient.presignedPutObject(minio.getBucketName(), processId + "/" + taskId + "/" + fileName, 60 * 60 * 1);

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
