package com.panyu.mybolg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panyu.mybolg.entity.Article;

import java.util.List;
import java.util.Map;

public interface ArticleService extends IService<Article> {

    /**
     * 分页查询文章列表，并填充作者、分类、标签信息
     */
    Page<Article> listWithDetails(Integer pageNum, Integer pageSize, String title, Long categoryId, Integer status);

    /**
     * 管理端分页查询文章列表（包含所有审核状态的文章），并填充作者、分类、标签信息
     */
    Page<Article> listWithDetailsForAdmin(Integer pageNum, Integer pageSize, String title, Long categoryId, Integer status);

    /**
     * 用户端分页查询自己的文章列表，并填充作者、分类、标签信息
     */
    Page<Article> listUserArticles(Long authorId, Integer pageNum, Integer pageSize, String title, Integer status);

    /**
     * 根据ID获取文章详情，并填充作者、分类、标签信息
     */
    Article getDetailById(Long id);

    /**
     * 搜索文章
     */
    Page<Article> searchArticles(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 保存文章及其标签关联
     */
    boolean saveWithTags(Article article, List<String> tags);

    /**
     * 更新文章及其标签关联
     */
    boolean updateWithTags(Article article, List<String> tags);

    /**
     * 删除文章及其关联数据（标签、评论、封面）
     */
    boolean deleteWithRelations(Long id);

    /**
     * 增加浏览量
     */
    boolean increaseViewCount(Long id);

    /**
     * 增加点赞量
     */
    boolean increaseLikeCount(Long id);

    /**
     * 填充文章列表的详细信息（作者、分类、标签）
     */
    void fillArticleDetails(List<Article> articles);

    /**
     * 填充文章列表的评论数量
     */
    void fillArticleCommentCounts(List<Article> articles);

    /**
     * 发送邮件通知作者审核结果
     */
    void notifyAuthorAuditResult(Article article);

    /**
     * 发送邮件通知admin有新文章待审核
     */
    void notifyAdminNewArticle(Article article);
}
