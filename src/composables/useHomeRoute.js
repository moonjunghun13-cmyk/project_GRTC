import {computed} from 'vue'
import {currentUser} from '../stores/currentUser'
import {isAdminAccount} from '../utils/memberAccount'
export const homeRoute=computed(()=>({name:!currentUser.value?'intro':isAdminAccount(currentUser.value)?'admin-dashboard':'user-complaints'}))
