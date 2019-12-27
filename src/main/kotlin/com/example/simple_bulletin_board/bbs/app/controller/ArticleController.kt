package com.example.simple_bulletin_board.bbs.app.controller

import com.example.simple_bulletin_board.bbs.app.request.ArticleRequest
import com.example.simple_bulletin_board.bbs.domain.entity.Article
import com.example.simple_bulletin_board.bbs.domain.repository.ArticleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class ArticleController {

    @Autowired
    lateinit var articleRepository: ArticleRepository

    @PostMapping
    @ResponseBody
    fun registerArticle(@ModelAttribute articleRequest: ArticleRequest): String {
        articleRepository.save(
                Article(
                        articleRequest.id,
                        articleRequest.name,
                        articleRequest.title,
                        articleRequest.contents,
                        articleRequest.articleKey)
        )
        return "Saved"
    }
}