import api from 'axios';
import instance from '@/api'; //api/index.js

//api.post()
//api.get()
//--> 비동기간에도 순서가 필요한 경우 await api.get()를 써주어야하고,
//axios처리가 들어있는 함수앞에는 async를 붙여주어야한다.
const BASE_URL = '/api/users';
const headers = { 'Content-Type': 'multipart/form-data' };

//외부 컴포넌트에서 사용해야하므로
//밖으로 export한 것만 사용 가능.
export default {
  //중복체크하는 함수
  async checkUsername(username) {
    const { data } = await api.get(`${BASE_URL}/checkusername/${username}`);
    console.log(data);
    return data;
  },

  //회원가입하는 함수
  async create(member) {
    const formData = new FormData();
    formData.append('username', member.username);
    formData.append('password', member.password);
    formData.append('email', member.email);

    if (member.avatar) {
      formData.append('avatar', member.avatar);
    }
    const { data } = await api.post(`${BASE_URL}`, formData, headers);
    console.log(data);
    return data;
  },

  async update(member) {
    const formData = new FormData();
    formData.append('username', member.username);
    formData.append('password', member.password);
    formData.append('email', member.email);
    if (member.avatar) {
      formData.append('avatar', member.avatar);
    }
    const { data } = await instance.put(
      `${BASE_URL}/${member.username}`,
      formData,
      headers,
    );
    console.log('AUTH PUT: ', data);
    return data;
  },
  async changePassword(formData) {
    const { data } = await api.put(
      `${BASE_URL}/${formData.username}/changepassword`,
      formData,
    );
    console.log('AUTH PUT: ', data);

    return data;
  },
};
