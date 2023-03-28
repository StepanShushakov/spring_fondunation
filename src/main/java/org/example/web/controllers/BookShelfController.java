package org.example.web.controllers;

import org.apache.log4j.Logger;
import org.example.app.exceptions.InvalidUploadDataException;
import org.example.app.services.BookService;
import org.example.web.dto.Book;
import org.example.web.dto.BookIdToRemove;
import org.example.web.dto.QueryRegex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.*;

@Controller
@RequestMapping(value = "/books")
@Scope("singleton")
public class BookShelfController {

    private Logger logger = Logger.getLogger(BookShelfController.class);
    private BookService bookService;

    @Autowired
    public BookShelfController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/shelf")
    public String books(Model model) {
        logger.info(this.toString());
        model.addAttribute("book", new Book());
        model.addAttribute("bookIdToRemove", new BookIdToRemove());
        model.addAttribute("bookList", bookService.getAllBooks());
        model.addAttribute("queryRegex", new QueryRegex());
        return "book_shelf";
    }

    @PostMapping("/save")
    public String saveBook(@Valid Book book, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("book", book);
            model.addAttribute("bookIdToRemove", new BookIdToRemove());
            model.addAttribute("bookList", bookService.getAllBooks());
            model.addAttribute("queryRegex", new QueryRegex());
            return "book_shelf";
        }
        if (!book.getAuthor().isEmpty()
                || !book.getTitle().isEmpty()
                || (book.getSize() != null && book.getSize() > 0)) {
            bookService.saveBook(book);
        }
        logger.info("current repository size: " + bookService.getAllBooks().size());
        return "redirect:/books/shelf";
    }

    @PostMapping("/remove")
    public String removeBook(@Valid BookIdToRemove bookIdToRemove, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("book", new Book());
            model.addAttribute("bookList", bookService.getAllBooks());
            model.addAttribute("queryRegex", new QueryRegex());
            return "book_shelf";
        } else {
            bookService.removeBookById(bookIdToRemove.getId());
            return "redirect:/books/shelf";
        }
    }

    @PostMapping("/removeByRegex")
    public String removeByRegex(@Valid QueryRegex queryRegex, BindingResult bindingResult, Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("book", new Book());
            model.addAttribute("bookList", bookService.getAllBooks());
            model.addAttribute("bookIdToRemove", new BookIdToRemove());
            return "book_shelf";
        }
        bookService.removeByRegex(queryRegex.getText());
        return "redirect:/books/shelf";
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws Exception {

        String name = file.getOriginalFilename();
        if (!name.isEmpty()) {
            byte[] bytes = file.getBytes();

            //create dir
            String rootPath = System.getProperty("catalina.home");
            File dir = new File(rootPath + File.separator + "external_uploads");
            if (!dir.exists()) {
                dir.mkdir();
            }

            //create file
            File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
            stream.write(bytes);
            stream.close();

            logger.info("new file saver at: " + serverFile.getAbsolutePath());

            return "redirect:/books/shelf";
        };
        logger.info("upload FAIL redirect back book list");
        throw new InvalidUploadDataException("file not found");
    }

    @ExceptionHandler(InvalidUploadDataException.class)
    public String handleError(Model model, InvalidUploadDataException exception) {
        model.addAttribute("errorMessage", exception.getMessage());
        return "errors/500";
    }
}
