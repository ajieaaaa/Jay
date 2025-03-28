package edu.ntu.ai.exam.mapper;

import edu.ntu.ai.exam.model.Exam;
import edu.ntu.ai.exam.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExamMapper {
    @Insert("INSERT INTO exams (examname, questioncount, createtime, creator_id) " +
            "VALUES (#{examName}, #{questionCount}, #{createTime}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Exam exam);

    @Select("SELECT * FROM exams")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examName", column = "examname"),
            @Result(property = "questionCount", column = "questioncount"),
            @Result(property = "createTime", column = "createtime"),
            @Result(property = "creatorId", column = "creator_id")
    })
    List<Exam> findAll();

    @Select("SELECT * FROM exams WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examName", column = "examname"),
            @Result(property = "questionCount", column = "questioncount"),
            @Result(property = "createTime", column = "createtime"),
            @Result(property = "creatorId", column = "creator_id")
    })
    Exam findById(Long id);

    @Select("SELECT * FROM exams WHERE creator_id = #{creatorId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examName", column = "examname"),
            @Result(property = "questionCount", column = "questioncount"),
            @Result(property = "createTime", column = "createtime"),
            @Result(property = "creatorId", column = "creator_id")
    })
    List<Exam> findByCreatorId(Long creatorId);

    @Insert("INSERT INTO exam_questions (exam_id, question_id) VALUES (#{examId}, #{questionId})")
    void insertExamQuestion(@Param("examId") Long examId, @Param("questionId") Long questionId);

    @Select("SELECT q.* FROM questions q " +
            "JOIN exam_questions eq ON q.id = eq.question_id " +
            "WHERE eq.exam_id = #{examId}")
    List<Question> getExamQuestions(Long examId);

    @Delete("DELETE FROM exam_questions WHERE exam_id = #{examId}")
    void deleteExamQuestions(Long examId);

    @Delete("DELETE FROM exams WHERE id = #{examId}")
    void deleteExam(Long examId);

    @Select("SELECT COUNT(*) FROM exam_questions WHERE question_id = #{questionId}")
    int countExamsByQuestionId(Long questionId);

    @Delete("DELETE FROM exam_questions WHERE question_id = #{questionId}")
    void deleteQuestionFromAllExams(Long questionId);
}