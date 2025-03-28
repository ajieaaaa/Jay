package edu.ntu.ai.exam.mapper;

import edu.ntu.ai.exam.model.Score;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ScoreMapper {
    @Select("SELECT s.id, s.user_id, s.exam_id, s.score, s.submittime, " +
            "s.actual_score, s.total_questions, " +
            "u.username as username, e.examName as examName " +
            "FROM scores s " +
            "JOIN users u ON s.user_id = u.id " +
            "JOIN exams e ON s.exam_id = e.id")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "score", column = "score"),
            @Result(property = "submitTime", column = "submittime"),
            @Result(property = "actualScore", column = "actual_score"),
            @Result(property = "totalQuestions", column = "total_questions"),
            @Result(property = "userName", column = "username"),
            @Result(property = "examName", column = "examName")
    })
    List<Score> findAll();

    @Insert("INSERT INTO scores (user_id, exam_id, score, submittime, actual_score, total_questions) " +
            "VALUES (#{userId}, #{examId}, #{score}, #{submitTime}, #{actualScore}, #{totalQuestions})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Score score);

    @Select("SELECT s.id, s.user_id, s.exam_id, s.score, s.submittime, " +
            "s.actual_score, s.total_questions, " +
            "u.username as username, e.examName as examName " +
            "FROM scores s " +
            "JOIN users u ON s.user_id = u.id " +
            "JOIN exams e ON s.exam_id = e.id " +
            "WHERE s.user_id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "score", column = "score"),
            @Result(property = "submitTime", column = "submittime"),
            @Result(property = "actualScore", column = "actual_score"),
            @Result(property = "totalQuestions", column = "total_questions"),
            @Result(property = "userName", column = "username"),
            @Result(property = "examName", column = "examName")
    })
    List<Score> findByUserId(Long userId);


    @Select("SELECT s.id, s.user_id, s.exam_id, s.score, s.submittime, " +
            "s.actual_score, s.total_questions, " +
            "u.username as username, e.examName as examName " +
            "FROM scores s " +
            "JOIN users u ON s.user_id = u.id " +
            "JOIN exams e ON s.exam_id = e.id " +
            "WHERE s.exam_id = #{examId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "score", column = "score"),
            @Result(property = "submitTime", column = "submittime"),
            @Result(property = "actualScore", column = "actual_score"),
            @Result(property = "totalQuestions", column = "total_questions"),
            @Result(property = "userName", column = "username"),
            @Result(property = "examName", column = "examName")
    })
    List<Score> findByExamId(Long examId);

    @Select("SELECT s.id, s.user_id, s.exam_id, s.score, s.submittime, " +
            "s.actual_score, s.total_questions, " +
            "u.username as username, e.examName as examName " +
            "FROM scores s " +
            "JOIN users u ON s.user_id = u.id " +
            "JOIN exams e ON s.exam_id = e.id " +
            "WHERE s.user_id = #{userId} AND s.exam_id = #{examId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "score", column = "score"),
            @Result(property = "submitTime", column = "submittime"),
            @Result(property = "actualScore", column = "actual_score"),
            @Result(property = "totalQuestions", column = "total_questions"),
            @Result(property = "userName", column = "username"),
            @Result(property = "examName", column = "examName")
    })
    Score findByUserIdAndExamId(@Param("userId") Long userId, @Param("examId") Long examId);

    @Select("SELECT AVG(score) FROM scores WHERE exam_id = #{examId}")
    Double getAverageScore(Long examId);

    @Select("SELECT MAX(score) FROM scores WHERE exam_id = #{examId}")
    Integer getHighestScore(Long examId);

    @Select("SELECT MIN(score) FROM scores WHERE exam_id = #{examId}")
    Integer getLowestScore(Long examId);

    @Select("SELECT COUNT(*) FROM scores WHERE exam_id = #{examId} AND score >= 60")
    Integer getPassCount(Long examId);

    @Select("SELECT COUNT(*) FROM scores WHERE exam_id = #{examId}")
    Integer getTotalCount(Long examId);
}