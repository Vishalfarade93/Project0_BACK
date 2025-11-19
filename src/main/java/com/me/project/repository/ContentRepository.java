package com.me.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.me.project.entity.ContentData;

public interface ContentRepository extends JpaRepository<ContentData, String> {

	@Override
	Optional<ContentData> findById(String id);
}
