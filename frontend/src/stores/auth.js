import { ref, computed, reactive } from 'vue';
import { defineStore } from 'pinia';
import axios from 'axios';

const initState = {
  token: '', // 접근 토큰(JWT)
  user: {
    username: '', // 사용자 ID
    email: '', // Email
    roles: [], // 권한 목록
  },
};

//중앙저장소에 넣어뒀다가 모든 컴포넌트에서 접근해서 꺼낼수 있도록 하자.
//pinia라이브러리, 중앙저장소.
//상태관리 라이브러리.
//데이터, 함수

export const useAuthStore = defineStore('auth', () => {
  const state = ref({ ...initState }); //전개연산자, 코드를 넣어줌.

  const isLogin = computed(() => !!state.value.user.username); // 로그인 여부
  const username = computed(() => state.value.user.username); // 로그인 사용자 ID
  const email = computed(() => state.value.user.email); // 로그인 사용자 email

  const login = async (member) => {
    // state.value.token = 'test token';
    // state.value.user = {
    //   username: member.username,
    //   email: member.username + '@test.com',
    // }; //token, user는 axios로 서버에서 받아올 값.
    //현재 테스트값으로 해보자!!

    //로그인 axios로 할 예정
    //login --> 성공하면 백엔드서버가 jwt토큰을 발급해줌.
    //브라우저의 localStorage에 저장해야함.

    const { data } = await axios.post('/api/users/login', member);
    state.value = { ...data };

    localStorage.setItem('auth', JSON.stringify(state.value));
    //json객체(js) --> string(localstorage), 직렬화
  };

  const logout = () => {
    localStorage.clear();
    state.value = { ...initState };
  };

  const getToken = () => state.value.token;

  const load = () => {
    const auth = localStorage.getItem('auth');
    if (auth != null) {
      state.value = JSON.parse(auth);
      //json객체(js) <-- string(localstorage), 역직렬화
      console.log(state.value);
    }
  };

  const changeProfile = (member) => {
    state.value.user.email = member.email;
    console.log(state.value);
    localStorage.setItem('auth', JSON.stringify(state.value));
  };

  load();

  return {
    state,
    isLogin,
    username,
    email,
    login,
    logout,
    getToken,
    changeProfile,
  };
});
