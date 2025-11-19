package com.me.project.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PiDevice {

	  @Id
	    private String deviceId;
	    private String deviceName;
	    private String department;
	    private String ipAddress;
	    private LocalDateTime lastSeen;
	    private boolean online;
}
