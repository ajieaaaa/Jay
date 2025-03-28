package edu.ntu.ai.exam.controller;

import edu.ntu.ai.exam.model.Exam;
import edu.ntu.ai.exam.model.Score;
import edu.ntu.ai.exam.model.User;
import edu.ntu.ai.exam.service.ExamService;
import edu.ntu.ai.exam.service.ScoreService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/exam")
public class ExamController {
    private final ExamService examService;
    private final ScoreService scoreService;

    @Autowired
    public ExamController(ExamService examService, ScoreService scoreService) {
        this.examService = examService;
        this.scoreService = scoreService;
    }


    // 教师的考试管理页面
    @GetMapping("/manage")
    public String showExamManagePage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"TEACHER".equals(user.getRole())) {
            return "redirect:/user/login";
        }
        List<Exam> exams = examService.getAllExams();
        model.addAttribute("exams", exams);
        model.addAttribute("user", user);
        return "exam_manage";
    }

    // 学生的考试列表页面
    @GetMapping
    public String examPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", user);
        if ("STUDENT".equals(user.getRole())) {
            List<Exam> availableExams = examService.getAvailableExams(user.getId());
            model.addAttribute("availableExams", availableExams);
            return "exam";
        } else {
            return "redirect:/exam/manage";
        }
    }

    // 创建新考试
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createExam(@RequestParam String examName,
                                        @RequestParam int questionCount,
                                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"TEACHER".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无权限创建考试");
        }

        try {
            Exam exam = examService.generateExam(examName, questionCount, user.getId());
            return ResponseEntity.ok(exam);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建考试失败：" + e.getMessage());
        }
    }

    @GetMapping("/{examId}/details")
    public String viewExamDetails(@PathVariable Long examId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        try {
            // 获取考试基本信息
            Exam exam = examService.getExamById(examId);
            if (exam == null) {
                throw new IllegalStateException("考试不存在");
            }

            // 获取考试统计信息
            Map<String, Object> statistics = scoreService.getExamStatistics(examId);

            // 获取所有学生的成绩
            List<Score> scores = scoreService.getScoresByExamId(examId);

            // 准备详细信息
            Map<String, Object> examDetails = new HashMap<>();
            examDetails.put("examName", exam.getExamName());
            examDetails.put("questionCount", exam.getQuestionCount());
            examDetails.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(exam.getCreateTime()));
            examDetails.put("creatorId", exam.getCreatorId());

            // 添加统计信息
            examDetails.put("totalStudents", statistics.get("totalStudents"));
            examDetails.put("averageScore", statistics.get("averageScore"));
            examDetails.put("highestScore", statistics.get("highestScore"));
            examDetails.put("lowestScore", statistics.get("lowestScore"));
            examDetails.put("passRate", statistics.get("passRate"));
            examDetails.put("scoreDistribution", statistics.get("scoreDistribution"));

            // 准备学生成绩列表
            List<Map<String, Object>> studentScores = scores.stream()
                    .map(score -> {
                        Map<String, Object> scoreMap = new HashMap<>();
                        scoreMap.put("userName", score.getUserName());
                        scoreMap.put("score", score.getScore());
                        scoreMap.put("actualScore", score.getActualScore());
                        scoreMap.put("totalQuestions", score.getTotalQuestions());
                        scoreMap.put("submitTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(score.getSubmitTime()));
                        return scoreMap;
                    })
                    .collect(Collectors.toList());

            model.addAttribute("examDetails", examDetails);
            model.addAttribute("studentScores", studentScores);

            return "exam_details";
        } catch (Exception e) {
            model.addAttribute("error", "获取考试详情失败：" + e.getMessage());
            return "error";
        }
    }
    // 开始考试页面
    @GetMapping("/{examId}/start")
    public String startExam(@PathVariable Long examId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/user/login";
        }

        try {
            if (examService.hasStudentTakenExam(examId, user.getId())) {
                return "redirect:/exam/" + examId + "/score";
            }

            Map<String, Object> examData = examService.getExamForStudent(examId);
            model.addAttribute("exam", examData.get("exam"));
            model.addAttribute("questions", examData.get("questions"));
            model.addAttribute("user", user);
            return "take_exam";
        } catch (Exception e) {
            model.addAttribute("error", "开始考试失败：" + e.getMessage());
            return "error";
        }
    }

    // 提交考试的接口
    @PostMapping("/{examId}/submit")
    @ResponseBody
    public ResponseEntity<?> submitExam(@PathVariable Long examId,
                                        @RequestBody Map<Long, String> answers,
                                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请先登录");
        }

        try {
            if (examService.hasStudentTakenExam(examId, user.getId())) {
                return ResponseEntity.badRequest().body("您已经提交过这个考试");
            }

            Map<String, Object> result = examService.submitAndScoreExam(examId, user.getId(), answers);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("提交答案失败：" + e.getMessage());
        }
    }

    @GetMapping("/{examId}/score")
    public String viewScore(@PathVariable Long examId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        try {
            // 获取考试信息
            Exam exam = examService.getExamById(examId);
            if (exam == null) {
                throw new IllegalStateException("考试不存在");
            }

            // 获取成绩信息
            Score score = examService.getStudentScore(examId, user.getId());
            if (score == null) {
                model.addAttribute("error", "未找到考试成绩");
                return "error";
            }

            // 创建成绩详情对象
            Map<String, Object> scoreDetails = new HashMap<>();
            scoreDetails.put("examName", exam.getExamName());
            scoreDetails.put("userName", user.getUsername());
            scoreDetails.put("score", score.getActualScore() + " 分");
            scoreDetails.put("scorePercentage", score.getScore() + "%");
            scoreDetails.put("questionCount", score.getTotalQuestions() + " 题");
            // 格式化提交时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            scoreDetails.put("submitTime", sdf.format(score.getSubmitTime()));

            model.addAttribute("scoreDetails", scoreDetails);
            return "exam_score";
        } catch (Exception e) {
            model.addAttribute("error", "获取成绩失败：" + e.getMessage());
            return "error";
        }
    }

    // 删除考试（仅教师）
    @PostMapping("/{examId}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteExam(@PathVariable Long examId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"TEACHER".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无权限删除考试");
        }

        try {
            examService.deleteExam(examId);
            return ResponseEntity.ok().body("考试删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除考试失败：" + e.getMessage());
        }
    }

    // 考试统计信息（仅教师）
    @GetMapping("/{examId}/statistics")
    public String examStatistics(@PathVariable Long examId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"TEACHER".equals(user.getRole())) {
            return "redirect:/user/login";
        }

        try {
            Map<String, Object> statistics = examService.getExamStatistics(examId);
            model.addAttribute("statistics", statistics);
            model.addAttribute("user", user);
            return "exam_statistics";
        } catch (Exception e) {
            model.addAttribute("error", "获取统计信息失败：" + e.getMessage());
            return "error";
        }
    }
}