package edu.ntu.ai.exam.service;

import edu.ntu.ai.exam.mapper.ExamMapper;
import edu.ntu.ai.exam.mapper.ScoreMapper;
import edu.ntu.ai.exam.model.Exam;
import edu.ntu.ai.exam.model.Question;
import edu.ntu.ai.exam.model.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {
    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private QuestionService questionService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public List<Exam> getAllExams() {
        return examMapper.findAll();
    }

    public Exam getExamById(Long examId) {
        return examMapper.findById(examId);
    }

    public List<Exam> getAvailableExams(Long userId) {
        List<Exam> allExams = examMapper.findAll();
        List<Score> userScores = scoreMapper.findByUserId(userId);
        Set<Long> completedExamIds = userScores.stream()
                .map(Score::getExamId)
                .collect(Collectors.toSet());

        return allExams.stream()
                .filter(exam -> !completedExamIds.contains(exam.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Exam generateExam(String examName, int questionCount, Long creatorId) {
        if (questionCount <= 0) {
            throw new IllegalArgumentException("题目数量必须大于0");
        }

        List<Question> allQuestions = questionService.getAllQuestions();
        if (allQuestions.size() < questionCount) {
            throw new IllegalArgumentException("题库中的题目数量不足");
        }

        Exam exam = new Exam();
        exam.setExamName(examName);
        exam.setQuestionCount(questionCount);
        exam.setCreateTime(new Date());
        exam.setCreatorId(creatorId);

        examMapper.insert(exam);

        List<Question> selectedQuestions = questionService.getRandomQuestions(questionCount);
        for (Question question : selectedQuestions) {
            examMapper.insertExamQuestion(exam.getId(), question.getId());
        }

        return exam;
    }


    public Map<String, Object> getExamForStudent(Long examId) {
        Exam exam = getExamById(examId);
        List<Question> questions = getExamQuestions(examId);

        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("questions", questions);
        return result;
    }

    public List<Question> getExamQuestions(Long examId) {
        return examMapper.getExamQuestions(examId);
    }

    public boolean hasStudentTakenExam(Long examId, Long userId) {
        return scoreMapper.findByUserIdAndExamId(userId, examId) != null;
    }

    @Transactional
    public Map<String, Object> submitAndScoreExam(Long examId, Long userId, Map<Long, String> answers) {
        if (hasStudentTakenExam(examId, userId)) {
            throw new IllegalStateException("您已经参加过这个考试");
        }

        Exam exam = getExamById(examId);
        if (exam == null) {
            throw new IllegalStateException("考试不存在");
        }

        List<Question> questions = getExamQuestions(examId);
        if (questions.isEmpty()) {
            throw new IllegalStateException("考试题目为空");
        }

        int correctCount = 0;
        for (Question question : questions) {
            String userAnswer = answers.get(question.getId());
            if (userAnswer != null && userAnswer.trim().equalsIgnoreCase(question.getAnswer().trim())) {
                correctCount++;
            }
        }

        double scorePercentage = (double) correctCount / exam.getQuestionCount() * 100;
        int totalScore = (int) Math.round(scorePercentage);
        Score score = new Score();
        score.setUserId(userId);
        score.setExamId(examId);
        score.setScore(totalScore);
        score.setSubmitTime(new Date());
        score.setActualScore(correctCount);  // 设置实际得分
        score.setTotalQuestions(exam.getQuestionCount());  // 设置总题目数

        try {
            scoreMapper.insert(score);
        } catch (Exception e) {
            throw new RuntimeException("保存考试成绩失败: " + e.getMessage(), e);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("score", String.format("%d / %d", correctCount, exam.getQuestionCount()));
        result.put("message", String.format("考试提交成功！您的得分是：%d / %d", correctCount, exam.getQuestionCount()));
        result.put("redirectUrl", "/exam/" + examId + "/score");
        return result;
    }


    @Transactional
    public void deleteExam(Long examId) {
        List<Score> scores = scoreMapper.findByExamId(examId);
        if (!scores.isEmpty()) {
            throw new IllegalStateException("已有学生参加过此考试，无法删除");
        }

        examMapper.deleteExamQuestions(examId);
        examMapper.deleteExam(examId);
    }

    public Map<String, Object> getExamStatistics(Long examId) {
        List<Score> scores = scoreMapper.findByExamId(examId);
        if (scores.isEmpty()) {
            throw new IllegalStateException("暂无考试数据");
        }

        Exam exam = getExamById(examId);
        int totalQuestions = exam.getQuestionCount();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalStudents", scores.size());

        Double avgScore = scoreMapper.getAverageScore(examId);
        Integer maxScore = scoreMapper.getHighestScore(examId);
        Integer minScore = scoreMapper.getLowestScore(examId);
        Integer passCount = scoreMapper.getPassCount(examId);
        Integer totalCount = scoreMapper.getTotalCount(examId);

        statistics.put("maxScore", String.format("%d / %d",
                maxScore * totalQuestions / 100, totalQuestions));
        statistics.put("minScore", String.format("%d / %d",
                minScore * totalQuestions / 100, totalQuestions));
        statistics.put("averageScore", String.format("%.1f / %d",
                avgScore * totalQuestions / 100.0, totalQuestions));
        statistics.put("passRate", String.format("%.1f%%",
                (double) passCount / totalCount * 100));

        Map<String, Long> scoreDistribution = new HashMap<>();
        scoreDistribution.put("0-59", scores.stream()
                .filter(s -> s.getScore() < 60).count());
        scoreDistribution.put("60-69", scores.stream()
                .filter(s -> s.getScore() >= 60 && s.getScore() < 70).count());
        scoreDistribution.put("70-79", scores.stream()
                .filter(s -> s.getScore() >= 70 && s.getScore() < 80).count());
        scoreDistribution.put("80-89", scores.stream()
                .filter(s -> s.getScore() >= 80 && s.getScore() < 90).count());
        scoreDistribution.put("90-100", scores.stream()
                .filter(s -> s.getScore() >= 90).count());
        statistics.put("scoreDistribution", scoreDistribution);

        List<Map<String, String>> submissionTimes = scores.stream()
                .map(score -> {
                    Map<String, String> submission = new HashMap<>();
                    submission.put("userName", score.getUserName());
                    submission.put("submitTime", dateFormat.format(score.getSubmitTime()));
                    submission.put("score", String.format("%d / %d",
                            score.getScore() * totalQuestions / 100, totalQuestions));
                    return submission;
                })
                .collect(Collectors.toList());
        statistics.put("submissions", submissionTimes);

        return statistics;
    }

    public Score getStudentScore(Long examId, Long userId) {
        Score score = scoreMapper.findByUserIdAndExamId(userId, examId);
        if (score == null) {
            return null;
        }

        // 获取考试信息以计算实际分数
        Exam exam = getExamById(examId);
        if (exam != null) {
            // 设置考试名称
            score.setExamName(exam.getExamName());

            // 计算实际得分（将百分比转换为实际分数）
            int actualScore = (int) Math.round(score.getScore() * exam.getQuestionCount() / 100.0);
            score.setActualScore(actualScore);
            score.setTotalQuestions(exam.getQuestionCount());
        }

        return score;
    }
}