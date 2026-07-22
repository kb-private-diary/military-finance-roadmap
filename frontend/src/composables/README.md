# composables

여러 화면에서 **반복되는 로직**을 함수로 분리해 두는 폴더입니다. (개발 컨벤션 §5)

## 규칙

- 파일·함수 이름은 **`use` 접두사** 필수 — `useGoal.js` → `useGoal()`
- 화면(`.vue`)에는 화면 표시 로직만, **데이터 조회·상태 관리는 여기로**
- 특정 도메인 전용이면 이름에 도메인을 붙인다 — `useRentGoal`, `useJobGoal`

## 예시

```js
// composables/useGoal.js
import { ref } from 'vue';
import api from '@/api';

export function useGoal(domain) {
  const goal = ref(null);
  const loading = ref(false);
  const error = ref(null);

  const fetchGoal = async (goalId) => {
    loading.value = true;
    try {
      const { data } = await api.get(`/${domain}/goals/${goalId}`);
      goal.value = data.data; // ApiResponse 로 감싸져 있음
    } catch (e) {
      error.value = e;
    } finally {
      loading.value = false;
    }
  };

  return { goal, loading, error, fetchGoal };
}
```

```vue
<!-- pages/travel/TravelGoalDetailPage.vue -->
<script setup>
import { onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useGoal } from '@/composables/useGoal';

const route = useRoute();
const { goal, loading, fetchGoal } = useGoal('travel');

onMounted(() => fetchGoal(route.params.goalId));
</script>
```

> 로드맵 4파트(travel·rent·car·job)는 흐름이 거의 같으므로, 공통으로 쓸 수 있는 것은
> 여기에 만들어 **네 명이 같은 코드를 네 번 작성하지 않도록** 합니다.
