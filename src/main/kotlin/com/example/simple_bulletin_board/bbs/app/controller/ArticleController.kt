package com.example.simple_bulletin_board.bbs.app.controller

import com.example.simple_bulletin_board.bbs.app.request.ArticleRequest
import com.example.simple_bulletin_board.bbs.domain.entity.Article
import com.example.simple_bulletin_board.bbs.domain.repository.ArticleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.*

@Controller
class ArticleController {

    @Autowired
    lateinit var articleRepository: ArticleRepository

    @PostMapping
    fun registerArticle(
            @ModelAttribute articleRequest: ArticleRequest,
            redirectAttributes: RedirectAttributes
    ): String {
        articleRepository.save(
                Article(
                        articleRequest.id,
                        articleRequest.name,
                        articleRequest.title,
                        articleRequest.contents,
                        articleRequest.articleKey)
        )
        redirectAttributes.addFlashAttribute("message", MESSAGE_REGISTER_NORMAL)
        return "redirect:/" // this enables redirect
    }

    @GetMapping("/")
    fun getArticleList(model: Model): String {
        model.addAttribute("articles", articleRepository.findAll()) // model is used for UI
        return "index" // means "index.html"
    }

    @GetMapping("/edit/{id}")
    fun getArticleEdit(@PathVariable id: Int, model: Model): String {
        return if (articleRepository.existsById(id)) {
            //attribute name is used by html
            model.addAttribute("article", articleRepository.findById(id))
            "edit" // means "edit.html"
        } else {
            "redirect:/"
        }
    }

    @PostMapping("/update")
    fun updateArticle(articleRequest: ArticleRequest): String {
        if (!articleRepository.existsById(articleRequest.id)) {
            return "redirect:/"
        }

        val article: Article = articleRepository.findById(articleRequest.id).get()

        if (articleRequest.articleKey != article.articleKey) {
            return "redirect:/edit/${articleRequest.id}"
        }

        article.also {
            it.name = articleRequest.name
            it.title = articleRequest.title
            it.contents = articleRequest.contents
            it.updateAt = Date()
        }
        articleRepository.save(article)

        return "redirect:/"
    }

    @GetMapping("/delete/confirm/{id}")
    fun getDeleteConfirm(@PathVariable id: Int, model: Model): String {
        if (!articleRepository.existsById(id)) {
            return "redirect:/"
        }
        model.addAttribute("article", articleRepository.findById(id).get())
        return "delete_confirm"
    }

    @PostMapping("/delete")
    fun deleteArticle(@ModelAttribute articleRequest: ArticleRequest): String {
        if (!articleRepository.existsById(articleRequest.id)) {
            return "redirect:/"
        }
        val article = articleRepository.findById(articleRequest.id).get()
        if (article.articleKey != articleRequest.articleKey) {
            return "redirect:/delete/confirm/${article.id}"
        }

        articleRepository.deleteById(articleRequest.id)
        return "redirect:/"
    }

    companion object {
        const val MESSAGE_REGISTER_NORMAL = "正常に投稿できました"
    }
}