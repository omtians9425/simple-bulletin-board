package com.example.simple_bulletin_board.bbs.unit.controller

import com.example.simple_bulletin_board.bbs.app.controller.ArticleController
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
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", ArticleController.MESSAGE_REGISTER_NORMAL))
    }

    @Test
    fun registerArticle_validationError() {
        mockMvc.perform(
                post("/")
                .param("name", "")
                .param("title", "")
                .param("contents", "")
                .param("articleKey", "")
        )
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attributeExists("errors"))
                .andExpect(flash().attributeExists("request"))
    }

    @Test
    fun getArticleListTest() {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk)
                .andExpect(model().attributeExists("page"))
                .andExpect(view().name("index")) // not content()#string(). this is for api (not view)
    }

    @Test
    fun getArticleEdit_notExists_redirect() {
        mockMvc.perform(get("/edit/" + 0))
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", ArticleController.MESSAGE_ARTICLE_DOES_NOT_EXISTS))
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
    fun updateArticle_validationError() {
        mockMvc.perform(post("/update"))
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/edit/0"))
                .andExpect(flash().attributeExists("errors"))
                .andExpect(flash().attributeExists("request"))
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
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", ArticleController.MESSAGE_ARTICLE_DOES_NOT_EXISTS))
                .andExpect(flash().attribute("alert_class", ArticleController.ALERT_CLASS_ERROR))
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
                .param("articleKey", "diff")
        )
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/edit/${latestArticle.id}"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", ArticleController.MESSAGE_ARTICLE_KEY_UNMATCH))
                .andExpect(flash().attribute("alert_class", ArticleController.ALERT_CLASS_ERROR))
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
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", ArticleController.MESSAGE_UPDATE_NORMAL))

        val updated = target.articleRepository.findAll().last()
        assertEquals("updated", updated.name)
    }

    @Test
    fun getDeleteConfirm_notExist_redirectToIndex() {
        mockMvc.perform(get("/delete/confirm/" + 0))
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", ArticleController.MESSAGE_ARTICLE_DOES_NOT_EXISTS))
                .andExpect(flash().attribute("alert_class", ArticleController.ALERT_CLASS_ERROR))
    }

    @Test
    @Sql(statements = ["INSERT INTO article (name, title, contents, article_key, register_at, update_at) VALUES ('test', 'test', 'test', 'test', now(), now())"])
    fun getDeleteConfirm_exist_deleteAndToDeleteScreen() {
        val latestArticle = target.articleRepository.findAll().last()

        mockMvc.perform(get("/delete/confirm/" + latestArticle.id))
                .andExpect(status().isOk)
                .andExpect(view().name("delete_confirm"))
    }

    @Test
    fun deleteArticle_validationError() {
        mockMvc.perform(
                post("/delete")
        )
        .andExpect(status().is3xxRedirection)
        .andExpect(view().name("redirect:/delete/confirm/0"))
        .andExpect(flash().attributeExists("errors"))
        .andExpect(flash().attributeExists("request"))
    }

    @Test
    fun deleteArticle_notExist_redirectToIndex() {
        mockMvc.perform(post("/delete")
                .param("id", "0")
                .param("name", "test")
                .param("title", "test")
                .param("contents", "test")
                .param("articleKey", ".err"))
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", ArticleController.MESSAGE_ARTICLE_DOES_NOT_EXISTS))
    }

    @Test
    @Sql(statements = ["INSERT INTO article (name, title, contents, article_key, register_at, update_at) VALUES ('test', 'test', 'test', 'test', now(), now())"])
    fun deleteArticle_notKeyMatch_redirectToDeleteConfirm() {
        val latestArticle = target.articleRepository.findAll().last()

        mockMvc.perform(post("/delete")
                .param("id", latestArticle.id.toString())
                .param("name", latestArticle.name)
                .param("title", latestArticle.title)
                .param("contents", latestArticle.contents)
                .param("articleKey", "diff"))
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/delete/confirm/${latestArticle.id}"))
                .andExpect(flash().attribute("message", ArticleController.MESSAGE_ARTICLE_KEY_UNMATCH))
                .andExpect(flash().attribute("alert_class", ArticleController.ALERT_CLASS_ERROR))
    }

    @Test
    @Sql(statements = ["INSERT INTO article (name, title, contents, article_key, register_at, update_at) VALUES ('test', 'test', 'test', 'test', now(), now())"])
    fun deleteArticle_keyMatch_deleteAndRedirectToIndex() {
        val latestArticle = target.articleRepository.findAll().last()

        mockMvc.perform(post("/delete")
                .param("id", latestArticle.id.toString())
                .param("name", latestArticle.name)
                .param("title", latestArticle.title)
                .param("contents", latestArticle.contents)
                .param("articleKey", latestArticle.articleKey))
                .andExpect(status().is3xxRedirection)
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", ArticleController.MESSAGE_DELETE_NORMAL))

        val result = target.articleRepository.findById(latestArticle.id).isPresent
        assertEquals(false, result)
    }
}