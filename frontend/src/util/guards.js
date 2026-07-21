//로그인하면 로그인되었는지에 대한 정보를
//중앙저장소에 isLogin이라는 변수에 그 값을 넣어둠.
//isLogin = true, false가 들어가있음.
//isLogin값을 가지고 와서 true가 아니면(false)
//로그인 컴포넌트를 먼저 라우팅해라라고 설정
//로그인 컴포넌트를 라우팅할때는 로그인하고 나서
//원래 라우팅하려고 했던 주소를 함께 보내주어야함.

import { useAuthStore } from '@/stores/auth';

//로그인이 필요한 컴포넌트 라우팅하기 전에(before)
//로그인되어있는지 확인하는 함수를 정의
export const isAuthenticated = (to, from) => {
  const auth = useAuthStore();

  if (!auth.isLogin) {
    //로그인이 X
    console.log('로그인 필요함.....');
    return { name: 'login', query: { next: to.name } };
  }
  console.log('로그인 인증됨....');
};
