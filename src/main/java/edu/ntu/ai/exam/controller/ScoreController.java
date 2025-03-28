package edu.ntu.ai.exam.controller;

import edu.ntu.ai.exam.model.Score;
import edu.ntu.ai.exam.service.ExamService;
import edu.ntu.ai.exam.service.ScoreService;
import edu.ntu.ai.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/score")
public class ScoreController {
    @Autowired
    private ScoreService scoreService;


    @GetMapping("/list")
    public String list(Model model) {
        List<Score> scores = scoreService.getAllScores();
        model.addAttribute("scores", scores);
        return "score_list";
    }

}