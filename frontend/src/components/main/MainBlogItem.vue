<template>
  <v-card class="mx-auto main-blog-item-container" outlined>
    <v-list-item>
      <v-list-item-content>
            <div v-if="blog.member.length == 1" class="main-blog-item-member">
              <v-tooltip top>
                <template v-slot:activator="{ on, attrs }">
                  <font-awesome-icon class="main-blog-item-icon" v-bind="attrs" v-on="on" :icon="['fas','user']" />
                  <span class="main-blog-item-num">{{blog.member.length}}</span>
                </template>
                <span>멤버</span>
              </v-tooltip>
            </div>
            <div v-else class="main-blog-item-member">
              <v-tooltip top>
                <template v-slot:activator="{ on, attrs }">
                  <font-awesome-icon class="main-blog-item-icon" v-bind="attrs" v-on="on"  :icon="['fas','user-friends']" />
                  <span class="main-blog-item-num">{{blog.member.length}}</span>
                </template>
                <span>멤버</span>
              </v-tooltip>
            </div>

        <v-list-item-title class="main-blog-item-title" @click="getBlogInfo(blog.bid)">{{blog.btitle}}</v-list-item-title>
        <v-list-item-subtitle>{{blog.bsubtitle}}</v-list-item-subtitle>

        <div class="main-blog-item-view-follower">
          <div class="main-blog-item-view-detail">
          <v-tooltip top>
            <template v-slot:activator="{ on, attrs }">
              <font-awesome-icon class="main-blog-item-icon" v-bind="attrs" v-on="on" :icon="['fas','eye']" />
              <span class="main-blog-item-num" id="main-blog-item-views-num">{{blog.views}}</span>
            </template>
            <span>방문 수</span>
          </v-tooltip>
          </div>

          <div class="main-blog-item-follower-detail">
          <v-tooltip top>
            <template v-slot:activator="{ on, attrs }">
                <font-awesome-icon class="main-blog-item-icon" v-bind="attrs" v-on="on" :icon="['fas','user-plus']" />
                <span class="main-blog-item-num">{{blog.followers}}</span>   
            </template>
            <span>팔로워</span>
          </v-tooltip>
          </div>
        </div>

        <div>
          <v-btn id="main-blog-item-hashtag-btn" rounded color="#6ac6c8" dark small v-for="hashtags in blog.hashtags" :key="hashtags.id" @click="searchByHashTag(hashtags.tname)"> #{{hashtags.tname}}</v-btn>
        </div>
      </v-list-item-content>
    </v-list-item>
  </v-card>
</template>

<script>
import { mapActions } from 'vuex'
export default {
  name: 'MainBlogItem',
  props: {
    blog: {
      type: Object
    }
  },
  data() {
    return {
      searchData: {
        searchBy: '2',
        searchInput: null,
      },
    }
  },
  methods: {
    ...mapActions('blog', ['getBlogInfo']),
    ...mapActions('main', ['search']),

    searchByHashTag(hashtag) {
      this.searchData.searchInput = hashtag
      this.search(this.searchData)
    }
  }
}
</script>
