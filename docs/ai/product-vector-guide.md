# Product 벡터

이 문서는 `product`에 벡터를 왜 넣는지 간단히 정리한 문서입니다.

## 한 줄로 보면

상품에는 원래 정보가 있고,  
벡터는 **비슷한 상품을 찾기 위한 검색용 값**입니다.

## product에 들어가는 값

기존 값:

- 상품명
- 상품 설명
- 가격
- 재고

추가된 값:

- `embedding`

즉 `embedding`은 상품을 검색하기 쉽게 바꿔 놓은 값입니다.

## 어떤 값을 벡터로 만들까

처음에는 아래 두 개만 쓰면 됩니다.

- `name`
- `description`

예:

```text
상품명: 맥북 프로 14
설명: M3 칩셋, 16GB RAM, 512GB SSD
```

이 텍스트를 임베딩 API에 보내면 숫자 배열이 나오고,  
그 값을 `embedding`에 저장합니다.

## 왜 이렇게 하나

일반 검색은 글자가 똑같아야 잘 찾습니다.

벡터 검색은 뜻이 비슷한 것도 찾기 쉽습니다.

예:

- 검색어: `영상 편집용 노트북`
- 결과: `맥북 프로 14`

즉 단어가 완전히 같지 않아도 가까운 상품을 찾을 수 있습니다.

## 저장 순서

1. 상품 저장
2. `name + description`으로 문장 만들기
3. 임베딩 API 호출
4. 나온 벡터를 `embedding`에 저장

## 검색 순서

1. 사용자가 검색어 입력
2. 검색어도 벡터로 변환
3. `product.embedding`과 비교
4. 가장 비슷한 상품 반환

## 지금 코드에서 보는 위치

- 엔티티: [Product.java](../../src/main/java/com/grepp/backend5/product/domain/model/Product.java)
- DB 컬럼 SQL: [product-pgvector.sql](../../src/main/resources/db/product-pgvector.sql)

## 지금 단계에서 기억할 것

- 원본 데이터: `name`, `description`
- 검색용 데이터: `embedding`
- 처음에는 `name + description`만 벡터로 만들면 충분합니다.
