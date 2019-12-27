package com.example.simple_bulletin_board.bbs.unit.controller

import com.example.simple_bulletin_board.bbs.app.controller.ArticleController
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
                .andExpect(status().isOk)
                .andExpect(content().string("Saved"))
    }
}