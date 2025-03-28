package edu.ntu.ai.exam.controller;

import edu.ntu.ai.exam.model.Question;
import edu.ntu.ai.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/question")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    // 显示题目列表
    @GetMapping("/list")
    public String list(Model model) {
        List<Question> questions = questionService.getAllQuestions();
        model.addAttribute("questions", questions);
        return "question_list";
    }

    // 显示添加题目页面
    @GetMapping("/add")
    public String showAddPage() {
        return "question_add";
    }

    // 处理添加题目请求
    @PostMapping("/add")
    public String add(@ModelAttribute Question question) {
        questionService.addQuestion(question);
        return "redirect:/question/list";
    }

    // 显示编辑题目页面
    @GetMapping("/edit/{id}")
    public String showEditPage(@PathVariable Long id, Model model) {
        Question question = questionService.getQuestionById(id);
        model.addAttribute("question", question);
        return "question_edit";
    }

    @PostMapping("/edit")
    public String edit(@ModelAttribute Question question) {
        try {
            questionService.updateQuestion(question);
            return "redirect:/question/list";
        } catch (Exception e) {
            return "redirect:/question/edit/" + question.getId() + "error";
        }
    }

    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            if (questionService.isQuestionInUse(id)) {
                return ResponseEntity.badRequest()
                        .body("该题目已被考试使用，无法删除");
            }
            questionService.deleteQuestion(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("删除失败：" + e.getMessage());
        }
    }
}