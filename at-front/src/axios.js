
import axios from 'axios'

const instance = axios.create({
    baseURL: 'http://localhost:8080/WAR2020/'
});

export default instance;