# AI 이미지 API

이 문서는 현재 프로젝트에 추가된 이미지 API를 정리한 문서입니다.

## 한 줄로 보면

두 가지가 있습니다.

- 이미지를 보내서 설명 받기
- 문장을 보내서 이미지 만들기

## 1. 이미지 보내기

이미지를 업로드하면 AI가 이미지를 보고 설명합니다.

API:

```http
POST /api/ai/images/analyze
```

형식:

- `multipart/form-data`

필드:

- `image`: 이미지 파일
- `prompt`: 선택

예:

```bash
curl -X POST "http://localhost:8080/api/ai/images/analyze" \
  -F "image=@sample.png" \
  -F "prompt=이 이미지를 설명해줘"
```

응답 예:

```json
{
  "prompt": "이 이미지를 설명해줘",
  "answer": "책상 위에 노트북이 놓여 있고 화면에는 작업 창이 보입니다."
}
```

## 2. 이미지 받기

문장을 보내면 AI가 이미지를 만듭니다.

API:

```http
POST /api/ai/images/generate
```

요청 예:

```json
{
  "prompt": "노을지는 바다를 바라보는 흰 고양이",
  "size": "1024x1024"
}
```

응답 예:

```json
{
  "prompt": "노을지는 바다를 바라보는 흰 고양이",
  "format": "png",
  "imageBase64": "iVBORw0KGgoAAA..."
}
```

즉 이 API는 이미지 파일을 바로 주는 것이 아니라  
base64 문자열을 응답으로 줍니다.

## 현재 구조

흐름은 이렇게 갑니다.

1. 컨트롤러가 요청 받기
2. 서비스가 처리
3. `WebClient`로 OpenAI 호출
4. 응답을 프로젝트 DTO로 변환

## 관련 파일

- 컨트롤러: [AiImageController.java](../../src/main/java/com/grepp/backend5/ai/presentation/controller/AiImageController.java)
- 서비스: [AiImageService.java](../../src/main/java/com/grepp/backend5/ai/application/service/AiImageService.java)
- OpenAI 호출: [OpenAiImageClient.java](../../src/main/java/com/grepp/backend5/ai/infrastructure/client/OpenAiImageClient.java)
- WebClient 설정: [RestClientConfig.java](../../src/main/java/com/grepp/backend5/config/RestClientConfig.java)

## 현재 설정

현재 `application.yaml` 기준으로 이 값을 사용합니다.

```yaml
openai:
  image-analysis:
    model: gpt-5.4-nano
  image:
    api-key: ${OPENAI_API_KEY:}
    model: gpt-image-1-mini
```

## 모델명 참고

현재 이미지 쪽은 이렇게 보면 됩니다.

- 이미지 분석: `gpt-5.4-nano`
- 이미지 생성: `gpt-image-1-mini`

채팅 모델을 바꿀 때는 공식 모델명을 그대로 써야 합니다.

예:

- `gpt-5.2`
- `gpt-5-mini`
- `gpt-5-nano`

## 필요한 것

- `OPENAI_API_KEY`
- 서버 재기동

## 지금 기억할 것

- `analyze`는 이미지 업로드 API입니다
- `generate`는 이미지 생성 API입니다
- 외부 통신은 `WebClient`로 합니다
