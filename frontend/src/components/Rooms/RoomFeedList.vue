<template>
  <div class="card-body feed-group">
    <div class="card-group row px-3">
      <RoomFeedDetail v-for="feed in this.feeds" :key="feed.id" :feed="feed" :roomId="roomId"/>
    </div>
  </div>
</template>

<script>
import RoomFeedDetail from '../Rooms/RoomFeedDetail.vue'

export default {
  name: 'RoomFeedList',
  props: {
    feeds: {
      type: Array
    },
    roomId: {
      type: Number
    }
  },
  components: {
    RoomFeedDetail
  },
  data() {
    return {
      UID: null,
      roomfeeds: []
    }
  },
  created() {
    if(localStorage.getItem('loginUID')){
      this.isLogin = true
      this.UID = localStorage.getItem('loginUID')
    } else if(sessionStorage.getItem('loginUID')) {
      this.isLogin = true
      this.UID = sessionStorage.getItem('loginUID')
    } else {
      this.isLogin = false
    }

    for (var i=0; i < this.feeds.length ; i++){
      if (this.feeds[i].roomId == this.roomId){
        this.roomfeeds.push(this.feeds[i])
      }
    }
  },
}
</script>

<style scoped>

</style>