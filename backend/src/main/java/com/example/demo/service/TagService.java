package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Tag;
import com.example.demo.TagDTO;
import com.example.demo.repository.TagRepository;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag createTag(Tag tag) {
        return tagRepository.save(tag);
    }

    @Transactional(readOnly = true)
    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName(), tag.getQuestions().size()))
                .toList();
    }

    public Optional getTagByName(String name) {
        return tagRepository.findByName(name);
    }
}
