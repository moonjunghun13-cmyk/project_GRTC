<script setup>
defineProps({ attachments: { type: Array, default: () => [] } })
</script>
<template><section><h2>첨부파일</h2><ul v-if="attachments.length"><li v-for="(file, index) in attachments" :key="index">{{ file.name }}</li></ul><p v-else>첨부파일이 없습니다.</p></section></template>
