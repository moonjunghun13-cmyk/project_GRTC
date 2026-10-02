<script setup>
defineProps({ type: { type: String, default: 'button' }, disabled: Boolean })
</script>
<template><button class="base-button" :type="type" :disabled="disabled"><slot /></button></template>
