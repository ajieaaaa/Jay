
// 独立工具类
package edu.ntu.ai.exam.util;

import java.util.List;

public class ContentUtils {
    public static String cleanContent(String content, List<String> deletedImages) {
        for (String filename : deletedImages) {
            String pattern = "!\\[图片]\\(.*?" + filename + "\\)";
            content = content.replaceAll(pattern, "");
        }
        return content.trim().replaceAll("\n+", "\n");
    }
}