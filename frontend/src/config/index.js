export default {
  title: '텅장일병구하기', // 메인 타이틀
  subtitle: 'KB 군장병 리텐션 서비스', // 서브 타이틀
  menus: [
    // 하단 탭/메인 메뉴 (기획안 Tab Navi 기준)
    {
      title: '대시보드',
      url: '/dashboard',
      icon: 'fa-solid fa-gauge-high',
    },
    {
      title: '시뮬레이터',
      url: '/simulator',
      icon: 'fa-solid fa-calculator',
    },
    {
      title: '로드맵',
      url: '/roadmap/travel', // 로드맵 진입(여행) — 하위: 여행/자취/자동차/진로
      icon: 'fa-solid fa-map-location-dot',
    },
    {
      title: '소셜',
      url: '/social',
      icon: 'fa-solid fa-ranking-star',
    },
    {
      title: '챗봇',
      url: '/chat',
      icon: 'fa-solid fa-robot',
    },
  ],
  accoutMenus: {
    // 인증 관련 메뉴
    login: {
      url: '/auth/login',
      title: '로그인',
      icon: 'fa-solid fa-right-to-bracket',
    },
    join: {
      url: '/auth/join',
      title: '회원가입',
      icon: 'fa-solid fa-user-plus',
    },
  },
};
