package kz.kcell.flow;

import io.minio.MinioClient;
import io.minio.errors.*;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/uploads")
public class MinioController {

    private static final Logger log = LoggerFactory.getLogger(MinioController.class);

    private final MinioClient minioClient;
    private final String bucketName = "uploads";


    public MinioController(@Value("${minio.url:http://localhost:9000}") String minioUrl,
                           @Value("${minio.access.key:AKIAIOSFODNN7EXAMPLE}") String minioAccessKey,
                           @Value("${minio.secret.key:wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY}") String minioSecretKey)
			throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException,
			InsufficientDataException, NoResponseException, InvalidBucketNameException, XmlPullParserException, InternalException,
			RegionConflictException, ErrorResponseException {
        minioClient = new MinioClient(minioUrl, minioAccessKey, minioSecretKey);
    }

    @EventListener
    public void makeBucket(ApplicationReadyEvent event)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InternalException,
			NoResponseException, InvalidBucketNameException, XmlPullParserException, RegionConflictException, ErrorResponseException {
		try {
			minioClient.makeBucket(bucketName);
		} catch (ErrorResponseException e) {
			if ("BucketAlreadyOwnedByYou".equals(e.errorResponse().code())) {
				log.debug("Bucket is already there, noop");
			} else {
				throw e;
			}
		}
    }

	@Autowired
	private IdentityService identityService;
	
	@Autowired
	private TaskService taskService;

	@RequestMapping(value = "/get/{processId}/{taskId}/{fileName:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getPresignedGetObjectUrl(@PathVariable("processId") String processId, @PathVariable("taskId") String taskId, @PathVariable("fileName") String fileName) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

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

        String url = minioClient.presignedGetObject(bucketName, processId + "/" + taskId + "/" + fileName, 60 * 60 * 1); // 1 hour

        return ResponseEntity.ok(stripProtocolHostPort(url));
	}

	@RequestMapping(value = "/put/{processId}/{taskId}/{fileName:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getPresignedPutObjectUrl(@PathVariable("processId") String processId, @PathVariable("taskId") String taskId, @PathVariable("fileName") String fileName) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException{

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

		String url = minioClient.presignedPutObject(bucketName, processId + "/" + taskId + "/" + fileName);

        return ResponseEntity.ok(stripProtocolHostPort(url));
	}

    private static String stripProtocolHostPort(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        return url.getPath() + "?" + url.getQuery();
    }

}
