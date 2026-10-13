<script setup>
import {ref,onMounted} from 'vue'
import ComplaintList from '../../../components/complaints/ComplaintList.vue'
import ListSummaryCards from '../../../components/admin/ListSummaryCards.vue'
import {general} from '../../../api/clients'
import {getComplaintOptions} from '../../../services/complaintOptions'
const items=ref([{label:'전체 민원',value:'',count:null},...getComplaintOptions().statuses.map(status=>({label:status.label,value:status.value,count:null}))])
onMounted(async()=>{try{const result=await general('get','/api/v1/admin/dashboard',undefined,{complainPeriod:'ALL',answerPeriod:'ALL'});items.value=[{label:'전체 민원',value:'',count:result.answers.total},...result.answers.byStatus.map(item=>({label:item.label,value:item.code,count:item.count}))]}catch{/* Keep unavailable totals distinct from zero; list errors remain in the existing list. */}})
</script>
<template><ComplaintList admin><template #summary="{draft,search}"><ListSummaryCards :items="items" :active-key="draft.status" kind="complaint" @select="draft.status=$event.value;search()" /></template></ComplaintList></template>
