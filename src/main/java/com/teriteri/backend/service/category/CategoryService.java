package com.teriteri.backend.service.category;

import com.teriteri.backend.pojo.Category;
import com.teriteri.backend.pojo.CustomResponse;

public interface CategoryService {
    CustomResponse getAll();

    Category getCategoryById(String mcId, String scId);
}
