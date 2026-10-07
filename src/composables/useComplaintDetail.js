import {ref,watch} from 'vue'
import {useRoute} from 'vue-router'
import {getComplaint} from '../services/complaintApi'
import {errorMessage} from '../api/clients'
export function useComplaintDetail(admin=false){const route=useRoute(),complaint=ref(null),loading=ref(false),error=ref(''),denied=ref(false),httpStatus=ref(null);let sequence=0;async function reload(){const request=++sequence;complaint.value=null;loading.value=true;denied.value=false;httpStatus.value=null;error.value='';try{const result=await getComplaint(route.params.id,admin);if(request===sequence)complaint.value=result}catch(e){if(request===sequence){httpStatus.value=e.response?.status||null;denied.value=httpStatus.value===403;error.value=errorMessage(e)}}finally{if(request===sequence)loading.value=false}}watch(()=>route.params.id,reload,{immediate:true});return {complaint,loading,error,denied,httpStatus,reload}}

