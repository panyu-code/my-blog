import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getArticleList, getArticleDetail, searchArticles } from '../api/article'

export const useArticleStore = defineStore('article', () => {
  const articleList = ref([])
  const articleDetail = ref(null)
  const total = ref(0)
  const loading = ref(false)
  const categories = ref([])
  const tags = ref([])

  // 获取文章列表
  const getArticleListAction = async (params, append = false) => {
    loading.value = true
    try {
      const res = await getArticleList(params)
      if (append && params.pageNum > 1) {
        // 追加模式：创建新数组但保持数据连续性
        const newList = res.data.list || []
        articleList.value = articleList.value.concat(newList)
      } else {
        // 替换模式：清空后设置新数据
        articleList.value = res.data.list || []
      }
      total.value = res.data.total
      return res
    } catch (error) {
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取文章详情
  const getArticleDetailAction = async (id) => {
    loading.value = true
    try {
      const res = await getArticleDetail(id)
      articleDetail.value = res.data
      return res
    } catch (error) {
      throw error
    } finally {
      loading.value = false
    }
  }

  // 搜索文章
  const searchArticlesAction = async (keyword) => {
    loading.value = true
    try {
      const res = await searchArticles(keyword)
      articleList.value = res.data.list
      total.value = res.data.total
      return res
    } catch (error) {
      throw error
    } finally {
      loading.value = false
    }
  }

  // 设置分类列表
  const setCategoriesAction = (list) => {
    categories.value = list
  }

  // 设置标签列表
  const setTagsAction = (list) => {
    tags.value = list
  }

  return {
    articleList,
    articleDetail,
    total,
    loading,
    categories,
    tags,
    getArticleListAction,
    getArticleDetailAction,
    searchArticlesAction,
    setCategoriesAction,
    setTagsAction
  }
})
