<script setup>
import ComplaintList from "../../../components/complaints/ComplaintList.vue"
import banner from "../../../assets/min.png"
</script>
<template><section class="citizen-list-page"><header class="citizen-list-heading" :style="{ '--list-banner': `url(${banner})` }"><div><p>광주교통공사 안내</p><h1>민원목록</h1><span>시민의 소중한 의견에 귀 기울여<br />더 나은 교통 서비스를 만들어갑니다.</span></div></header><ComplaintList class="citizen-complaint-list" :page-size="5" /></section></template>

<style scoped>.citizen-complaint-list{flex:1;min-height:0;width:100%}.citizen-complaint-list :deep(.list-table-wrap){flex:1;min-height:0;overflow:auto}.citizen-complaint-list :deep(table){height:auto}.citizen-complaint-list :deep(.list-pagination){flex-shrink:0;margin-top:auto;padding-top:12px}</style>

<style scoped>
.citizen-complaint-list :deep(.list-table-wrap){position:relative;isolation:isolate;background:#FFFFFF}

.citizen-complaint-list :deep(table),.citizen-complaint-list :deep(tbody),.citizen-complaint-list :deep(tbody tr),.citizen-complaint-list :deep(tbody td){background:transparent}
.citizen-complaint-list :deep(tbody tr:hover){background:rgba(229,246,255,.65)}
</style>
<style scoped>
.citizen-list-page{max-width:1400px;width:100%;margin:0 auto;display:flex;flex-direction:column;flex:1;min-height:0;gap:16px;color:#163D58}
.citizen-list-heading{background:var(--list-banner) center / cover no-repeat;min-height:200px;padding:24px 36px;border-radius:14px;box-sizing:border-box;flex-shrink:0;display:flex;align-items:center}
.citizen-list-heading>div{max-width:60%}.citizen-list-heading p{margin:0 0 5px;color:#476A82;font-size:13px}.citizen-list-heading h1{font-size:36px;margin:0 0 12px;color:#006BA6;font-weight:800}.citizen-list-heading span{color:#385C74;font-size:15px;line-height:1.65}
.citizen-complaint-list{gap:16px}
.citizen-complaint-list :deep(.list-search){padding:18px;border:1px solid #D5E4EF;border-radius:14px;background:white;align-items:center}
.citizen-complaint-list :deep(.list-search :is(input,select,button)),.citizen-complaint-list :deep(.register-button){border-color:#ADC8DC;border-radius:7px;height:44px}
.citizen-complaint-list :deep(.list-search .search-button),.citizen-complaint-list :deep(.register-button){background:#0078AE;border-color:#0078AE;color:white}
.citizen-complaint-list :deep(.list-table-wrap){border:1px solid #D5E4EF;border-radius:14px}
.citizen-complaint-list :deep(th){height:54px;background:#006BA6;color:white;font-size:14px;position:sticky;top:0;z-index:1}
.citizen-complaint-list :deep(td){height:54px;border-bottom-color:#E0EBF2;font-size:15px}
.citizen-complaint-list :deep(.list-pagination){padding-top:0;margin-top:auto}
@media(max-width:750px){.citizen-list-heading{min-height:165px;padding:22px 20px;background-position:60% center}.citizen-list-heading>div{max-width:68%}.citizen-list-heading h1{font-size:28px}.citizen-list-heading span{font-size:13px}.citizen-complaint-list :deep(.list-search){padding:14px}}
</style>
<style scoped>
.citizen-list-heading{min-height:150px;height:150px;padding:16px 28px}
.citizen-list-heading h1{font-size:32px;margin-bottom:8px}
.citizen-list-heading span{font-size:14px;line-height:1.5}
.citizen-complaint-list :deep(.list-search){padding:12px 16px}
.citizen-complaint-list :deep(.list-search :is(input,select,button)),.citizen-complaint-list :deep(.register-button){height:38px}
.citizen-complaint-list :deep(th){height:44px}
.citizen-complaint-list :deep(td){height:44px;padding:4px 8px;box-sizing:border-box}
@media(max-width:750px){.citizen-list-heading{height:auto;min-height:130px;padding:16px 20px}.citizen-list-heading h1{font-size:28px}.citizen-list-heading span{font-size:13px}}
</style>
