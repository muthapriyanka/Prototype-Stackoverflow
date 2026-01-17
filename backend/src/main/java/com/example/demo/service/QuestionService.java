package com.example.demo.service;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.demo.AnswerDTO;
import com.example.demo.Question;
import com.example.demo.QuestionDetailResponse;
import com.example.demo.QuestionRequest;
import com.example.demo.QuestionResponse;
import com.example.demo.QuestionWithAnswersDTO;
import com.example.demo.Tag;
import com.example.demo.TagDTO;
import com.example.demo.Vote;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.VoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final VoteRepository voteRepository;
    private final TagRepository tagRepository;

    @Cacheable("questions")
    public List<QuestionWithAnswersDTO> getAllQuestions() {
    return questionRepository.findAllByOrderByCreatedAtDesc().stream().map(q -> {
        QuestionWithAnswersDTO dto = new QuestionWithAnswersDTO();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setBody(q.getBody());
        dto.setAnswers(q.getAnswers().stream().map(a -> {
            AnswerDTO adto = new AnswerDTO();
            adto.setId(a.getId());
            adto.setBody(a.getBody());
            // Handle possible null user
            adto.setUsername(a.getUser() != null ? a.getUser().getUsername() : "Unknown");
            return adto;
        }).toList());
        return dto;
    }).toList();
}
    
    @Cacheable(value = "question", key = "#id")
    public Optional<Question> getQuestionById(String id) {
        return questionRepository.findById(id);
    }

    public QuestionResponse getQuestionResponseById(String id) {
    Question q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
    return new QuestionResponse(
        q.getId(),
        q.getTitle(),
        q.getBody(),
        voteRepository.getVoteCount(q.getId(), Vote.EntityType.QUESTION),
        q.getAnswers().size(),
        q.getCreatedAt(),
        q.getTags()
         .stream()
         .map(t -> new TagDTO(t.getId(), t.getName()))  // ✅ send objects
         .toList()
    );
}

public QuestionDetailResponse getQuestionDetail(String id) {

    Question q = questionRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Question not found"));

    List<AnswerDTO> answers = q.getAnswers().stream().map(a -> {
        AnswerDTO dto = new AnswerDTO();
        dto.setId(a.getId());
        dto.setBody(a.getBody());
        dto.setUsername(
            a.getUser() != null ? a.getUser().getUsername() : "Unknown"
        );
        return dto;
    }).toList();

    return new QuestionDetailResponse(
        q.getId(),
        q.getTitle(),
        q.getBody(),
        voteRepository.getVoteCount(q.getId(), Vote.EntityType.QUESTION),
        q.getCreatedAt(),
        q.getTags().stream().map(Tag::getName).toList(),
        answers
    );
}

  public List<QuestionResponse> getAllQuestionResponses() {
    return questionRepository.findAllByOrderByCreatedAtDesc()
        .stream()
        .map(q -> new QuestionResponse(
            q.getId(),
        q.getTitle(),
        q.getBody(),
        voteRepository.getVoteCount(q.getId(), Vote.EntityType.QUESTION),
        q.getAnswers().size(),
        q.getCreatedAt(),
        q.getTags()
         .stream()
         .map(t -> new TagDTO(t.getId(), t.getName()))  // ✅ send objects
         .toList()
        ))
        .toList();
}

    @CacheEvict(value = { "questions", "question" }, allEntries = true)
    public Question createQuestion(QuestionRequest request) {
        Question q = new Question();
        q.setTitle(request.getTitle());
        q.setBody(request.getBody());
        Set<Tag> tagSet = new HashSet<>();
        for (String tagId : request.getTagIds()) {
            if (tagId == null) {
                continue; // skip bad values
            }
        System.out.println("Received tagIds: " + request.getTagIds());
            tagSet.add(tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found")));
        }
        q.setTags(tagSet);

        return questionRepository.save(q);
    }

    @CacheEvict(value = { "questions", "question" }, allEntries = true)
    public void deleteQuestion(String id) {
        questionRepository.deleteById(id);
    }

}
  
