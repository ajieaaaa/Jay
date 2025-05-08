package edu.ntu.ai.exam.mapper;

import edu.ntu.ai.exam.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {
    // 查询所有题目（自动映射新字段）
    @Select("SELECT * FROM questions")
    List<Question> findAll();

    // 根据ID查询题目（自动映射新字段）
    @Select("SELECT * FROM questions WHERE id = #{id}")
    Question findById(Long id);

    // 动态条件查询（知识点/题型/难度）
    @SuppressWarnings("MyBatisDynamicSql")
    @Select("<script>" +
            "SELECT * FROM questions" +
            "<where>" +
            "  <if test='knowledge_point != null'> AND knowledge_point = #{knowledge_point}</if>" +
            "  <if test='question_type != null'> AND question_type = #{question_type}</if>" +
            "  <if test='difficulty != null'> AND difficulty = #{difficulty}</if>" +
            "</where>" +
            "</script>")
    List<Question> findByCriteria(
            @Param("knowledge_point") String knowledge_point,
            @Param("question_type") String question_type,
            @Param("difficulty") String difficulty
    );

    // 获取下一个ID（无需修改）
    @Select("SELECT COALESCE(MAX(id), 0) + 1 FROM questions")
    Long getNextId();

    // 插入题目（包含新字段）
    @Insert("INSERT INTO questions(id, content, optionA, optionB, optionC, optionD, answer, " +
            "question_type, knowledge_point, difficulty) " +
            "VALUES(#{id}, #{content}, #{optionA}, #{optionB}, #{optionC}, #{optionD}, #{answer}, " +
            "#{question_type}, #{knowledge_point}, #{difficulty})")
    void insert(Question question);

    // 更新题目（包含新字段）
    @Update("UPDATE questions SET " +
            "content = #{content}, optionA = #{optionA}, optionB = #{optionB}, " +
            "optionC = #{optionC}, optionD = #{optionD}, answer = #{answer}, " +
            "question_type = #{question_type}, knowledge_point = #{knowledge_point}, difficulty = #{difficulty} " +
            "WHERE id = #{id}")
    void update(Question question);

    // 删除题目（无需修改）
    @Delete("DELETE FROM questions WHERE id = #{id}")
    void delete(Long id);
}