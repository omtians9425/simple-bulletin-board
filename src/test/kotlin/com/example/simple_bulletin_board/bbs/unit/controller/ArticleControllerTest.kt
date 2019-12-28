package com.example.simple_bulletin_board.bbs.unit.controller

import com.example.simple_bulletin_board.bbs.app.controller.ArticleController
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest
class ArticleControllerTest {

    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var target: ArticleController

    @Before
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(target).build()
    }

    @Test
    fun registerArticleTest() {
        mockMvc.perform(
                post("/")
                        .param("name", "test")
                        .param("title", "test")
                        .param("contents", "test")
                        .param("articleKey", "test"))
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/"))
    }

    @Test
    fun getArticleListTest() {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk)
                .andExpect(model().attributeExists("articles"))
                .andExpect(view().name("index")) // not content()#string(). this is for api (not view)
    }

    @Test
    fun getArticleEdit_notExists_redirect() {
        mockMvc.perform(get("/edit/" + 0))
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/"))
    }

    @Test
    @Sql(statements = ["INSERT INTO article (name, title, contents, article_key) VALUES ('test', 'test', 'test', 'test')"])
    fun getArticleEdit_exists_edit() {
        val lastArticle = target.articleRepository.findAll().last()

        mockMvc.perform(get("/edit/" + lastArticle.id))
                .andExpect(status().isOk)
                .andExpect(view().name("edit"))
    }

    @Test
    fun updateArticle_notExists_redirectToIndex() {
        mockMvc.perform(post("/update")
                .param("id", "0")
                .param("name", "test")
                .param("title", "test")
                .param("contents", "test")
                .param("articleKey", "err.")
        )
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/"))
    }

    @Test
    @Sql(statements = ["INSERT INTO article (name, title, contents, article_key, register_at, update_at) VALUES ('test', 'test', ' test', 'test', now(), now());"])
    fun updateArticle_notKeyMatch_redirectToEdit() {
        val latestArticle = target.articleRepository.findAll().last()

        mockMvc.perform(post("/update")
                .param("id", latestArticle.id.toString())
                .param("name", latestArticle.name)
                .param("title", latestArticle.title)
                .param("contents", latestArticle.contents)
                .param("articleKey", "differentKey")
        )
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/edit/${latestArticle.id}"))
    }

    @Test
    @Sql(statements = ["INSERT INTO article (name, title, contents, article_key, register_at, update_at) VALUES ('test', 'test', ' test', 'test', now(), now());"])
    fun updateArticle_keyMatch_updateAndRedirectToIndex() {
        val latestArticle = target.articleRepository.findAll().last()

        mockMvc.perform(post("/update")
                .param("id", latestArticle.id.toString())
                .param("name", "updated")
                .param("title", latestArticle.title)
                .param("contents", latestArticle.contents)
                .param("articleKey", latestArticle.articleKey)
        )
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/"))

        val updated = target.articleRepository.findAll().last()
        assertEquals("updated", updated.name)
    }

    @Test
    fun getDeleteConfirm_notExist_redirectToIndex() {
        mockMvc.perform(get("/delete/confirm/" + 0))
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/"))
    }

    @Test
    @Sql(statements = ["INSERT INTO article (name, title, contents, article_key, register_at, update_at) VALUES ('test', 'test', 'test', 'test', now(), now())"])
    fun getDeleteConfirm_exist_deleteAndToDeleteScreen() {
        val latestArticle = target.articleRepository.findAll().last()

        mockMvc.perform(get("/delete/confirm/" + latestArticle.id))
                .andExpect(status().isOk)
                .andExpect(view().name("delete_confirm"))
    }


}