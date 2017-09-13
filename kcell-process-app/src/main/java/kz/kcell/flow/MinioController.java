package kz.kcell.flow;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xmlpull.v1.XmlPullParserException;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidExpiresRangeException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.NoResponseException;

@RestController
@RequestMapping("/uploads")
public class MinioController {

    private static final Logger log = LoggerFactory.getLogger(MinioController.class);

    @Value("${minio.url:http://localhost:9000}")
	private String minioUrl;

    @Value("${minio.access.key:AKIAIOSFODNN7EXAMPLE}")
	private String minioAccessKey;

    @Value("${minio.secret.key:wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY}")
	private String minioSecretKey;
	
	@Autowired
	private IdentityService identityService;
	
	@Autowired
	private TaskService taskService;
	
	private MinioClient minioClient;

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

		String url = this.getMinioClient().presignedGetObject("uploads", processId + "/" + taskId + "/" + fileName, 60 * 60 * 1); // 1 hour
		return ResponseEntity.ok(url);
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

		String url = this.getMinioClient().presignedPutObject("uploads", processId + "/" + taskId + "/" + fileName);
		return ResponseEntity.ok(url);
	}
	
	private MinioClient getMinioClient() throws InvalidEndpointException, InvalidPortException {
		if (minioClient == null) {
			minioClient = new MinioClient(minioUrl, minioAccessKey, minioSecretKey);
		}
		return minioClient;
	}

}
