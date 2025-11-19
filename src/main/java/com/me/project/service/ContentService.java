package com.me.project.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.me.project.entity.ContentData;
import com.me.project.repository.ContentRepository;

@Service
public class ContentService {
		@Value("${file.upload-dir}")
		public String dir;
		
		@Autowired
	    private	ContentRepository contentRepository;
		
		public ContentData saveContent(MultipartFile file,String title) throws IllegalStateException, IOException {
			
			String newId =UUID.randomUUID().toString();
			String filename=newId+"_"+file.getOriginalFilename();
			File  filedir =new File(System.getProperty("user.dir")+File.separator+dir);
			if(!filedir.exists()) {
				filedir.mkdirs();
			}
			
			File destination= new File(filedir,filename );
			file.transferTo(destination);
		
			ContentData data = ContentData.builder().id(newId).fileName(filename).title(title).filePath(destination.getAbsolutePath()).build();
			return contentRepository.save(data) ;
		}
		
		public List<ContentData> getAllContents() {
			return contentRepository.findAll();
		}
		
		public String deleteContent(String id) {
			 ContentData content = contentRepository.findById(id)
				        .orElseThrow(() -> new RuntimeException("Content not found"));
			 
			  String filePath = content.getFilePath();
			  
			  File file = new File(filePath);
			    if (file.exists()) {
			        file.delete();
			    }
			contentRepository.deleteById(id);
			
			return "Content with id "+id+" deleted successfully";
		}
}
