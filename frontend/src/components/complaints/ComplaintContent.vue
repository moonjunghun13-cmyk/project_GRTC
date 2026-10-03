<script setup>
import { complaintTypes, complaintLabel } from '../../mocks/complaintForm'
import StatusBadge from '../common/StatusBadge.vue'
import AttachmentList from './AttachmentList.vue'
defineProps({ complaint: { type: Object, default: () => ({}) } })
</script>
<template><article><h2>{{ complaint.title || '민원 상세' }}</h2><span v-if="complaint.type">{{ complaintLabel(complaintTypes,complaint.type) }} · </span><StatusBadge v-if="complaint.status" :status="complaint.status" :label="complaint.statusLabel" /><p class="complaint-text">{{ complaint.content || '민원 데이터를 불러오는 기능은 준비 중입니다.' }}</p><AttachmentList :attachments="complaint.attachments || []" /></article></template>
