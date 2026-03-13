package com.example.dailyplanner.repository;

import com.example.dailyplanner.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByUserId(String userId);

}
