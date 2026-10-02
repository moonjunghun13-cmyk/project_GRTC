<script setup>
import { computed } from 'vue'
const props = defineProps({ page: { type: Number, default: 1 }, totalPages: { type: Number, default: 1 } })
const emit = defineEmits(['update:page'])
const lastPage = computed(() => Math.max(1, props.totalPages))
</script>
<template><nav class="pagination" aria-label="페이지 이동"><button :disabled="page <= 1" @click="emit('update:page', page - 1)">이전</button><span aria-live="polite">{{ page }} / {{ lastPage }}</span><button :disabled="page >= lastPage" @click="emit('update:page', page + 1)">다음</button></nav></template>
