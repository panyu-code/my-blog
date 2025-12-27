package com.panyu.mybolg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panyu.mybolg.entity.*;
import com.panyu.mybolg.mapper.ArticleMapper;
import com.panyu.mybolg.service.*;
import com.panyu.mybolg.utils.RustFsUtil;
import com.panyu.mybolg.vo.TagVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private UserService userService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private ArticleTagService articleTagService;

    @Resource
    private TagService tagService;

    @Resource
    private CommentService commentService;

    @Resource
    private UploadService uploadService;

    @Resource
    private RustFsUtil rustFsUtil;

    @Override
    public Page<Article> listWithDetails(Integer pageNum, Integer pageSize, String title, Long categoryId, Integer status) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Article::getTitle, title);
        }
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }

        wrapper.orderByDesc(Article::getIsTop, Article::getCreateTime);
        Page<Article> result = page(page, wrapper);

        // 填充详细信息
        fillArticleDetails(result.getRecords());

        return result;
    }

    @Override
    public Article getDetailById(Long id) {
        Article article = getById(id);
        if (article == null) {
            return null;
        }

        // 填充详细信息
        List<Article> articles = new ArrayList<>();
        articles.add(article);
        fillArticleDetails(articles);

        return article;
    }

    @Override
    public Page<Article> searchArticles(String keyword, Integer pageNum, Integer pageSize) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.isNotBlank(keyword), Article::getTitle, keyword)
                .or()
                .like(StringUtils.isNotBlank(keyword), Article::getContent, keyword);
        wrapper.eq(Article::getStatus, 1);

        Page<Article> result = page(page, wrapper);

        // 填充详细信息
        fillArticleDetails(result.getRecords());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithTags(Article article, List<String> tags) {
        // 保存文章
        boolean saved = save(article);
        if (!saved) {
            return false;
        }

        // 保存标签关联
        saveTags(article.getId(), tags);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithTags(Article article, List<String> tags) {
        // 更新文章
        boolean updated = updateById(article);
        if (!updated) {
            return false;
        }

        // 更新标签关联
        saveTags(article.getId(), tags);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithRelations(Long id) {
        try {
            // 获取文章信息
            Article article = getById(id);
            if (article == null) {
                return false;
            }

            // 1. 删除文章-标签关联
            LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(ArticleTag::getArticleId, id);
            articleTagService.remove(tagWrapper);

            // 2. 删除文章评论
            LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.eq(Comment::getArticleId, id);
            commentService.remove(commentWrapper);

            // 3. 删除文章封面
            if (article.getCover() != null && !article.getCover().isEmpty()) {
                deleteCover(article.getCover());
            }

            // 4. 删除文章记录
            return removeById(id);
        } catch (Exception e) {
            throw new RuntimeException("删除文章失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean increaseViewCount(Long id) {
        Article article = getById(id);
        if (article == null) {
            return false;
        }
        article.setViewCount(article.getViewCount() + 1);
        return updateById(article);
    }

    @Override
    public boolean increaseLikeCount(Long id) {
        Article article = getById(id);
        if (article == null) {
            return false;
        }
        article.setLikeCount(article.getLikeCount() + 1);
        return updateById(article);
    }

    @Override
    public void fillArticleDetails(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        fillAuthorName(articles);
        fillCategoryName(articles);
        fillTags(articles);
    }

    /**
     * 填充文章作者名字
     */
    private void fillAuthorName(List<Article> articles) {
        // 收集所有作者ID
        List<Long> authorIds = articles.stream()
                .map(Article::getAuthorId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (authorIds.isEmpty()) {
            return;
        }

        // 批量查询用户
        List<User> users = userService.listByIds(authorIds);
        Map<Long, String> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        // 填充作者名字
        articles.forEach(article -> {
            if (article.getAuthorId() != null) {
                article.setAuthorName(userMap.get(article.getAuthorId()));
            }
        });
    }

    /**
     * 填充文章分类名称
     */
    private void fillCategoryName(List<Article> articles) {
        // 收集所有分类ID
        List<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (categoryIds.isEmpty()) {
            return;
        }

        // 批量查询分类
        List<Category> categories = categoryService.listByIds(categoryIds);
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 填充分类名称
        articles.forEach(article -> {
            if (article.getCategoryId() != null) {
                article.setCategoryName(categoryMap.get(article.getCategoryId()));
            }
        });
    }

    /**
     * 填充文章标签（Web端展示，包含颜色信息）
     */
    private void fillTags(List<Article> articles) {
        try {
            // 收集所有文章ID
            List<Long> articleIds = articles.stream()
                    .map(Article::getId)
                    .collect(Collectors.toList());

            // 查询文章-标签关联
            LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(ArticleTag::getArticleId, articleIds);
            List<ArticleTag> articleTags = articleTagService.list(wrapper);

            if (articleTags.isEmpty()) {
                articles.forEach(article -> article.setTagList(new ArrayList<>()));
                return;
            }

            // 收集所有标签ID
            List<Long> tagIds = articleTags.stream()
                    .map(ArticleTag::getTagId)
                    .distinct()
                    .collect(Collectors.toList());

            // 查询标签
            List<Tag> tags = tagService.listByIds(tagIds);

            // 构建标签ID -> Tag对象的映射
            Map<Long, Tag> tagMap = tags.stream()
                    .collect(Collectors.toMap(Tag::getId, tag -> tag));

            // 按文章ID分组，生成TagVO列表
            Map<Long, List<TagVO>> articleTagMap = articleTags.stream()
                    .collect(Collectors.groupingBy(
                            ArticleTag::getArticleId,
                            Collectors.mapping(
                                    at -> {
                                        Tag tag = tagMap.get(at.getTagId());
                                        return tag != null ? new TagVO(tag.getName(), tag.getColor()) : null;
                                    },
                                    Collectors.filtering(
                                            tagVO -> tagVO != null,
                                            Collectors.toList()
                                    )
                            )
                    ));

            // 填充标签
            articles.forEach(article -> {
                List<TagVO> articleTagList = articleTagMap.get(article.getId());
                article.setTagList(articleTagList != null ? articleTagList : new ArrayList<>());
            });
        } catch (Exception e) {
            // 异常时设置空数组
            articles.forEach(article -> article.setTagList(new ArrayList<>()));
            throw new RuntimeException("填充标签失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存文章标签关联
     */
    private void saveTags(Long articleId, List<String> tags) {
        if (articleId == null) {
            return;
        }

        // 删除旧的标签关联
        LambdaQueryWrapper<ArticleTag> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ArticleTag::getArticleId, articleId);
        articleTagService.remove(deleteWrapper);

        // 保存新的标签关联
        if (tags != null && !tags.isEmpty()) {
            List<ArticleTag> articleTags = new ArrayList<>();
            for (String tagIdStr : tags) {
                try {
                    Long tagId = Long.parseLong(tagIdStr);
                    ArticleTag articleTag = new ArticleTag();
                    articleTag.setArticleId(articleId);
                    articleTag.setTagId(tagId);
                    articleTags.add(articleTag);
                } catch (NumberFormatException e) {
                    // 忽略无效的标签ID
                }
            }

            if (!articleTags.isEmpty()) {
                articleTagService.saveBatch(articleTags);
            }
        }
    }

    /**
     * 删除封面文件
     */
    private void deleteCover(String coverUrl) {
        try {
            // 查询upload表中的封面记录
            LambdaQueryWrapper<Upload> uploadWrapper = new LambdaQueryWrapper<>();
            uploadWrapper.eq(Upload::getFileUrl, coverUrl);
            Upload upload = uploadService.getOne(uploadWrapper);

            if (upload != null) {
                // 删除文件
                if (upload.getFilePath() != null && !upload.getFilePath().isEmpty()) {
                    rustFsUtil.delete(upload.getFilePath());
                }

                // 删除upload表记录
                uploadService.removeById(upload.getId());
            } else {
                // 如果upload表中没有记录，尝试直接从URL提取key删除
                if (coverUrl.contains("/blog/")) {
                    String key = coverUrl.substring(coverUrl.indexOf("/blog/"));
                    rustFsUtil.delete(key);
                }
            }
        } catch (Exception e) {
            // 封面删除失败不影响整体流程
        }
    }
}
