package com.example.dailyplanner.repository;

import com.example.dailyplanner.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, String> {

//    List<Tag> findByUserId(String userId);

}
