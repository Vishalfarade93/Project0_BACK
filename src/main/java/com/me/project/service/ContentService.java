package com.me.project.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.me.project.entity.ContentData;
import com.me.project.repository.ContentRepository;

@Service
public class ContentService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ContentRepository contentRepository;

    public ContentData saveContent(MultipartFile file, String title) {

        String newId = UUID.randomUUID().toString();
        
        ContentData temp = ContentData.builder()
                .id(newId)
                .fileName(file.getOriginalFilename())
                .title(title)
                .filePath("uploading")
                .build();

        contentRepository.save(temp);

        uploadToCloudinaryAsync(file, newId, title);

        return temp; 
    }

    @Async
    public void uploadToCloudinaryAsync(MultipartFile file, String id, String title) {

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "project0_uploads")
            );

            String url = uploadResult.get("secure_url").toString();
            System.out.println("Uploaded to Cloudinary: " + url);

            saveUploadedContent(id, title, file.getOriginalFilename(), url);

        } catch (Exception e) {
            System.out.println("Async Cloudinary upload failed: " + e.getMessage());
        }
    }
    public void saveUploadedContent(String id, String title, String originalName, String cloudUrl) {

        ContentData finalData = ContentData.builder()
                .id(id)
                .fileName(originalName)
                .title(title)
                .filePath(cloudUrl)
                .build();

        contentRepository.save(finalData);
    }

    public String deleteContent(String id) {
        ContentData content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        try {
            String publicId = extractPublicId(content.getFilePath());
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.out.println("Cloudinary deletion failed: " + e.getMessage());
        }

        contentRepository.deleteById(id);
        return "Content deleted successfully";
    }

    private String extractPublicId(String url) {
        String withoutBase = url.substring(url.indexOf("project0_uploads"));
        return withoutBase.substring(0, withoutBase.lastIndexOf('.'));
    }
    
    public List<ContentData> getAllContents() {
        return contentRepository.findAll();
    }
}
