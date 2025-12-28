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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

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
        } else {
            // web端默认只查看已发布的文章，不包括草稿
            wrapper.eq(Article::getStatus, 1);
        }
        // web端只查看已审核的文章
        wrapper.eq(Article::getAuditStatus, 1);

        wrapper.orderByDesc(Article::getIsTop, Article::getCreateTime);
        Page<Article> result = page(page, wrapper);

        // 填充详细信息
        fillArticleDetails(result.getRecords());

        return result;
    }

    @Override
    public Page<Article> listWithDetailsForAdmin(Integer pageNum, Integer pageSize, String title, Long categoryId, Integer status) {
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
        // 管理端查看所有审核状态的文章（除了已删除的）
        // 不过滤审核状态，显示所有状态的文章
        wrapper.orderByDesc(Article::getIsTop, Article::getCreateTime);
        Page<Article> result = page(page, wrapper);

        // 填充详细信息
        fillArticleDetails(result.getRecords());

        return result;
    }

    @Override
    public Page<Article> listUserArticles(Long authorId, Integer pageNum, Integer pageSize, String title, Integer status) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        // 只查询指定作者的文章
        wrapper.eq(Article::getAuthorId, authorId);
        
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Article::getTitle, title);
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        // 不过滤审核状态，显示所有状态的文章
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

        wrapper.eq(Article::getStatus, 1)  // 只搜索已发布的文章
               .eq(Article::getAuditStatus, 1)  // 只搜索已审核的文章
               .and(StringUtils.isNotBlank(keyword), w -> w.like(Article::getTitle, keyword)
                   .or()
                   .like(Article::getContent, keyword));

        Page<Article> result = page(page, wrapper);

        // 填充详细信息
        fillArticleDetails(result.getRecords());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithTags(Article article, List<String> tags) {
        // 设置默认审核状态为待审核
        if (article.getAuditStatus() == null) {
            article.setAuditStatus(0); // 待审核
        }

        // 保存文章
        boolean saved = save(article);
        if (!saved) {
            return false;
        }

        // 保存标签关联
        saveTags(article.getId(), tags);

        // 发送邮件通知管理员（只在新发布且待审核时发送通知）
        if (article.getStatus() == 1 && article.getAuditStatus() == 0) {
            notifyAdminNewArticle(article);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithTags(Article article, List<String> tags) {
        // 保存更新前的文章信息用于对比
        Article oldArticle = getById(article.getId());

        // 如果文章状态是草稿，则审核状态应设为已审核（因为草稿不需要审核）
        if (article.getStatus() != null && article.getStatus() == 0) {
            article.setAuditStatus(1); // 直接设置为已审核
        }

        // 更新文章
        boolean updated = updateById(article);
        if (!updated) {
            return false;
        }

        // 更新标签关联
        saveTags(article.getId(), tags);

        // 如果是新发布的文章（从草稿状态变为发布状态）且待审核，发送通知
        if (oldArticle != null &&
                oldArticle.getStatus() == 0 &&
                article.getStatus() == 1 &&
                article.getAuditStatus() == 0) {
            notifyAdminNewArticle(article);
        }
        
        // 如果文章从发布状态变为草稿状态，发送通知（可选）
        if (oldArticle != null &&
                oldArticle.getStatus() == 1 &&
                article.getStatus() == 0) {
            // 可以添加相应的处理逻辑
        }

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
     * 填充文章作者名字和头像
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
        Map<Long, String> nameMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, String> avatarMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getAvatar));

        // 填充作者名字和头像
        articles.forEach(article -> {
            if (article.getAuthorId() != null) {
                article.setAuthorName(nameMap.get(article.getAuthorId()));
                article.setAuthorAvatar(avatarMap.get(article.getAuthorId()));
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
                // 如果上upload表中没有记录，尝试直接从上URL提取key删除
                if (coverUrl.contains("/blog/")) {
                    String key = coverUrl.substring(coverUrl.indexOf("/blog/"));
                    rustFsUtil.delete(key);
                }
            }
        } catch (Exception e) {
            // 封面删除失败不影响整体流程
        }
    }

    @Override
    public void notifyAuthorAuditResult(Article article) {
        try {
            User author = userService.getById(article.getAuthorId());
            if (author == null || author.getEmail() == null || author.getEmail().isEmpty()) {
                return;
            }

            String subject = "文章审核结果通知";
            String text;
            if (article.getAuditStatus() == 1) {
                text = String.format("您好 %s,\n\n" +
                                "您发布的文章《%s》已通过审核，现在已显示在博客中。\n\n" +
                                "感谢您的投稿！\n\n" +
                                "此为系统自动发送，请勿回复。",
                        author.getNickname(), article.getTitle());
            } else {
                text = String.format("您好 %s,\n\n" +
                                "很遗憾，您发布的文章《%s》未通过审核。\n\n" +
                                "拒绝理由：%s\n\n" +
                                "请修改后重新提交。\n\n" +
                                "此为系统自动发送，请勿回复。",
                        author.getNickname(), article.getTitle(),
                        article.getAuditReason() != null && !article.getAuditReason().isEmpty() ?
                                article.getAuditReason() : "内容不符合要求");
            }

            sendEmail(author.getEmail(), subject, text);
        } catch (Exception e) {
            System.err.println("发送审核结果邮件失败: " + e.getMessage());
        }
    }

    @Override
    public void notifyAdminNewArticle(Article article) {
        try {
            // 获取admin账号
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, "admin");
            User admin = userService.getOne(wrapper);
            if (admin == null || admin.getEmail() == null || admin.getEmail().isEmpty()) {
                return;
            }

            User author = userService.getById(article.getAuthorId());
            String authorName = author != null ? author.getNickname() : "未知用户";

            String subject = "有新的文章待审核";
            String text = String.format("您好,\n\n" +
                            "有新的文章需要审核：\n\n" +
                            "标题：%s\n" +
                            "作者：%s\n" +
                            "摘要：%s\n\n" +
                            "请及时登录后台进行审核。\n\n" +
                            "此为系统自动发送，请勿回复。",
                    article.getTitle(), authorName, article.getSummary());

            sendEmail(admin.getEmail(), subject, text);
        } catch (Exception e) {
            System.err.println("发送新文章通知邮件失败: " + e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(emailFrom);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("发送邮件失败: " + e.getMessage());
        }
    }
}
