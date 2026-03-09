# 로컬 쿠버네티스 설치 가이드 (Windows & macOS)

쿠버네티스를 처음 쓰는 사람을 위한 로컬 설치 방법입니다.

## 공통 체크
- 하드웨어: CPU 2코어+, RAM 8GB+ 권장
- 가상화: BIOS/펌웨어에서 가상화(VT-x/AMD-V) 활성화
- 확인: Docker가 설치돼 있고 동작하는지 `docker run hello-world`로 테스트

---
## Windows 10/11
### A) Docker Desktop 내장 쿠버네티스 (가장 간단)
1. Docker Desktop 설치 (WSL2 권장) → [https://docs.docker.com/desktop/install/windows](https://docs.docker.com/desktop/install/windows)
2. Docker Desktop 실행 → Settings → Kubernetes → "Enable Kubernetes" 체크 → Apply & Restart
3. `kubectl` 클라이언트는 Docker Desktop에 포함됩니다. PowerShell에서 확인:
   ```powershell
   kubectl version --client
   kubectl get nodes
   ```

### B) Minikube (Docker 드라이버 사용)
1. Chocolatey 설치 후 minikube/kubectl 설치:
   ```powershell
   choco install -y minikube kubernetes-cli
   ```
2. Docker Desktop이 켜져 있는 상태에서 클러스터 시작:
   ```powershell
   minikube start --driver=docker
   ```
3. 확인:
   ```powershell
   kubectl get nodes
   ```

문제 시: WSL2 기능이 꺼져 있거나 Hyper-V 충돌이 없는지 확인. 방화벽/안티바이러스가 가상 네트워크를 막지 않는지 체크.

---
## macOS (Intel/Apple Silicon 공통)
### A) Docker Desktop 내장 쿠버네티스
1. Docker Desktop 설치 → [https://docs.docker.com/desktop/install/mac](https://docs.docker.com/desktop/install/mac)
2. Docker Desktop 실행 → Settings → Kubernetes → "Enable Kubernetes" 체크 → Apply & Restart
3. 터미널에서 확인:
   ```bash
   kubectl version --client
   kubectl get nodes
   ```

### B) Minikube (Homebrew)
1. Homebrew로 설치:
   ```bash
   brew install minikube kubectl
   ```
2. Docker Desktop이 켜져 있다면 Docker 드라이버를 사용해 시작 (권장):
   ```bash
   minikube start --driver=docker
   ```
   도커를 쓰지 않을 경우: `--driver=hyperkit`(Intel) 또는 `--driver=virtualbox` 등 선택
3. 확인:
   ```bash
   kubectl get nodes
   ```

문제 시: 리소스 부족으로 실패하면 `minikube start --cpus=4 --memory=8g`처럼 자원을 늘려 시도. Apple Silicon은 일부 x86 전용 이미지를 사용할 때 이슈가 있을 수 있으니 arm64 이미지인지 확인.

---
## 설치 후 공통 확인
```bash
kubectl cluster-info
kubectl get pods -A
```
