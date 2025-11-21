package com.me.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentData {
		@Id
		private String id;
		private String title;
		private String fileName;
		private String filePath;
		private String piFileName;
		private boolean isDisplayed = false;

		
}
