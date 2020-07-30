// blog 상태 관리 모듈
import BlogService from '@/services/blog-service'

export const blog = {
  namespaced: true,
  state: {
    //블로그 정보
    bid: null,
    // blogData: null,
    blogData: {
      bid: null,
      btitle: null,
      bsubtitle: null,
      bcontent: null,
      hashtags: null,
      
    },

    //전체 카테고리
    categoryListData: [],

    //전체 글 목록
    postListData: [],

    //블로그 게시글 상세정보
    postData: {
      pid: '',
      lcid: '1', /***나중에 수정***/
      mcid: '1', /***나중에 수정***/
      ptitle: '',
      pcontent: '',
      author: '',
      postTime: '',
      update_time: '',
      ptype: null
    }
  },
  getters: {
    getpostListData(state) {
      return state.postListData;
    },

  },
  mutations: {
    initPostData(state) {
      state.postData = {
        pid: '',
        lcid: '1', /***나중에 수정***/
        mcid: '1', /***나중에 수정***/
        ptitle: '',
        pcontent: '',
        author: '',
        postTime: '',
        update_time: '',
        ptype: ''
      }
    },

    setPostListData(state, postList) {
      state.postListData = postList;
    },

    setPostDetailData(state, postData) {
      state.postData = postData;
    },
    
    SET_BID(state, bid) {
      state.bid = bid
    },

    SET_BLOGDATA(state, blogData) {
      state.blogData = blogData
    }
  },
  actions: {
    // 블로그 추가 (API 문서 - 26~29 D)
    createBlog(response) {
      BlogService.createBlog(response)
    },
    
    // 블로그 정보 조회 (API 문서 - 28D)
    getBlogInfo({ commit }, bid) {
      BlogService.getBlogInfo({ commit }, bid)
    },

    // 블로그 게시글 작성 (API 문서 - 44D)
    createPost(response) {
      BlogService.createPost(response)
    },

    // 블로그 게시글 전체 조회 (API 문서 - 62D)
    lookupPostList({commit}) {
      return BlogService.lookupPostList()
      .then(postListData => {
        commit('setPostListData', postListData)
      })
      .catch(error => console.log(error.data.message))
    },

    // 블로그 게시글 상세 조회 (API 문서 - 70D)
    lookupPostDetail({commit}, response) {
      return BlogService.lookupPostDetail(response)
      .then(postDetailData => {
        commit('setPostDetailData', postDetailData)
      })
      .catch(error => console.log(error.data.message))
    },

    // 블로그 게시글 수정 (API 문서 - 54D)
    updatePost({commit}, response) {
      return BlogService.updatePost(response)
      .then(result => {
        commit('setPostDatailData', result.data)
        alert(result.message)
      })
      .catch(error => console.log(error.response.data.message))
    },

    // 블로그 게시글 삭제 (API 문서 - 65D)
    deletePost(response) {
      BlogService.deletePost(response)
    }
  }
}