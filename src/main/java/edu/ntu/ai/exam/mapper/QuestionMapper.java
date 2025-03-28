package edu.ntu.ai.exam.mapper;

import edu.ntu.ai.exam.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {
    @Select("SELECT * FROM questions")
    List<Question> findAll();

    @Select("SELECT * FROM questions WHERE id = #{id}")
    Question findById(Long id);

    @Select("SELECT COALESCE(MAX(id), 0) + 1 FROM questions")
    Long getNextId();

    @Insert("INSERT INTO questions(id, content, optionA, optionB, optionC, optionD, answer) " +
            "VALUES(#{id}, #{content}, #{optionA}, #{optionB}, #{optionC}, #{optionD}, #{answer})")
    void insert(Question question);

    @Update("UPDATE questions SET content = #{content}, " +
            "optionA = #{optionA}, optionB = #{optionB}, " +
            "optionC = #{optionC}, optionD = #{optionD}, " +
            "answer = #{answer} WHERE id = #{id}")
    void update(Question question);

    @Delete("DELETE FROM questions WHERE id = #{id}")
    void delete(Long id);
}
