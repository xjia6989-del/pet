import axios from 'axios'

axios.defaults.baseURL = 'http://localhost:9999'
const request = axios.create({
    // withCredentials: true   // 注释掉
})

export default request