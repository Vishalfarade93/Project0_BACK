package com.me.project.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.me.project.entity.ContentData;
import com.me.project.entity.PiDevice;
import com.me.project.repository.ContentRepository;
import com.me.project.repository.PiDeviceRepository;
import com.me.project.service.ContentService;

@RestController
@RequestMapping("/api/content")
@CrossOrigin(origins = "*")
public class ContentController {
	@Autowired
	private ContentRepository contentRepository;
	
	@Autowired
	private PiDeviceRepository piDeviceRepo;
	@GetMapping
	public String home() {
		return "Content Service is running";
	}
	@Autowired
		private ContentService contentService;
	
		
	//Upload file
		@PostMapping("/upload")
		public ResponseEntity<ContentData> upload(@ RequestParam("file") MultipartFile file,
				 @RequestParam("title") String title) throws IllegalStateException, IOException {
			ContentData content = contentService.saveContent(file,title);
			return ResponseEntity.ok(content);
		}

		//Display content by pushing to rashpberry pi
		@PostMapping("/display/{id}")
		public ResponseEntity<String> displayOnPi(@PathVariable String id) throws IOException {
		    ContentData data = contentRepository.findById(id)
		            .orElseThrow(() ->	 new RuntimeException("File not found"));
		  
		    PiDevice device = piDeviceRepo.findById(id).orElseThrow(() -> new RuntimeException("Raspberry Pi not found"));
		    String piUrl = "http://" + device.getIpAddress() + ":5000";
		    try {
		    	File file = new File(data.getFilePath());
			    HttpHeaders headers = new HttpHeaders();
			    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			    body.add("file", new FileSystemResource(file));

			    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			    RestTemplate restTemplate = new RestTemplate();
			    ResponseEntity<String> response = restTemplate.postForEntity(piUrl+"/upload", requestEntity, String.class);
			    data.setDisplayed(true);
			    contentRepository.save(data);
			    return ResponseEntity.ok("Sent to Pi: " + response.getBody());
			} catch (Exception e) {
				return ResponseEntity.status(500).body("Error contacting Raspberry Pi: " + e.getMessage());
			}
		    
		}
		
	//Get all contents
		@GetMapping("/all")
		public ResponseEntity<List<ContentData>> getAllContents() {
			List<ContentData> allContents = contentService.getAllContents();
			return ResponseEntity.ok(allContents);
		}
		
//Delete content
		@DeleteMapping("/delete/{id}")
		public ResponseEntity<String>deleteContent(@PathVariable String id){
			String deleted = this.contentService.deleteContent(id);
			return ResponseEntity.ok(deleted);
		}

//Remove content from raspberry pi
		@PostMapping("/remove/{id}")
		public ResponseEntity<String> removeFromPi(@PathVariable String id) {
			 PiDevice device = piDeviceRepo.findById(id).orElseThrow(() -> new RuntimeException("Raspberry Pi not found"));
			    String piUrl = "http://" + device.getIpAddress() + ":5000";	
			
		    ContentData data = contentRepository.findById(id)
		            .orElseThrow(() -> new RuntimeException("File not found"));

		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		    // We only send fileName to Raspberry Pi
		    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		    body.add("fileName", data.getFileName());

		    HttpEntity<MultiValueMap<String, Object>> requestEntity =
		            new HttpEntity<>(body, headers);

		    try {
		        RestTemplate restTemplate = new RestTemplate();
		        ResponseEntity<String> response =
		                restTemplate.postForEntity(piUrl+"/remove", requestEntity, String.class);
		        data.setDisplayed(false);
		        contentRepository.save(data);
		        return ResponseEntity.ok("Removed from PI: " + response.getBody());

		    } catch (Exception e) {
		        return ResponseEntity.status(500).body("Error contacting Raspberry Pi: " + e.getMessage());
		    }
		}

		
}
