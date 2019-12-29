package com.example.simple_bulletin_board.bbs.app.controller

import com.example.simple_bulletin_board.bbs.app.request.ArticleRequest
import com.example.simple_bulletin_board.bbs.domain.entity.Article
import com.example.simple_bulletin_board.bbs.domain.repository.ArticleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.*

@Controller
class ArticleController {

    @Autowired
    lateinit var articleRepository: ArticleRepository

    @PostMapping("/")
    fun registerArticle(
            @Validated @ModelAttribute articleRequest: ArticleRequest,
            result: BindingResult, // result of validation
            redirectAttributes: RedirectAttributes
    ): String {
        // when validation error
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result)
            redirectAttributes.addFlashAttribute("request", articleRequest)

            return "redirect:/"
        }
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
    fun getArticleList(
            @ModelAttribute articleRequest: ArticleRequest,
            @RequestParam(value = "page", defaultValue = "0", required = false) page: Int,
            model: Model
    ): String {

        val pageable = PageRequest.of(
                page,
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "updateAt")
                        .and(Sort.by(Sort.Direction.ASC, "id"))
        )

        if (model.containsAttribute("errors")) {
            val key = BindingResult.MODEL_KEY_PREFIX + "articleRequest" // thymeleaf references error info by this format
            println("error: $key, ${BindingResult.MODEL_KEY_PREFIX}")
            model.addAttribute(key, model.asMap()["errors"]) // get value
        }

        if (model.containsAttribute("request")) {
            // pass the request contents so that view can reproduce them at the time of validation error
            model.addAttribute("articleRequest", model.asMap()["request"]) // get value
        }

        val articles: Page<Article> = articleRepository.findAll(pageable)
        model.addAttribute("page", articles) // model is used for UI
        return "index" // means "index.html"
    }

    @GetMapping("/edit/{id}")
    fun getArticleEdit(
            @PathVariable id: Int,
            model: Model,
            redirectAttributes: RedirectAttributes
    ): String {
        return if (articleRepository.existsById(id)) {
            if (model.containsAttribute("request")) {
                model.addAttribute("article", model.asMap()["request"])
            } else {
                //attribute name is used by html
                model.addAttribute("article", articleRepository.findById(id).get())
            }

            if (model.containsAttribute("errors")) {
                val key = BindingResult.MODEL_KEY_PREFIX + "article"
                model.addAttribute(key, model.asMap()["errors"])
            }

            "edit" // means "edit.html"
        } else {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_DOES_NOT_EXISTS)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            "redirect:/"
        }
    }

    @PostMapping("/update")
    fun updateArticle(
            @Validated articleRequest: ArticleRequest,
            result: BindingResult, // [getArticleEdit] result
            redirectAttributes: RedirectAttributes
    ): String {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result)
            redirectAttributes.addFlashAttribute("request", articleRequest)

            return "redirect:/edit/${articleRequest.id}"
        }

        if (!articleRepository.existsById(articleRequest.id)) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_DOES_NOT_EXISTS)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/"
        }

        val article: Article = articleRepository.findById(articleRequest.id).get()

        if (articleRequest.articleKey != article.articleKey) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_KEY_UNMATCH)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/edit/${articleRequest.id}"
        }

        article.also {
            it.name = articleRequest.name
            it.title = articleRequest.title
            it.contents = articleRequest.contents
            it.updateAt = Date()
        }
        articleRepository.save(article)
        redirectAttributes.addFlashAttribute("message", MESSAGE_UPDATE_NORMAL)

        return "redirect:/"
    }

    @GetMapping("/delete/confirm/{id}")
    fun getDeleteConfirm(
            @PathVariable id: Int,
            model: Model,
            redirectAttributes: RedirectAttributes
    ): String {
        if (!articleRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_DOES_NOT_EXISTS)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/"
        }
        model.addAttribute("article", articleRepository.findById(id).get())

        val key = BindingResult.MODEL_KEY_PREFIX + "article"
        if (model.containsAttribute("errors")) {
            model.addAttribute(key, model.asMap()["errors"])
        }
        return "delete_confirm"
    }

    @PostMapping("/delete")
    fun deleteArticle(
            @Validated @ModelAttribute articleRequest: ArticleRequest,
            result: BindingResult,
            redirectAttributes: RedirectAttributes
    ): String {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result)
            redirectAttributes.addFlashAttribute("request", articleRequest)
            return "redirect:/delete/confirm/${articleRequest.id}"
        }

        if (!articleRepository.existsById(articleRequest.id)) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_DOES_NOT_EXISTS)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/"
        }
        val article = articleRepository.findById(articleRequest.id).get()
        if (article.articleKey != articleRequest.articleKey) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_KEY_UNMATCH)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/delete/confirm/${article.id}"
        }

        articleRepository.deleteById(articleRequest.id)
        redirectAttributes.addFlashAttribute("message", MESSAGE_DELETE_NORMAL)
        return "redirect:/"
    }

    companion object {
        const val MESSAGE_REGISTER_NORMAL = "正常に投稿できました"
        const val MESSAGE_ARTICLE_DOES_NOT_EXISTS = "対象の記事が見つかりませんでした"
        const val MESSAGE_UPDATE_NORMAL = "正常に更新しました"
        const val MESSAGE_ARTICLE_KEY_UNMATCH = "投稿KEYが一致しません"
        const val MESSAGE_DELETE_NORMAL = "正常に削除しました"

        const val ALERT_CLASS_ERROR = "alert-error"

        const val PAGE_SIZE = 10
    }
}