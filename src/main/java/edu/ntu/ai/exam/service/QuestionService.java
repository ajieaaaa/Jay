package edu.ntu.ai.exam.service;

import edu.ntu.ai.exam.mapper.ExamMapper;
import edu.ntu.ai.exam.mapper.QuestionMapper;
import edu.ntu.ai.exam.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class QuestionService {
    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamMapper examMapper;

    public List<Question> getAllQuestions() {
        return questionMapper.findAll();
    }

    public Question getQuestionById(Long id) {
        return questionMapper.findById(id);
    }

    @Transactional
    public void addQuestion(Question question) {
        // 获取下一个可用的ID
        Long nextId = questionMapper.getNextId();
        question.setId(nextId);
        questionMapper.insert(question);
    }

    public List<Question> getRandomQuestions(int count) {
        List<Question> allQuestions = getAllQuestions();
        if (allQuestions.size() <= count) {
            return allQuestions;
        }
        List<Question> shuffledQuestions = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffledQuestions);
        return shuffledQuestions.subList(0, count);
    }

    @Transactional
    public void updateQuestion(Question question) {
        Question existingQuestion = questionMapper.findById(question.getId());
        if (existingQuestion == null) {
            throw new RuntimeException("题目不存在");
        }
        questionMapper.update(question);
    }

    public boolean isQuestionInUse(Long questionId) {
        return examMapper.countExamsByQuestionId(questionId) > 0;
    }

    @Transactional
    public void deleteQuestion(Long id) {
        // 先删除题目与考试的关联
        examMapper.deleteQuestionFromAllExams(id);
        // 再删除题目本身
        questionMapper.delete(id);
    }
}