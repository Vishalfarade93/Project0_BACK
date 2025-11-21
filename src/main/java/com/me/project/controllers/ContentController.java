package com.me.project.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
public class ContentController {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private PiDeviceRepository piDeviceRepo;

    @Autowired
    private ContentService contentService;


    @PostMapping("/upload")
    public ResponseEntity<ContentData> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam("title") String title) throws Exception {
        ContentData content = contentService.saveContent(file, title);
        return ResponseEntity.ok(content);
    }


    @PostMapping("/display/{id}")
    public ResponseEntity<String> displayOnPi(@PathVariable String id) {
        ContentData data = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        PiDevice device = piDeviceRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Pi registered"));

        String piUrl = "http://" + device.getIpAddress() + ":5000";

        try {
            File file;

   
            if (data.getFilePath().startsWith("http")) {
                System.out.println("Downloading Cloudinary file...");

                URL url = new URL(data.getFilePath());
                InputStream in = url.openStream();

                file = File.createTempFile("cloud_", ".jpg");
                FileOutputStream out = new FileOutputStream(file);

                in.transferTo(out);
                in.close();
                out.close();
            } else {
                file = new File(data.getFilePath());
            }
         
            data.setPiFileName(file.getName());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));
            

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response =
                    restTemplate.postForEntity(piUrl + "/upload", requestEntity, String.class);

            data.setDisplayed(true);
            contentRepository.save(data);

            return ResponseEntity.ok("Sent to Pi: " + response.getBody());

        } catch (Exception e) {
            System.out.println("Error contacting Pi: " + e.getMessage());
            return ResponseEntity.status(500).body("Error contacting Pi: " + e.getMessage());
        }
    }


    @GetMapping("/all")
    public ResponseEntity<List<ContentData>> getAllContents() {
        return ResponseEntity.ok(contentService.getAllContents());
    }

    // Delete from database
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteContent(@PathVariable String id) {
        return ResponseEntity.ok(contentService.deleteContent(id));
    }

    // Remove From Pi
    @PostMapping("/remove/{id}")
    public ResponseEntity<String> removeFromPi(@PathVariable String id) {

        ContentData data = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        PiDevice device = piDeviceRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Pi registered"));

        String piUrl = "http://" + device.getIpAddress() + ":5000";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        String piFileName = data.getPiFileName();
        if (piFileName == null) {
            return ResponseEntity.status(500)
                    .body("Pi filename missing for this content.");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("fileName", piFileName);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response =
                    restTemplate.postForEntity(piUrl + "/remove", requestEntity, String.class);

            data.setDisplayed(false);
            contentRepository.save(data);

            return ResponseEntity.ok("Removed from Pi: " + response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error contacting Raspberry Pi: " + e.getMessage());
        }
    }
}
