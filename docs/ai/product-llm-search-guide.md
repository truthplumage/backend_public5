# Product LLM 검색

이 문서는 상품 LLM 검색 API를 쉽게 정리한 문서입니다.

## 한 줄로 보면

질문을 넣으면  
먼저 비슷한 상품을 찾고  
그 상품들을 보고 LLM이 답변을 만듭니다.

## 순서

1. 사용자가 질문 입력
2. 질문을 벡터로 변환
3. 비슷한 상품 검색
4. 검색된 상품 목록을 LLM에 전달
5. LLM이 답변 생성

즉 바로 답변만 만드는 게 아니라,  
먼저 상품을 찾고 그 결과를 바탕으로 답변합니다.

## API

```http
POST /api/products/llm-search
```

요청 예:

```json
{
  "question": "영상 편집용 노트북 추천해줘",
  "size": 3
}
```

응답 예:

```json
{
  "question": "영상 편집용 노트북 추천해줘",
  "answer": "맥북 프로 14가 가장 적합합니다.",
  "products": [
    {
      "id": "....",
      "name": "Macbook Pro 14"
    }
  ]
}
```

## 왜 이렇게 하나

LLM만 바로 호출하면 근거 없이 답할 수 있습니다.

지금 구조는:

- 먼저 상품 검색
- 그다음 검색 결과만 보고 답변

이라서 더 안전합니다.

## 결과가 없으면

비슷한 상품이 없으면  
상품 목록은 비어 있고  
답변도 관련 상품이 없다고 내려갑니다.

## 설정

LLM 답변 기능은 기본값이 꺼져 있습니다.

```yaml
openai:
  chat:
    enabled: false
```

실제로 쓰려면:

```yaml
openai:
  chat:
    enabled: true
```

환경변수도 필요합니다.

```bash
OPENAI_API_KEY=...
```

## 켜져 있지 않으면

- 벡터 검색은 할 수 있습니다
- LLM 답변은 실제로 만들지 않습니다
- 대신 안내 문구와 상품 목록만 반환할 수 있습니다

## 관련 파일

- API: [ProductController.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/presentation/controller/ProductController.java)
- 서비스: [ProductApplicationService.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/application/service/ProductApplicationService.java)
- 요청 DTO: [ProductLlmSearchRequest.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/presentation/dto/request/ProductLlmSearchRequest.java)
- 응답 DTO: [ProductLlmSearchResponse.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/presentation/dto/response/ProductLlmSearchResponse.java)
- OpenAI 구현: [OpenAiProductLlmAnswerGenerator.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/infrastructure/llm/OpenAiProductLlmAnswerGenerator.java)

## 지금 기억할 것

- 먼저 검색하고 그다음 답변합니다
- 답변의 근거는 검색된 상품 목록입니다
- OpenAI를 켜야 실제 LLM 답변이 생성됩니다
