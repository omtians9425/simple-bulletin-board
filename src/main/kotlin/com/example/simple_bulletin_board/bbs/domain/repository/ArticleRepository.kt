package com.example.simple_bulletin_board.bbs.domain.repository

import com.example.simple_bulletin_board.bbs.domain.entity.Article
import org.springframework.data.jpa.repository.JpaRepository

interface ArticleRepository : JpaRepository<Article, Int>