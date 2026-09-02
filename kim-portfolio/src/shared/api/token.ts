// 토큰 보관소
// 

const KEY = 'token'

export const getToken = () => localStorage.getItem(KEY)
export const setToken = (t: string) => localStorage.setItem(KEY, t)
export const clearToken = () => localStorage.removeItem(KEY)