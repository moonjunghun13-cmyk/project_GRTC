<script setup>
import { statusText } from '../../services/complaintOptions'
defineProps({status:{type:String,default:''},label:String})
</script>
<template><span class="status-badge" :data-status="status"><i aria-hidden="true"></i>{{ statusText({status,statusLabel:label}) }}</span></template>
<style scoped>
.status-badge{display:inline-flex;align-items:center;gap:6px;border-radius:20px;padding:4px 10px;font-size:14px;white-space:nowrap;background:#f0f4f6;color:#52616d}.status-badge i{width:6px;height:6px;border-radius:50%;background:currentColor}.status-badge[data-status=WAITING]{background:#fff0ef;color:#ba4646}.status-badge[data-status=RECEIVED]{background:#fff4e7;color:#ab6720}.status-badge[data-status=TRANSFERRED]{background:#edf4ff;color:#3875b4}.status-badge[data-status=ANSWERED]{background:#eaf8ef;color:#348452}
</style>
