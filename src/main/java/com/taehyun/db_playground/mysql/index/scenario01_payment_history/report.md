# 대용량 데이터베이스 성능 실험 리포트

## 1. 실험 개요

* **목적**: 대용량 결제 히스토리 테이블에서 특정 조건 조회 시, 인덱스 유무 및 인덱스 구성 방식에 따른 성능 변화를 측정하고 최적의 인덱스 전략을 확인 합니다.
* **대상 테이블**: `payments_v1`
* **데이터 스케일**: 총 **5,000,000 건** (500만 건)
* **실험 환경**: Docker MySQL 8.x / Spring Boot 통합 테스트 환경

## 2. 테스트 시나리오

* **비즈니스 요구사항**
> "특정 유저가 지정한 특정 기간 동안 결제에 **성공(SUCCESS)** 한 내역 중, **카드(CARD)** 결제 건만 **최신순(created_at DESC)** 으로 화면에 노출합니다."

---

## 3. 성능 측정 결과 요약 (Benchmark)

| 실험 단계 | 소요 시간 (ms) | 소요 시간 (초) | 성능 개선율 | 비고 |
| --- | --- | --- | --- | --- |
| **Case 1: No Index** | 294 ms | 0.294 초 | 기준점 (100%) | Full Table Scan + Filesort 부하 발생 |
| **Case 2: 단일 인덱스 (`user_id`)** | 84 ms | 0.084 초 | **약 71.4% 개선** | Index Lookup 진입, `Using filesort` 잔존 |
| **Case 3: 복합 인덱스 (최적화)** | **79 ms** | **0.079 초** | **약 73.1% 개선** | `type: range`, **정렬 연산 완전 제거** |

---

## 4. 단계별 실행 계획(EXPLAIN) 및 상세 분석

### Case 1: 인덱스가 없는 상태 (No Index)

* **측정 결과**: `294 ms` (조회 건수: `4 건`)
* **MySQL 실행 계획 (`EXPLAIN`)**:

| id | select_type | table | type | rows | filtered | Extra |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | SIMPLE | p1_0 | **ALL** | 996,546 | 0.04 | Using where; **Using filesort** |

* **핵심 지표 분석**:
* `type: ALL`: 500만 건 대용량 테이블을 처음부터 끝까지 전부 읽는 Full Table Scan이 발생하여 대량의 디스크 I/O 유발.
* `rows: 996,546`: 단 4건의 실데이터를 찾기 위해 내부 통계 공식상 약 100만 개에 달하는 행을 검사하는 비효율 발생.
* `Extra: Using filesort`: 인덱스를 통한 정렬 최적화가 불가능하여, 쿼리 실행 후 메모리(Sort Buffer)에서 무거운 파일 정렬 연산을 수행함.


---

### Case 2: 단일 인덱스 적용 후 (`user_id`)

* **적용 인덱스**: `CREATE INDEX idx_user_id ON payments_v1(user_id);`
* **측정 결과**: `84 ms` (조회 건수: `4 건`)
* **MySQL 실행 계획 (`EXPLAIN`)**:

| id | select_type | table | type | key | rows | filtered | Extra |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | SIMPLE | p1_0 | **ref** | idx_user_id | 15 | 0.37 | Using where; **Using filesort** |

* **핵심 지표 분석**:
* `type: ref`: Full Table Scan에서 B-Tree 인덱스를 통한 고속 참조 조회(Index Lookup)로 변경.
* `rows: 15`: 탐색해야 할 행의 수가 100만 건에서 단 15건으로 대폭 감소하여 CPU 및 데이터 스캔 부하가 감소.
* `Extra: Using filesort`: `user_id` 조건은 인덱스를 탔으나, `created_at DESC` 정렬 조건을 인덱스가 지원하지 못해 메모리 정렬 연산이 여전히 존재.

---

### Case 3: 복합 인덱스 적용 후 (최적화 상태)

* **적용 인덱스**: `CREATE INDEX idx_user_payment_created ON payments_v1(user_id, payment_type, payment_status, created_at);`
* **측정 결과**: `79 ms` (조회 건수: `4 건`)
* **MySQL 실행 계획 (`EXPLAIN`)**:

| id | select_type | table | type | key | rows | filtered | Extra |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | SIMPLE | p1_0 | **range** | idx_user_payment_created | 4 | 100.00 | Using index condition; **Backward index scan** |

* **핵심 지표 분석**:
* `type: range`: 복합 인덱스 구성 후, `BETWEEN` 조건으로 지정된 특정 3달간의 데이터 범위만 타깃하여 스캔했음을 의미.
* `rows: 4` / `filtered: 100.00`: 최종 결과인 4건을 추출하기 위해 정확히 4건의 행만 검사함.
* `Extra: Backward index scan`: `Using filesort`가 나타나지 않음. 인덱스 마지막 열에 `created_at`을 배치하여 이미 정렬된 인덱스 블록을 역방향으로 읽어 내림으로써 **정렬 연산 비용을 0**으로 구성.
* `Extra: Using index condition`: ICP(Index Condition Pushdown) 메커니즘이 작동하여 디스크에서 테이블 레코드를 읽기 전 스토리지 엔진 레벨에서 조건을 선 필터링, 불필요한 디스크 I/O를 원천 차단함.

---

## 알게된 점 (Lessons Learned)

1. **복합 인덱스 설계의 대원칙 규칙**
   * 복합 인덱스 구성 시 딱 떨어지는 동등 조건(`=`) 컬럼들을 전방에 배치하고, 범위 조건(`BETWEEN`, `>`, `<`)이나 정렬 조건(`ORDER BY`) 컬럼을 후방에 배치해야 인덱스의 전 구간 스캔 효율이 극대화된다는 사실을 실측 데이터로 확인 했습니다.
2. **테스트 가독성 및 유지보수성 향상**
   * 테스트 코드 작성 시 하드코딩된 값 대신 고정 시점 필터를 매개변수(Parameter)로 추출하여 주입함으로, 가독성이 높고 시점이 들어가서 시간이 지나면 깨질 수 있는 테스트 코드를 안정성 있게 작성하는 방법을 알게되었습니다.
3. **테스트 API**
   * `public SELF allSatisfy(Consumer<? super ELEMENT> requirements)` 리스트의 요소값을 반복문 없이 검증.
   * `public SELF isSortedAccordingTo(Comparator<? super ELEMENT> comparator)` `Comparator`을 통해 리스트의 요소가 정렬이 되어있는지 검증 
