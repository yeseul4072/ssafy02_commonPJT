<template>
<div>
  <v-list>
    <v-list-item v-for="member in this.members" :key="member.id">
        <v-list-item-avatar @click="goprofile(member)">
          <v-img v-if="member.userThumbnail != null" :src="'data:' + member.imageType + ';base64,' + member.userThumbnail"></v-img>
          <v-img v-if="member.userThumbnail == null" src="../../../public/profile/profile.png"></v-img>
        </v-list-item-avatar>

        <v-list-item-content @click="goprofile(member)">
          <v-list-item-title v-text="member.userName"  @click="goprofile(member)">{{ member.userName }}</v-list-item-title>
        </v-list-item-content>
        
        <v-list-item-icon>
          <v-btn v-if="member.id != UID && captainId == UID" rounded style="background-color: #fd462e; font-family: 'Black Han Sans', sans-serif;" 
          class="text-white" @click="goout(member)">내보내기</v-btn>
        </v-list-item-icon>
          <v-dialog v-model="dialog" max-width="250px">
            <v-card class="pt-6 text-center">
              <v-card-text>정말 <span style="color: #fd462e;">{{ outmembername }}</span>님을 내보낼까요?</v-card-text>
              <v-card-actions>
              <div class="d-flex justify-content-center">
                <v-btn color="#fd462e" class="mr-3 ml-4 rounded-xl" style="border:1px solid #fd462e" outlined text @click="delmodal">취소하기</v-btn>
                <v-btn style="background-color: #fd462e;" class="rounded-xl text-white" text @click="realout">내보내기</v-btn>
              </div>
              </v-card-actions>
            </v-card>
          </v-dialog>
    </v-list-item>
  </v-list>
</div>
</template>

<script>
import axios from 'axios'

export default {
  name: "Memout",
  props: {
      roomId: {
        type: Number
      },
      captainId: {
        type: Number
      },
      in: {
        type: Boolean
      }
  },
  data() {
    return {
      UID: '',
      members: [],
      dialog: false,
      outmemberId: null,
      outmembername: null,
    }
  },
  watch: {
    in() {
    
      axios.get(`http://${this.$store.state.address}:8080/study/getStudyroomMembers`, {
        params: {
          roomId: this.roomId
        }
      })
      .then(response => {
        console.log(response)
        this.members = response.data.object
        // console.log(this.members)
      })
    }
  },
  created() {
    if(localStorage.getItem('loginUID')){
      this.UID = localStorage.getItem('loginUID')
    } else if(sessionStorage.getItem('loginUID')) {
      this.UID = sessionStorage.getItem('loginUID')
    }

    axios.get(`http://${this.$store.state.address}:8080/study/getStudyroomMembers`, {
      params: {
        roomId: this.roomId
      }
    })
    .then(response => {
      console.log(response)
      this.members = response.data.object
      // console.log(this.members)
    })
  },
  methods: {
    goprofile(member) {
      this.$router.push(`/mystudy/${member.id}`)
    },
    realout() {
      var content = {
        roomId: this.roomId,
        UID: this.outmemberId
      }
      axios.post(`http://${this.$store.state.address}:8080/study/removeMember`, content)
      .then(response => {
        console.log(response)
        axios.get(`http://${this.$store.state.address}:8080/study/getStudyroomMembers`, {
          params: {
            roomId: this.roomId
          }
        })
        .then(response => {
          console.log(response)
          this.members = response.data.object
          // console.log(this.members)
        })
      })
      this.dialog = false
      this.outmemberId = null
      this.outmembername = null
    },
    goout(member) {
      this.dialog = true,
      this.outmemberId = member.id,
      this.outmembername = member.userName
    },
    delmodal() {
      this.dialog = false,
      this.outmemberId = null
      this.outmembername = null
    }
  },
}
</script>

<style>

</style>