import { fileURLToPath, URL } from 'node:url';

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      // 챗봇(B안): /api/chat/** → FastAPI(별도 서버). '/api' 규칙보다 먼저 선언(더 구체적 경로 우선).
      '/api/chat': {
        target: 'http://localhost:8000', // 챗봇 FastAPI 서버 (에스더) — 포트는 팀 합의값으로
        changeOrigin: true,
      },
      // 그 외 모든 /api/** → Spring 백엔드
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    // 배포 빌드 결과물 → 백엔드 war 에 함께 패키징
    outDir: '../backend/src/main/webapp/resources',
    emptyOutDir: true,
  },
});
