package com.teriteri.backend.service.video;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VideoService {
    List<Map<String, Object>> getVideosWithUserAndCategoryByIds(Set<Object> set, Integer index, Integer quantity);

}
