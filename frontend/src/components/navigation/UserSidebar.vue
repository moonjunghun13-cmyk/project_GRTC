<script setup>
import AdminSidebar from './AdminSidebar.vue'
defineProps({ user: { type: Object, default: null } })
</script>
<template><AdminSidebar role="user" :user="user" /></template>
