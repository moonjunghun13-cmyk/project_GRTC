<script setup>
import {ref,computed} from 'vue'
import {faqs,faqCategories} from '../../../data/citizenHelp'
import character from '../../../assets/ja.png'
import './help.css'
const keyword=ref(''),category=ref('전체')
const filtered=computed(()=>faqs.filter(f=>(category.value==='전체'||f.category===category.value)&&(!keyword.value.trim()||(f.question+' '+f.answer).toLocaleLowerCase().includes(keyword.value.trim().toLocaleLowerCase()))))
</script>
<template><section class="citizen-help"><header class="help-hero" aria-label="자주 묻는 질문"><img :src="character" alt="자주 묻는 질문" /></header><div class="help-search"><label for="faq-search">질문 검색</label><input id="faq-search" v-model="keyword" type="search" placeholder="로그인, 첨부파일, 답변 등을 검색하세요"/></div><div class="help-tabs" role="group" aria-label="FAQ 카테고리"><button v-for="item in faqCategories" :key="item" type="button" :aria-pressed="category===item" @click="category=item">{{item}}</button></div><p class="help-results" role="status">{{filtered.length}}개의 질문</p><div class="faq-list"><details v-for="item in filtered" :key="item.id" class="help-card"><summary><span class="faq-category">{{item.category}}</span><span>{{item.question}}</span><span class="faq-chevron" aria-hidden="true">⌄</span></summary><p>{{item.answer}}</p></details><p v-if="!filtered.length" class="help-card help-empty">검색 결과가 없습니다. 다른 검색어나 카테고리를 선택해 주세요.</p></div><footer class="help-footer">민원 작성 방법이 궁금하신가요? <RouterLink :to="{name:'user-complaint-guide'}">민원 작성 가이드 보기 →</RouterLink></footer></section></template>
