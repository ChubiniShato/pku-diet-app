# Docker Setup Guide for PKU Diet App UI

## 🚀 Quick Start

### 1. Prepare Dependencies (One-time setup)
```bash
# Option A: Use npm script (recommended)
cd ui
npm run docker:deps
cd ..

# Option B: Use fix script (Linux/Mac)
cd ui
chmod +x fix-docker-deps.sh
./fix-docker-deps.sh
cd ..

# Option C: Use fix script (Windows)
cd ui
./fix-docker-deps.bat
cd ..

# Option D: Manual commands
cd ui
rm -f package-lock.json
npm install
cd ..
```

### 2. Build and Run Full Stack
```bash
# Build and start all services
docker compose --profile fullstack up -d

# View logs to monitor build progress
docker compose logs -f ui
```

### 3. Verify Deployment
```bash
# Check all services are running
docker compose ps

# Should show:
# NAME          IMAGE          STATUS          PORTS
# pku-db-1      postgres:16    Up              0.0.0.0:5432->5432/tcp
# pku-api-1     pku-api        Up              0.0.0.0:8080->8080/tcp
# pku-ui-1      pku-ui         Up              0.0.0.0:80->80/tcp

# Test health endpoints
curl http://localhost/health          # UI health
curl http://localhost:8080/actuator/health  # API health

# Test deep links (SPA routing)
curl http://localhost/critical
curl http://localhost/day/2024-01-15

# Access the application
open http://localhost

# Quick verification
echo "✅ UI Health:" && curl -s http://localhost/health
echo "✅ API Health:" && curl -s http://localhost:8080/actuator/health
echo "✅ SPA Routing:" && curl -s -o /dev/null -w "%{http_code}" http://localhost/critical
```

## Issue Resolution: Docker Build Dependency Conflicts

### Problem
The Docker build failed because of dependency version mismatches between `package.json` and `package-lock.json`.

### Solution Applied
1. **Updated Dockerfile** to use `npm install` for maximum flexibility
2. **Modified build script** to use Vite's built-in TypeScript support
3. **Added `docker:deps` script** to regenerate dependencies when needed
4. **Created fix scripts** for both Windows and Linux/Mac

### Files Updated
- `ui/package.json` - Updated build scripts and dependencies
- `ui/Dockerfile` - Changed to `npm install` for flexibility
- `ui/fix-docker-deps.sh` - Linux/Mac fix script
- `ui/fix-docker-deps.bat` - Windows fix script
- `ui/env-config.txt` - Environment configuration template

## Understanding the Fix

### Why This Happened
- **npm ci** requires an existing `package-lock.json` file with exact version matches
- **npm install** is more flexible and generates/updates the lock file as needed
- Our fix scripts were deleting the lock file, causing `npm ci` to fail

### Why npm install Works Better for Docker
- **Flexible**: Works with or without existing `package-lock.json`
- **Self-healing**: Generates lock file if missing
- **Compatible**: Works with our dependency fix scripts

## Alternative Solutions (if build still fails)

### Option A: Force Clean Build
```bash
cd ui
npm run docker:deps  # This removes old lock file and reinstalls
cd ..
docker compose --profile fullstack up -d
```

### Option B: Manual Docker Build
```bash
# Build UI image manually
cd ui
docker build -t pku-diet-app-ui .

# Then run the full stack
cd ..
docker compose --profile fullstack up -d
```

### Option C: Force Clean Build
```bash
# Stop all containers
docker compose down

# Clean everything
docker system prune -f
docker builder prune -f

# Rebuild from scratch
docker compose --profile fullstack up --build -d
```

## Environment Configuration

### For Docker Deployment
The UI expects the API to be available at `http://api:8080/api/v1` (internal Docker network).

### For Local Development
If running the UI outside Docker, use:
```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Testing the Deployment

### 1. Check Container Status
```bash
docker compose ps
```

### 2. Check Health Endpoints
```bash
# API Health
curl http://localhost:8080/actuator/health

# UI Health
curl http://localhost/health
```

### 3. Test Deep Links
```bash
# Test SPA routing
curl http://localhost/critical
curl http://localhost/day/2024-01-15
```

### 4. Access the Application
- **UI**: http://localhost
- **API**: http://localhost:8080
- **Database**: localhost:5432

## Troubleshooting

### Build Still Failing?
1. Check Docker version: `docker --version`
2. Ensure sufficient disk space: `docker system df`
3. Clear all Docker data: `docker system prune -a`

### UI Not Loading?
1. Check browser console for errors
2. Verify API is running: `curl http://localhost:8080/actuator/health`
3. Check environment variables: `docker compose exec ui env | grep VITE_API_BASE_URL`

### Database Connection Issues?
1. Check database logs: `docker compose logs db`
2. Verify connection: `docker compose exec db pg_isready -U pku -d pku`

## Alternative: Manual Build

If Docker continues to have issues, you can build and run manually:

```bash
# Build the UI
cd ui
npm install
npm run build

# Serve with a simple HTTP server
npx serve -s dist -l 3000
```

Then access at http://localhost:3000

## Success Indicators

✅ **Docker containers running**:
```bash
docker compose ps
# Should show: db, api, ui all "Up"
```

✅ **Health checks passing**:
```bash
curl http://localhost/health
# Should return: "healthy"
```

✅ **UI accessible**:
- http://localhost loads the PKU Diet App
- Navigation works without 404 errors
- Deep links work (e.g., /critical, /day/2024-01-15)

The deployment is now ready with proper production build, Docker orchestration, and SPA routing support!
