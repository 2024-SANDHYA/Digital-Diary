package com.sandhya.digital_diary.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Controller
public class DiaryController {

    private static final String ENTRY_DIR = "diary_entries/";

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @PostMapping("/save")
    public String saveEntry(@RequestParam String date, @RequestParam String content, Model model) throws IOException {
        Files.createDirectories(Paths.get(ENTRY_DIR));
        Files.write(Paths.get(ENTRY_DIR + date + ".txt"), content.getBytes());
        model.addAttribute("msg", "Saved entry for " + date);
        return "home";
    }

    @GetMapping("/load")
    public String loadEntry(@RequestParam String date, Model model) throws IOException {
        Path file = Paths.get(ENTRY_DIR + date + ".txt");
        if (Files.exists(file)) {
            String content = Files.readString(file);
            model.addAttribute("loadedDate", date);
            model.addAttribute("loadedContent", content);
        } else {
            model.addAttribute("msg", "No entry found for " + date);
        }
        return "home";
    }

@GetMapping("/search")
public String search(@RequestParam(required = false) String keyword, Model model) throws IOException {
    if (keyword == null || keyword.trim().isEmpty()) {
        return "search"; // just show the page without doing anything
    }

    List<String> results = new ArrayList<>();
    Files.createDirectories(Paths.get("diary_entries/"));
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("diary_entries/"))) {
        for (Path file : stream) {
            String content = Files.readString(file);
            if (content.toLowerCase().contains(keyword.toLowerCase())) {
                results.add(file.getFileName().toString().replace(".txt", ""));
            }
        }
    }
    model.addAttribute("results", results);
    return "search";
}

    @GetMapping("/export")
public void exportToPdf(@RequestParam String date, HttpServletResponse response) throws IOException {
    Path file = Paths.get("diary_entries/" + date + ".txt");
    if (!Files.exists(file)) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Entry not found.");
        return;
    }

    String content = Files.readString(file);

    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=DiaryEntry_" + date + ".pdf");

    try (OutputStream out = response.getOutputStream()) {
        com.lowagie.text.Document document = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, out);
        document.open();
        document.add(new com.lowagie.text.Paragraph("Diary Entry - " + date));
        document.add(new com.lowagie.text.Paragraph("\n" + content));
        document.close();
    } catch (Exception e) {
        response.sendError(500, "Error generating PDF: " + e.getMessage());
    }
}

}
