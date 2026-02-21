# tudubem

Spring Boot 기반의 월드/맵 관리 및 그리드맵 생성 서버입니다.  
H2 파일 DB를 사용하고, 센서맵 이미지를 업로드하여 GridMap을 생성/캐시할 수 있습니다.

## 기술 스택
- Java 21
- Spring Boot 4.0.3
- Spring WebFlux
- Spring Data JPA
- H2 Database (file mode)
- springdoc-openapi (Swagger UI)
- Lombok

## 실행 환경
- JDK 21
- Gradle Wrapper 사용 (`./gradlew`)

## 실행 방법
```bash
./gradlew bootRun
```

애플리케이션 기본 주소:
- `http://localhost:8080`

## 주요 접속 경로
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- H2 Console: `http://localhost:8080/h2-console`

H2 접속 정보:
- JDBC URL: `jdbc:h2:file:./data/tudubem-db;DB_CLOSE_DELAY=-1`
- Username: `sa`
- Password: (빈 값)

## 설정 파일
`src/main/resources/application.properties`

현재 주요 설정:
- 파일 DB: `spring.datasource.url=jdbc:h2:file:./data/tudubem-db;DB_CLOSE_DELAY=-1`
- JPA 방언: `spring.jpa.database-platform=org.hibernate.dialect.H2Dialect`
- 업로드 제한: `spring.webflux.multipart.max-disk-usage-per-part=10MB`
- 맵 이미지 저장 경로: `app.map.image-dir=./data/map`
- Swagger 경로:
  - `springdoc.swagger-ui.path=/swagger-ui`
  - `springdoc.api-docs.path=/api-docs`

## API 개요
- Map API: `/map`
- Area API: `/map/{mapId}/areas`
- Keepout API: `/map/{mapId}/keepout-zones`
- GridMap API: `/grid-map`
- Robot API: `/robot`

상세 API 문서:
- `docs/CONTROLLER_API.md`

## HTTP 테스트 스크립트
IntelliJ HTTP Client 기준:
- `src/test/http/map.http`
- `src/test/http/area.http`
- `src/test/http/keepout.http`
- `src/test/http/world.http`

`world.http`는 다음 흐름을 포함합니다.
- map 생성
- 센서맵 업로드
- keepout 생성/비활성화
- grid-map 빌드
- 캐시된 grid-map PNG 조회

## 테스트 실행
```bash
./gradlew test
```

## 프로젝트 구조
```text
src/main/java/org/example/tudubem
├── config                  # OpenAPI 설정
├── robot                   # Robot 도메인
└── world
    ├── area                # Area 도메인
    ├── grid                # GridMap 모델/팩토리
    ├── keepout             # Keepout 도메인
    ├── map                 # Map 도메인
    └── WorldService 등      # 월드 오케스트레이션/캐시
```
