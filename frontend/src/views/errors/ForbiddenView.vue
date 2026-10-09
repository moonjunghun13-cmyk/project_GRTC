<script setup>
import { useRoute } from 'vue-router'
import AccessNotice from '../../components/common/AccessNotice.vue'
const route = useRoute()
</script>
<template><AccessNotice :reason="route.query.reason === 'ownership' ? 'ownership' : 'admin'" /></template>
