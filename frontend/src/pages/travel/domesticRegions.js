export const DOMESTIC_COUNTRY = '대한민국';

export const DOMESTIC_REGIONS = {
  '수도권': [
    '서울', '인천', '수원', '성남', '고양', '용인', '부천', '안산', '안양', '평택', '김포', '의정부', '이천',],
    '강원': ['강릉', '춘천', '원주'],
    '충청': ['대전', '청주', '충주', '천안', '공주', '보령', '제천', '서산'],
    '전라': ['전주', '익산', '목포', '여수'],
    '경상': ['부산', '대구', '울산', '창원', '김해', '김천', '안동', '경주', '거제', '포항', '진주', '사천', '상주', '양산',],
};

export const findDomesticRegion = (city) =>
  Object.entries(DOMESTIC_REGIONS).find(([, regionCities]) =>
    regionCities.includes(city),
  )?.[0] || '';
