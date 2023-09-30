# Core-service for catcher
### Swit 연결 필요

## Layer 설명
### Resource
내부 서비스끼리 통신할 때 사용

1. Web-Service > Catcher-Service
2. Catcher-Service > Core-Service

### Core
서비스의 핵심 로직이 존재하는 공간으로 추상적인 개념만 있어야 한다. 또는 도메인(분리해도 상관 없음)
1. 여러 정보를 모아서 조합해서 내보내는 로직

**있으면 안되는 부분**
1. 특정 DB를 사용한다(Repo 호출만 허용)
2. 특정 클라우드를 사용한다

### Datasource
1. DB와 통신할 때 사용

### Infrastructure
외부 서비스와 통신할 때 사용
1. AWS와 통신
