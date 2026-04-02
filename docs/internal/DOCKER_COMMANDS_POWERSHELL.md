# 🐳 Docker Commands - PowerShell Guide
**PKU Diet App - ბექენდი და ფრონტენდი**

---

## 📋 **სარჩევი**
1. [სწრაფი გაშვება](#სწრაფი-გაშვება)
2. [ბექენდი (API + Database + Redis)](#ბექენდი-api--database--redis)
3. [ფრონტენდი (React UI)](#ფრონტენდი-react-ui)
4. [სრული სისტემა (ბექენდი + ფრონტენდი)](#სრული-სისტემა)
5. [სამუშაო ბრძანებები](#სამუშაო-ბრძანებები)

---

## ⚡ **სწრაფი გაშვება**

### 🚀 **ყველაფერი ერთად (Recommended)**
```powershell
# 1. გადადი project root-ში
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app

# 2. გაუშვი ბექენდი + database + redis
docker compose up -d

# 3. გაუშვი ფრონტენდი (ახალ terminal-ში)
cd ui
docker compose up
```

**შედეგი:**
- ✅ API: http://localhost:8080
- ✅ UI: http://localhost:5173
- ✅ Database: PostgreSQL (internal)
- ✅ Redis: Cache (internal)

---

## 🔧 **ბექენდი (API + Database + Redis)**

### **Option 1: Docker Compose (Recommended)**

```powershell
# პროექტის root დირექტორია
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app

# გაშვება (background-ში)
docker compose up -d

# ლოგების ნახვა
docker compose logs -f api

# სტატუსის შემოწმება
docker compose ps

# გაჩერება
docker compose down

# გაჩერება + მონაცემების წაშლა (volumes)
docker compose down -v
```

### **Option 2: ნაბიჯ-ნაბიჯ გაშვება**

#### **ნაბიჯი 1: Database (PostgreSQL)**
```powershell
# PostgreSQL კონტეინერი
docker run -d `
  --name pku-postgres `
  -e POSTGRES_DB=pku `
  -e POSTGRES_USER=pku `
  -e POSTGRES_PASSWORD=pku `
  -p 5432:5432 `
  -v pku-pgdata:/var/lib/postgresql/data `
  postgres:16-alpine

# შემოწმება
docker logs pku-postgres
```

#### **ნაბიჯი 2: Redis (Cache)**
```powershell
# Redis კონტეინერი
docker run -d `
  --name pku-redis `
  -p 6379:6379 `
  -v pku-redisdata:/data `
  redis:7-alpine redis-server --save 60 1 --loglevel warning

# შემოწმება
docker exec pku-redis redis-cli ping
# Expected: PONG
```

#### **ნაბიჯი 3: Backend API**
```powershell
# API-ის build
cd services/api
docker build -t pku-api:latest .

# API-ის გაშვება
docker run -d `
  --name pku-api `
  --network host `
  -e SPRING_PROFILES_ACTIVE=docker `
  -e SPRING_DATA_REDIS_HOST=localhost `
  -e SPRING_DATA_REDIS_PORT=6379 `
  -e DB_URL="jdbc:postgresql://localhost:5432/pku" `
  -e DB_USER=pku `
  -e DB_PASSWORD=pku `
  -p 8080:8080 `
  pku-api:latest

# ლოგების ნახვა
docker logs -f pku-api
```

### **Health Check**
```powershell
# API Health
Invoke-WebRequest -Uri http://localhost:8080/actuator/health | Select-Object -ExpandProperty Content

# Database Health
docker exec pku-postgres pg_isready -U pku -d pku

# Redis Health
docker exec pku-redis redis-cli ping
```

---

## 🎨 **ფრონტენდი (React UI)**

### **Option 1: Docker Compose**

```powershell
# გადადი ui დირექტორიაში
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app\ui

# გაშვება (foreground - ლოგები ხილული)
docker compose up

# გაშვება (background)
docker compose up -d

# ლოგების ნახვა
docker compose logs -f ui

# გაჩერება
docker compose down
```

### **Option 2: Docker Run**

```powershell
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app\ui

# UI კონტეინერი
docker run -it --rm `
  --name pku-ui `
  -v ${PWD}:/app `
  -w /app `
  -p 5173:5173 `
  -e VITE_API_BASE_URL=http://localhost:8080 `
  node:22-alpine `
  sh -c "npm install && npm run dev"
```

### **Production Build**

```powershell
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app\ui

# Build Docker image
docker build -t pku-ui:latest .

# გაშვება (production)
docker run -d `
  --name pku-ui-prod `
  -p 80:80 `
  pku-ui:latest

# ნახვა: http://localhost
```

---

## 🔗 **სრული სისტემა (ბექენდი + ფრონტენდი)**

### **ვარიანტი A: ორი Compose ფაილი**

#### **Terminal 1: Backend**
```powershell
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app
docker compose up -d
docker compose logs -f api
```

#### **Terminal 2: Frontend**
```powershell
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app\ui
docker compose up
```

### **ვარიანტი B: Unified Compose**

შექმენი `docker-compose.full.yml`:
```powershell
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app

# გაშვება
docker compose -f docker-compose.yml -f ui/docker-compose.yml up -d

# ან, თუ გაქვს single full compose file:
docker compose -f docker-compose.full.yml up -d
```

---

## 🛠️ **სამუშაო ბრძანებები**

### **📊 Status & Monitoring**

```powershell
# ყველა კონტეინერის სტატუსი
docker ps -a

# Compose services
docker compose ps

# CPU/Memory გამოყენება
docker stats

# ლოგები (ყველა service)
docker compose logs -f

# კონკრეტული service-ის ლოგები
docker compose logs -f api
docker compose logs -f db
```

### **🔄 Restart & Rebuild**

```powershell
# Restart service
docker compose restart api

# Rebuild და restart
docker compose up -d --build api

# ყველას rebuild
docker compose build --no-cache
docker compose up -d
```

### **🧹 Cleanup**

```powershell
# გაჩერება (volumes რჩება)
docker compose down

# გაჩერება + volumes წაშლა
docker compose down -v

# ყველა გაჩერებული კონტეინერის წაშლა
docker container prune -f

# ყველა unused image-ის წაშლა
docker image prune -a -f

# სრული cleanup (გაფრთხილება: წაშლის ყველაფერს!)
docker system prune -a --volumes -f
```

### **🐛 Debugging**

```powershell
# კონტეინერში შესვლა (API)
docker exec -it pku-api sh
# ან compose-ით:
docker compose exec api sh

# Database-ში შესვლა
docker exec -it pku-postgres psql -U pku -d pku

# Redis CLI
docker exec -it pku-redis redis-cli

# ფაილების კოპირება კონტეინერიდან
docker cp pku-api:/app/logs/app.log ./logs/

# Environment variables ნახვა
docker compose exec api env | Sort-Object
```

### **📦 Database Operations**

```powershell
# Database backup
docker exec pku-postgres pg_dump -U pku pku > backup_$(Get-Date -Format 'yyyy-MM-dd').sql

# Database restore
Get-Content backup.sql | docker exec -i pku-postgres psql -U pku -d pku

# Database shell
docker exec -it pku-postgres psql -U pku -d pku

# Run SQL file
Get-Content schema.sql | docker exec -i pku-postgres psql -U pku -d pku
```

### **🔍 Network Inspection**

```powershell
# Network-ების სია
docker network ls

# Compose network inspect
docker network inspect pku-diet-app_default

# კონტეინერის IP მისამართი
docker inspect pku-api | Select-String -Pattern "IPAddress"
```

---

## 🚀 **სრული სცენარები**

### **სცენარი 1: პირველი გაშვება (Fresh Start)**

```powershell
# 1. Project root
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app

# 2. .env ფაილის შექმნა (თუ არ არსებობს)
if (-not (Test-Path .env)) {
    Copy-Item env.example .env
    Write-Host "✅ .env ფაილი შეიქმნა. შეცვალე საჭირო მნიშვნელობები!"
}

# 3. Backend build & start
docker compose build
docker compose up -d

# 4. დაელოდე API-ს (health check)
Write-Host "⏳ ველოდები API-ს..."
$maxRetries = 30
for ($i = 0; $i -lt $maxRetries; $i++) {
    try {
        $response = Invoke-WebRequest -Uri http://localhost:8080/actuator/health -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ API მზადაა!"
            break
        }
    } catch {
        Write-Host "⏳ ველოდები... ($($i+1)/$maxRetries)"
        Start-Sleep -Seconds 2
    }
}

# 5. Frontend start
cd ui
docker compose up
```

### **სცენარი 2: Development Mode (Hot Reload)**

```powershell
# Terminal 1: Backend
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app
docker compose up -d db redis
# API გაუშვი IDE-დან (IntelliJ/VS Code) hot reload-ით

# Terminal 2: Frontend
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app\ui
npm install
npm run dev
# ან Docker-ით (volumes mount-ით hot reload მუშაობს):
docker compose up
```

### **სცენარი 3: Production Simulation**

```powershell
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app

# Production profile-ით
$env:SPRING_PROFILES_ACTIVE="prod"
docker compose up -d

# ან environment variable compose-ში:
docker compose --env-file .env.prod up -d

# Frontend production build
cd ui
docker build -t pku-ui:prod .
docker run -d -p 80:80 pku-ui:prod
```

### **სცენარი 4: Reset Everything**

```powershell
cd C:\Users\pc\OneDrive\Desktop\pku-diet-app

# ყველაფრის გაჩერება
docker compose down -v
cd ui
docker compose down -v
cd ..

# Volumes წაშლა
docker volume rm pku-diet-app_pgdata pku-diet-app_redisdata -f

# Images წაშლა
docker rmi pku-api:latest pku-ui:latest -f

# ახლიდან გაშვება
docker compose build --no-cache
docker compose up -d
```

---

## 📱 **URL-ები**

აპლიკაცია გაშვების შემდეგ ხელმისაწვდომია:

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:5173 | React UI (Development) |
| **Frontend Prod** | http://localhost | React UI (Production) |
| **Backend API** | http://localhost:8080 | REST API |
| **API Docs** | http://localhost:8080/swagger-ui.html | Swagger UI |
| **Health Check** | http://localhost:8080/actuator/health | API Health |
| **Metrics** | http://localhost:8080/actuator/prometheus | Prometheus Metrics |
| **Database** | localhost:5432 | PostgreSQL |
| **Redis** | localhost:6379 | Redis Cache |

---

## 🔐 **Default Credentials**

### Database
```
Host: localhost
Port: 5432
Database: pku
Username: pku
Password: pku
```

### Application
```
არ არის default user - გაიარე რეგისტრაცია:
POST http://localhost:8080/api/v1/auth/register
```

---

## ⚠️ **ხშირი პრობლემები და გადაწყვეტები**

### **Problem 1: Port already in use**
```powershell
# შეამოწმე რა იყენებს port-ს
netstat -ano | findstr :8080
netstat -ano | findstr :5173

# Process-ის კვლა
taskkill /F /PID <PID>

# ან Docker-ის cleanup
docker compose down
```

### **Problem 2: Database connection failed**
```powershell
# შეამოწმე database health
docker compose logs db

# Database restart
docker compose restart db

# ან სრული reset
docker compose down -v
docker compose up -d
```

### **Problem 3: npm install fails in UI**
```powershell
# Clean cache
cd ui
Remove-Item node_modules -Recurse -Force
Remove-Item package-lock.json -Force

# Docker volume-ის წაშლა
docker compose down -v
docker compose up
```

### **Problem 4: API არ იტვირთება (Spotless error)**
```powershell
# Format code before build
cd services/api
mvn spotless:apply
docker compose build api
docker compose up -d api
```

---

## 🎯 **Best Practices**

### ✅ **DO:**
- `docker compose up -d` უფრო სწრაფია development-ისთვის
- `docker compose logs -f` გამოიყენე debugging-ისთვის
- `.env` ფაილი დაამატე `.gitignore`-ში
- volumes გამოიყენე მონაცემების შესანახად

### ❌ **DON'T:**
- `docker system prune -a --volumes` production-ზე!
- `.env` ფაილი არ დაამატო Git-ში
- `docker compose down -v` მონაცემების წაშლის გარეშე ფიქრის

---

## 📚 **დამატებითი რესურსები**

- [Docker Compose Docs](https://docs.docker.com/compose/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)
- [Node.js Docker Hub](https://hub.docker.com/_/node)

---

**შექმნა:** 2025-10-01  
**პროექტი:** PKU Diet App  
**Docker Compose Version:** 3.8+

