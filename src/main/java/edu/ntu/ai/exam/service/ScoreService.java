package edu.ntu.ai.exam.service;

import edu.ntu.ai.exam.mapper.ScoreMapper;
import edu.ntu.ai.exam.model.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreService {
    @Autowired
    private ScoreMapper scoreMapper;

     //获取所有成绩记录
    public List<Score> getAllScores() {
        return scoreMapper.findAll();
    }

     // 根据考试ID获取成绩记录
    public List<Score> getScoresByExamId(Long examId) {
        return scoreMapper.findByExamId(examId);
    }

      //获取考试统计信息
    public Map<String, Object> getExamStatistics(Long examId) {
        Map<String, Object> statistics = new HashMap<>();

        // 获取该考试的所有成绩
        List<Score> scores = getScoresByExamId(examId);
        if (scores.isEmpty()) {
            throw new IllegalStateException("暂无考试数据");
        }

        // 计算统计数据
        Double avgScore = scoreMapper.getAverageScore(examId);
        Integer maxScore = scoreMapper.getHighestScore(examId);
        Integer minScore = scoreMapper.getLowestScore(examId);
        Integer passCount = scoreMapper.getPassCount(examId);
        Integer totalCount = scoreMapper.getTotalCount(examId);

        // 填充统计信息
        statistics.put("totalStudents", totalCount);
        statistics.put("averageScore", avgScore != null ? String.format("%.1f", avgScore) : "0.0");
        statistics.put("highestScore", maxScore != null ? maxScore : 0);
        statistics.put("lowestScore", minScore != null ? minScore : 0);
        statistics.put("passRate", totalCount > 0 ?
                String.format("%.1f%%", (double) passCount / totalCount * 100) : "0.0%");

        // 计算分数分布
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

        // 提交时间记录
        List<Map<String, String>> submissions = scores.stream()
                .map(score -> {
                    Map<String, String> submission = new HashMap<>();
                    submission.put("userName", score.getUserName());
                    submission.put("submitTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(score.getSubmitTime()));
                    submission.put("score", String.format("%d / %d",
                            score.getActualScore(), score.getTotalQuestions()));
                    return submission;
                })
                .collect(Collectors.toList());
        statistics.put("submissions", submissions);

        return statistics;
    }

}