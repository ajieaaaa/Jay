package edu.ntu.ai.exam.controller;

import edu.ntu.ai.exam.model.Question;
import edu.ntu.ai.exam.service.FileStorageService;
import edu.ntu.ai.exam.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/question")
public class QuestionController {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private FileStorageService fileStorageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 显示题目列表（新增筛选参数支持）
    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) String knowledge_point,
            @RequestParam(required = false) String question_type,
            @RequestParam(required = false) String difficulty,
            Model model) {
        List<Question> questions = questionService.findQuestionsByCriteria(knowledge_point, question_type, difficulty);
        model.addAttribute("questions", questions);

        // 传递筛选参数用于页面回显
        model.addAttribute("currentKnowledgePoint", knowledge_point);
        model.addAttribute("currentQuestionType", question_type);
        model.addAttribute("currentDifficulty", difficulty);

        return "question_list";
    }
    // 文件上传异常处理
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleFileSizeExceed() {
        return ResponseEntity.badRequest().body("单个文件大小不能超过5MB");
    }

    // 新增：处理高级搜索请求（RESTful风格）
    @PostMapping("/search")
    public String advancedSearch(
            @RequestParam(required = false) String knowledge_point,
            @RequestParam(required = false) String question_type,
            @RequestParam(required = false) String difficulty,
            RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("knowledge_point", knowledge_point);
        redirectAttributes.addAttribute("question_type", question_type);
        redirectAttributes.addAttribute("difficulty", difficulty);
        return "redirect:/question/list";
    }

    // 显示添加题目页面
    @GetMapping("/add")
    public String showAddPage(Model model) {
        // 注入枚举选项供前端选择
        model.addAttribute("questionTypes", List.of("SINGLE_CHOICE", "MULTIPLE_CHOICE"));
        model.addAttribute("difficultyLevels", List.of("EASY", "MEDIUM", "HARD"));
        return "question_add";
    }

    // 处理添加题目请求（增加参数校验）
    @PostMapping("/add")
    @Transactional
    public String add(
            @Valid @ModelAttribute Question question,
            BindingResult result,
            @RequestParam("answers") List<String> answers,
            @RequestParam("contentImages") MultipartFile[] contentImages,
            @RequestParam("optionAImage") MultipartFile optionAImage,
            @RequestParam("optionBImage") MultipartFile optionBImage,
            @RequestParam("optionCImage") MultipartFile optionCImage,
            @RequestParam("optionDImage") MultipartFile optionDImage) throws IOException {
        // 验证答案
        if (answers == null || answers.isEmpty()) {
            result.rejectValue("answer", "field.required", "请至少选择一个正确答案");
        } else {
            // 根据题型验证答案数量
            if (question.getQuestion_type().equals("SINGLE_CHOICE") && answers.size() > 1) {
                result.rejectValue("answer", "field.invalid", "单选题只能选择一个答案");
            }
            // 将答案列表转为逗号分隔字符串
            String answerStr = String.join(",", answers);
            question.setAnswer(answerStr);
        }

        if (result.hasErrors()) {
            return "question_add";
        }

        // 先保存题目获取ID
        questionService.addQuestion(question);
        Long questionId = question.getId();

        // 处理内容图片
        StringBuilder contentBuilder = new StringBuilder(question.getContent());
        for (int i = 0; i < contentImages.length; i++) {
            if (!contentImages[i].isEmpty()) {
                String filename = fileStorageService.storeFile(contentImages[i], questionId, "content_" + i);
                contentBuilder.append("\n![图片](/uploads/questions/").append(filename).append(")");
            }
        }
        question.setContent(contentBuilder.toString());

        // 在调用处传入question对象
        processOptionImage(optionAImage, question, "A", question::setOptionA);
        processOptionImage(optionBImage, question, "B", question::setOptionB);
        processOptionImage(optionCImage, question, "C", question::setOptionC);
        processOptionImage(optionDImage, question, "D", question::setOptionD);

        // 更新题目信息
        questionService.updateQuestion(question);
        return "redirect:/question/list";

    }

    private void processOptionImage(MultipartFile file,
                                    Question question, // 添加Question参数
                                    String option,
                                    java.util.function.Consumer<String> setter) throws IOException {
        if (!file.isEmpty() && !file.getOriginalFilename().isEmpty()) {
            // 获取当前选项内容
            String currentContent = switch (option) {
                case "A" -> question.getOptionA();
                case "B" -> question.getOptionB();
                case "C" -> question.getOptionC();
                case "D" -> question.getOptionD();
                default -> "";
            };

            // 存储文件并拼接新内容
            String filename = fileStorageService.storeFile(file, question.getId(), "option_" + option);
            String newContent = currentContent + "\n![图片](/uploads/questions/" + filename + ")";

            // 更新选项内容
            setter.accept(newContent);
        }
    }


    // 显示编辑页面（需传递枚举选项）
    @GetMapping("/edit/{id}")
    public String showEditPage(@PathVariable Long id, Model model) {
        Question question = questionService.getQuestionById(id);
        model.addAttribute("question", question);

        // 注入枚举选项
        model.addAttribute("questionTypes", List.of("SINGLE_CHOICE", "MULTIPLE_CHOICE"));
        model.addAttribute("difficultyLevels", List.of("EASY", "MEDIUM", "HARD"));

        return "question_edit";
    }

    // 处理编辑请求（增加参数校验）
    @PostMapping("/edit")
    @Transactional
    public String edit(
            @Valid @ModelAttribute Question question,
            BindingResult result,
            @RequestParam("contentImages") MultipartFile[] contentImages,
            @RequestParam("optionAImage") MultipartFile optionAImage,
            @RequestParam("optionBImage") MultipartFile optionBImage,
            @RequestParam("optionCImage") MultipartFile optionCImage,
            @RequestParam("optionDImage") MultipartFile optionDImage,
            @RequestParam(name = "deletedImages", required = false) List<String> deletedImages,
            Model model) throws IOException {

        if (result.hasErrors()) {
            model.addAttribute("questionTypes", List.of("SINGLE_CHOICE", "MULTIPLE_CHOICE"));
            model.addAttribute("difficultyLevels", List.of("EASY", "MEDIUM", "HARD"));
            return "question_edit";
        }

        // 处理被删除的图片
        if (deletedImages != null && !deletedImages.isEmpty()) {
            deletedImages.forEach(filename -> {
                fileStorageService.deleteFile(filename);
                // 从内容中移除图片引用
                question.setContent(question.getContent().replaceAll("!\\[图片]\\(.*" + filename + "\\)", ""));
                question.setOptionA(question.getOptionA().replaceAll("!\\[图片]\\(.*" + filename + "\\)", ""));
                question.setOptionB(question.getOptionA().replaceAll("!\\[图片]\\(.*" + filename + "\\)", ""));
                question.setOptionC(question.getOptionA().replaceAll("!\\[图片]\\(.*" + filename + "\\)", ""));
                question.setOptionD(question.getOptionA().replaceAll("!\\[图片]\\(.*" + filename + "\\)", ""));
            });
        }

        // 处理新内容图片（保留原有内容）
        StringBuilder contentBuilder = new StringBuilder(question.getContent());
        for (MultipartFile file : contentImages) {
            if (!file.isEmpty()) {
                String filename = fileStorageService.storeFile(file, question.getId(), "content");
                contentBuilder.append("\n![图片](/uploads/questions/").append(filename).append(")");
            }
        }
        question.setContent(contentBuilder.toString());

        // 处理选项图片（保留原有内容）
        processOptionImage(optionAImage, question, "A", question::setOptionA);
        processOptionImage(optionBImage, question, "B", question::setOptionB);
        processOptionImage(optionCImage, question, "C", question::setOptionC);
        processOptionImage(optionDImage, question, "D", question::setOptionD);

        try {
            questionService.updateQuestion(question);
            return "redirect:/question/list";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("questionTypes", List.of("SINGLE_CHOICE", "MULTIPLE_CHOICE"));
            model.addAttribute("difficultyLevels", List.of("EASY", "MEDIUM", "HARD"));
            return "question_edit";
        }
    }

    // 删除逻辑保持不变
    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            if (questionService.isQuestionInUse(id)) {
                return ResponseEntity.badRequest()
                        .body("该题目已被考试使用，无法删除");
            }
            questionService.deleteQuestion(id);
            fileStorageService.deleteFilesByQuestionId(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("删除失败：" + e.getMessage());
        }
    }

}