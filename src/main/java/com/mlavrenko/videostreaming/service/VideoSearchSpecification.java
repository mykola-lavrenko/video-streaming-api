package com.mlavrenko.videostreaming.service;

import com.mlavrenko.videostreaming.domain.VideoMetadata;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class VideoSearchSpecification {
    public Specification<VideoMetadata> createSearchSpecification(String title, String director, Integer yearOfRelease) {
        return Specification.allOf(
                createFieldSpecification(title, this::filterByTitle),
                createFieldSpecification(director, this::filterByDirector),
                createFieldSpecification(yearOfRelease, this::filterByYearOfRelease)
        );
    }

    private <T> Specification<VideoMetadata> createFieldSpecification(T field, Function<T, Specification<VideoMetadata>> filterFunction) {
        return field == null ? alwaysTrue() : filterFunction.apply(field);
    }

    private Specification<VideoMetadata> alwaysTrue() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    private Specification<VideoMetadata> filterByTitle(String title) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private Specification<VideoMetadata> filterByDirector(String director) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("director")), "%" + director.toLowerCase() + "%");
    }

    private Specification<VideoMetadata> filterByYearOfRelease(Integer yearOfRelease) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("yearOfRelease"), yearOfRelease);
    }
}
