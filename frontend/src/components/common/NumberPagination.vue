<script setup>
defineProps({page:Number,pages:Array,totalPages:Number})
const emit=defineEmits(['change'])
</script>
<template><nav class="number-pagination" aria-label="목록 페이지 이동"><button :disabled="page===1" @click="emit('change',page-1)">이전</button><button v-for="p in pages" :key="p" :aria-current="page===p?'page':undefined" @click="emit('change',p)">{{p}}</button><button :disabled="page===totalPages" @click="emit('change',page+1)">다음</button></nav></template>
<style scoped>.number-pagination{display:flex;justify-content:center;gap:7px}.number-pagination button{height:34px;min-width:34px;border:1px solid #d8e6ef;background:white;border-radius:6px;color:#41647d;cursor:pointer}.number-pagination button[aria-current=page]{background:#0879ac;border-color:#0879ac;color:white}.number-pagination button:disabled{opacity:.45;cursor:default}.number-pagination button:focus-visible{outline:3px solid #64b3e0;outline-offset:2px}</style>
