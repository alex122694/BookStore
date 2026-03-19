<template>
  <div class="about-wrapper">
    <v-parallax
      :src="aboutImage" 
      height="500"
    >
      <div class="d-flex flex-column fill-height justify-center align-center text-white hero-overlay">
        <h1 class="text-h2 font-weight-black mb-4">關於森林書屋</h1>
        <h4 class="text-h5 font-weight-light">一群熱愛閱讀的人，在這裡為您築起一座心靈森林。</h4>
      </div>
    </v-parallax>

    <v-container class="py-15">
      <v-row justify="center">
        <v-col cols="12" md="10" lg="8">
          <div class="section-content mb-12">
            <h2 class="text-h4 font-weight-bold text-primary mb-6">我們的故事</h2>
            <p class="text-body-1 text-grey-darken-2 leading-loose">
              森林書屋成立於 2024 年，最初只是幾個好友在客廳裡的讀書會。我們深信，在資訊爆炸的時代，每個人都需要一個能安放靈魂的地方。於是，我們決定將這份對文字的熱愛轉化為實體的空間與線上的平台，讓好書能像種子一樣，在每位讀者的心中發芽。
            </p>
          </div>

          <v-row class="mb-12">
            <v-col cols="12" md="6">
              <v-card class="pa-6 rounded-xl border-sm bg-light-green shadow-none" flat>
                <v-icon color="primary" size="36" class="mb-4">mdi-leaf</v-icon>
                <h3 class="text-h5 font-weight-bold mb-3">嚴選好書</h3>
                <p class="text-body-2">我們不追求海量的藏書，而是透過「策展」的角度，為您挑選真正值得細讀的作品。</p>
              </v-card>
            </v-col>
            <v-col cols="12" md="6">
              <v-card class="pa-6 rounded-xl border-sm bg-light-green shadow-none" flat>
                <v-icon color="primary" size="36" class="mb-4">mdi-heart-outline</v-icon>
                <h3 class="text-h5 font-weight-bold mb-3">閱讀溫度</h3>
                <p class="text-body-2">除了購書，我們更重視讀者間的交流。透過書評與討論，讓閱讀不再是一件孤單的事。</p>
              </v-card>
            </v-col>
          </v-row>

          <div class="section-content">
            <h2 class="text-h4 font-weight-bold text-primary mb-6">我們的承諾</h2>
            <p class="text-body-1 text-grey-darken-2 leading-loose mb-4">
              在森林書屋，我們承諾提供最舒適的選書環境與最誠摯的服務。無論您是在尋找失落已久的童年回憶，還是探索前沿的科技知見，這裡的護林員（管理員）都會陪伴您一同尋找。
            </p>
            <p class="text-body-1 text-grey-darken-2 leading-loose">
              未來，我們計畫舉辦更多線下讀書會與作家見面會，誠摯邀請您加入這個溫暖的大家庭。
            </p>
          </div>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup>
import aboutImage from '@/common/components/imgs/spiderspider.png'

</script>

<style scoped>
.about-wrapper {
  background-color: #fdfbf7;
}

.hero-overlay {
  background: linear-gradient(to bottom, rgba(46, 92, 67, 0.4), rgba(46, 92, 67, 0.7));
}

.leading-loose {
  line-height: 1.8;
  letter-spacing: 0.5px;
}

.bg-light-green {
  background-color: rgba(46, 92, 67, 0.05) !important;
  border-color: rgba(46, 92, 67, 0.1) !important;
}

.text-primary {
  color: #2e5c43 !important;
}

h2 {
  position: relative;
  padding-bottom: 12px;
}

h2::after {
  content: "";
  position: absolute;
  left: 0;
  bottom: 0;
  width: 50px;
  height: 4px;
  background-color: #2e5c43;
  border-radius: 2px;
}
</style>