<script setup>
import banner from '../../assets/min.png'
</script>
<template>
  <section class="complaint-banner" aria-labelledby="complaint-banner-title">
    <img :src="banner" alt="" width="1712" height="287" />
    <div class="complaint-banner-copy">
      <h1 id="complaint-banner-title">민원관리</h1>
      <p>시민의 소중한 의견에 귀기울여<br />더 나은 교통 서비스를 만들어갑니다.</p>
    </div>
  </section>
</template>
<style scoped>
.complaint-banner { position: relative; margin: 0; padding: 0; background: #f3faff; border: 0; border-radius: 0; box-shadow: none; container-type: inline-size; }
.complaint-banner img { display: block; width: 100%; height: auto; }
.complaint-banner-copy { position: absolute; inset: 0 auto 0 60px; max-width: 58%; display: flex; flex-direction: column; justify-content: center; text-align: left; }
.complaint-banner h1 { margin: 0 0 14px; color: #00699f; font-size: 48px; font-weight: 900; line-height: 1.2; }
.complaint-banner p { margin: 0; color: #666666; font-size: 24px; font-weight: 600; line-height: 1.55; }
@container (max-width: 1200px) {
  .complaint-banner-copy { left: 40px; }
  .complaint-banner h1 { font-size: 40px; }
  .complaint-banner p { font-size: 20px; }
}
@container (max-width: 760px) {
  .complaint-banner-copy { position: static; max-width: none; padding: 16px 20px; }
  .complaint-banner h1 { font-size: 28px; }
  .complaint-banner p { font-size: 15px; }
}
</style>
