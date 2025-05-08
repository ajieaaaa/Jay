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
import java.util.Set;

@Service
public class QuestionService {
    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamMapper examMapper;


    // 查询所有题目（无需修改）
    public List<Question> getAllQuestions() {
        return questionMapper.findAll();
    }

    // 根据ID查询题目（无需修改）
    public Question getQuestionById(Long id) {
        return questionMapper.findById(id);
    }

    // 新增：动态条件查询题目
    public List<Question> findQuestionsByCriteria(String knowledgePoint, String questionType, String difficulty) {
        return questionMapper.findByCriteria(knowledgePoint, questionType, difficulty);
    }
    // 在更新/新增时增加枚举校验
    private static final Set<String> VALID_TYPES = Set.of("SINGLE_CHOICE", "MULTIPLE_CHOICE");
    private static final Set<String> VALID_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");
    @Transactional
    public void addQuestion(Question question) {
        // 校验题型和难度枚举值
        if (question.getQuestion_type() != null && !VALID_TYPES.contains(question.getQuestion_type())) {
            throw new IllegalArgumentException("非法的题型参数");
        }
        if (question.getDifficulty() != null && !VALID_DIFFICULTIES.contains(question.getDifficulty())) {
            throw new IllegalArgumentException("非法的难度参数");
        }
        // 获取下一个可用的ID（逻辑不变）
        Long nextId = questionMapper.getNextId();
        question.setId(nextId);

        // 新增字段已通过question对象传递（需确保实体类包含新字段）
        questionMapper.insert(question);
    }

    // 随机选题逻辑（无需修改）
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
        // 新增字段会自动通过question对象更新
        Question existingQuestion = questionMapper.findById(question.getId());
        if (existingQuestion == null) {
            throw new RuntimeException("题目不存在");
        }
        questionMapper.update(question); // 包含新字段的更新
    }

    // 题目使用状态检查（无需修改）
    public boolean isQuestionInUse(Long questionId) {
        return examMapper.countExamsByQuestionId(questionId) > 0;
    }

    @Transactional
    public void deleteQuestion(Long id) {
        // 关联关系清理逻辑（无需修改）
        examMapper.deleteQuestionFromAllExams(id);

        questionMapper.delete(id);
    }
    // 可扩展方法示例：按难度随机抽题
    public List<Question> getRandomQuestionsByDifficulty(int count, String difficulty) {
        List<Question> filtered = findQuestionsByCriteria(null, null, difficulty);
        Collections.shuffle(filtered);
        return filtered.size() <= count ? filtered : filtered.subList(0, count);
    }
}