# 🔬 DB Playground: Backend & Database Deep-Dive Laboratory

대규모 트래픽 환경과 금융 도메인(Toss-style)에서 요구되는 **데이터 정합성 보장, 조회 성능 최적화, 비동기 아키텍처**를 실험하고 검증하는 백엔드 기술 레퍼런스 저장소입니다.

단순한 기능 구현을 넘어 기술의 내부 원리를 파헤치고, 가설을 세워 테스트 코드로 수치적 개선을 증명하는 것을 목적으로 합니다.

---
# 📝 학습 방식

1. 가상의 시나리오를 `scenario.md`에 작성 한다.
2. TDD 적용으로 테스트 코드 먼저 작성한다.
3. 검증한 기록을 `report.md`에 작성한다.

**AI를 사용해 시나리오를 작성하고 구현 중 몰랐던 부분은 집고 넘어간다**

---

## 🛠️ Tech Stack & Infrastructure

- **Language:** Java 21
- **Framework:** Spring Boot 3.x, Spring Data JPA, Spring WebFlux
- **Query Option:** QueryDSL (Jakarta)
- **Databases:** MySQL 8.0, Redis 7.0, MongoDB 6.0 (Docker-driven)

---

## 📂 Extensible Package Structure

본 프로젝트는 기술 스택 단위의 최상위 패키지 구성을 통해 추후 새로운 기술(예: Kafka, Virtual Thread 등)을 언제든 유연하게 추가할 수 있는 범용적 구조를 가집니다.

```text
com.taehyun.dbplayground/
│
├── mysql/                          # MySQL 관련 성능 및 동시성 실험
│   ├── index/                      # 인덱스 최적화 및 실행 계획(EXPLAIN) 분석
│   └── lock/                       # DB 동시성 제어 (낙관적/비관적 락, 데드락)
│
├── jpa/                            # JPA & Hibernate 내부 메커니즘 최적화
│   └── optimization/               # 영속성 컨텍스트 관리 및 N+1 문제 해결
│
├── redis/                          # Redis 인메모리 솔루션 검증
│   ├── cache/                      # 고성능 캐싱 전략 및 Cache Stampede 방어
│   └── lock/                       # Redisson 기반 분산 락 구현 (다중 서버 동시성)
│
├── mongodb/                        # MongoDB NoSQL 아키텍처 실습
│   └── document/                   # 비정형 데이터 구조 설계 및 폴리글롯 퍼시스턴스
│
└── global/                         # 전역 공통 컴포넌트 (Config, Exception 등)
```
최 하위 폴더에는 `scenarioXX_학습 내용` 의 패키지를 두고 내부 `scenario.md` `report.md` 의 파일을 둔다
