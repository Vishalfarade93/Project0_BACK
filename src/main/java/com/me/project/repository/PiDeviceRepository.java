package com.me.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.me.project.entity.PiDevice;

public interface PiDeviceRepository extends JpaRepository<PiDevice, String> {
	
}
