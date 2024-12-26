# 패키지 구조
```
com.hhplus.hanghae_clean_arch
│
├── biz
│   ├── lecture
│      ├── application     # 여러 서비스를 조합해 사용하는 Facade 클래스
│      ├── controller      # API 요청을 처리하는 컨트롤러 클래스들
│      ├── domain          # 엔티티 클래스들 (Lecture, LectureHistory, Student 등)
│      ├── dto             # 데이터 전송 객체 (DTO) 클래스들
│      ├── repository      # 데이터베이스와 상호작용하는 리포지토리 클래스들
│      └── service         # 비즈니스 로직을 처리하는 서비스 클래스들
│
└── exception              # 사용자 정의 예외 클래스들
```


# ERD 설계
---
![image](https://github.com/user-attachments/assets/51898f2a-7fe8-40e1-85ac-ef916d08883a)

### 1. **Lecture (강의 테이블)**

- **목표**: 강의에 대한 기본 정보를 저장합니다.
- **주요 필드**:
    - `id`: 강의 고유 식별자 (자동 증가)
    - `title`: 강의 제목
    - `instructor`: 강의 강사
    - `capacity`: 강의 최대 정원
    - `currentEnrollment`: 현재 강의에 등록된 인원 수
    - `date`: 강의 시작 날짜
- **설계 이유**:
    - 강의 정보를 관리하려면 강의의 제목, 강사, 최대 정원, 현재 등록된 인원, 시작 날짜 등의 기본 정보가 필요합니다.
    - `capacity`와 `currentEnrollment`를 통해 강의의 수용 능력과 현재 등록된 학생 수를 추적할 수 있습니다.
    - `id`는 강의를 고유하게 구별할 수 있게 도와줍니다.
- **추후 개선..**:
    - currentEnrollment와 capacity의 값은 종종 변경될 수 있기 때문에 이를 데이터베이스에서 관리하기보다는 애플리케이션 레벨에서 처리할 수 있는 방법도 생각중에 있습니다.

### 2. **LectureHistory (강의 신청 내역 테이블)**

- **목표**: 학생이 신청한 강의 내역을 관리합니다.
- **주요 필드**:
    - `id`: 신청 내역 고유 식별자
    - `student`: 신청한 학생 (다른 테이블인 `Student`와 연결)
    - `lecture`: 신청한 강의 (다른 테이블인 `Lecture`와 연결)
    - `appliedAt`: 신청 시각
    - `status`: 신청 상태 (예: `APPLIED`, `CANCELED` 등)
- **설계 이유**:
    - 학생이 어떤 강의를 신청했는지 확인하려면 `student`와 `lecture` 정보를 연결하는 테이블이 필요합니다.
    - `LectureHistory`는 학생과 강의 사이의 관계를 나타내며, 학생이 신청한 강의를 기록합니다.
    - `status` 필드를 통해 학생의 신청 상태를 관리하고, 강의 신청과 관련된 다양한 상태 변경을 처리할 수 있습니다.

### 3. **Student (학생 테이블)**

- **목표**: 학생의 기본 정보를 저장합니다.
- **주요 필드**:
    - `id`: 학생 고유 식별자 (자동 증가)
    - `name`: 학생 이름
- **설계 이유**:
    - 학생 정보를 관리하려면 학생의 이름과 고유 식별자가 필요합니다.
    - `id`는 학생을 유니크하게 식별하는 데 사용되며, 다른 테이블(`LectureHistory`)에서 학생을 참조할 때 필요합니다.
