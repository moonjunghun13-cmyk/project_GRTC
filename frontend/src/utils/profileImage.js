import adminImage from '../assets/admin.png'
import userImage from '../assets/user.png'
export function profileImage(role) { return role === 'ADMIN' ? adminImage : userImage }
