<script setup>
import character from '../../assets/x.png'
defineProps({status:{type:Number,default:404},reason:{type:String,default:''},admin:Boolean})
</script>
<template><section class="complaint-error-card" role="alert"><img :src="character" alt="민원 안내 캐릭터"/><h2>{{reason==='withdrawn'?'철회된 민원입니다':reason==='deleted'?'삭제된 민원입니다':status===403?'민원을 열람할 수 없습니다':'민원을 찾을 수 없습니다'}}</h2><p>{{reason==='withdrawn'?'철회된 민원은 상세 내용을 확인할 수 없습니다.':reason==='deleted'?'삭제된 민원은 상세 내용을 확인할 수 없습니다.':status===403?'본인이 작성한 민원만 상세 내용을 확인할 수 있습니다.':'삭제되었거나 존재하지 않는 민원입니다.'}}</p><RouterLink :to="{name:admin?'admin-complaints':'user-complaints'}">민원 목록으로</RouterLink></section></template>
<style scoped>
.complaint-error-card{box-sizing:border-box;min-height:380px;width:100%;padding:28px 24px;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;background:#fff;border:1px solid #dce7ee;border-radius:16px;color:#173e5c}.complaint-error-card img{display:block;width:760px;max-width:100%;height:auto;max-height:none;object-fit:contain;flex-shrink:0}.complaint-error-card h2{margin:18px 0 10px;font-size:30px;font-weight:800;line-height:1.4}.complaint-error-card p{margin:0;color:#788996;font-size:17px;line-height:1.6}.complaint-error-card a{display:inline-flex;align-items:center;justify-content:center;min-height:46px;margin-top:24px;padding:0 26px;background:#064b76;color:white;border-radius:9px;font-size:16px;font-weight:700;text-decoration:none}.complaint-error-card a:hover{background:#043e61}.complaint-error-card a:focus-visible{outline:3px solid #64b3e0;outline-offset:3px}@media(max-width:600px){.complaint-error-card{padding:24px 18px}.complaint-error-card h2{font-size:24px}.complaint-error-card p{font-size:16px}}
</style>


