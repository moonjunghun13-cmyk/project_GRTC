<script setup>
import { statusText } from '../../services/complaintOptions'
defineProps({status:{type:String,default:''},label:String})
</script>
<template><span class="status-badge" :data-status="status"><i aria-hidden="true"></i>{{ statusText({status,statusLabel:label}) }}</span></template>
<style scoped>
.status-badge{display:inline-flex;align-items:center;gap:6px;border-radius:20px;padding:4px 10px;font-size:14px;white-space:nowrap;background:#f0f4f6;color:#52616d}.status-badge i{width:6px;height:6px;border-radius:50%;background:currentColor}.status-badge[data-status=WAITING]{background:#fbe8e7;color:#a83742}.status-badge[data-status=RECEIVED]{background:#fff0d9;color:#945c19}.status-badge[data-status=TRANSFERRED]{background:#e5eef9;color:#286798}.status-badge[data-status=ANSWERED]{background:#e2f2e8;color:#247448}
</style>
