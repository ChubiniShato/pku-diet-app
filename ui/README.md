# PKU Diet App - UI

React 18 + TypeScript + Vite application for the PKU Diet Management System.

## 🚀 Quick Start

### Development
```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The application will be available at `http://localhost:5173`

### Production Build
```bash
# Fix dependencies (one-time)
npm run docker:deps

# Build for production
npm run build

# Deploy with Docker
cd ..
docker compose --profile fullstack up -d
```

### Quick Fix for Docker Issues
```bash
# Windows
cd ui && ./fix-docker-deps.bat

# Linux/Mac
cd ui && chmod +x fix-docker-deps.sh && ./fix-docker-deps.sh
```

### Preview Production Build
```bash
# Preview production build locally
npm run preview
```

The built files will be in the `dist/` directory.

## 🐳 Docker Deployment

### Build Docker Image
```bash
# Build the Docker image
docker build -t pku-diet-app-ui .

# Run the container
docker run -p 80:80 pku-diet-app-ui
```

### Docker Compose (Full Stack)
```bash
# Start full stack (API + UI + Database)
docker compose --profile fullstack up

# Start only API and database
docker compose up

# Start only UI (requires API running)
docker compose --profile fullstack up ui
```

## 🔧 Environment Variables

### Development (.env)
Create a `.env` file in the root directory:

```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api/v1

# Application Configuration
VITE_APP_TITLE="PKU Diet App"
VITE_APP_VERSION="1.0.0"
```

### Production Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `VITE_API_BASE_URL` | Backend API base URL | `http://localhost:8080/api/v1` | Yes |
| `UI_PORT` | Port for UI service | `80` | No |
| `NODE_ENV` | Environment mode | `production` | No |

### Docker Compose Environment Variables

| Variable | Description | Default | Service |
|----------|-------------|---------|---------|
| `DB_NAME` | PostgreSQL database name | `pku` | Database |
| `DB_USER` | PostgreSQL username | `pku` | Database |
| `DB_PASSWORD` | PostgreSQL password | `pku` | Database |
| `DB_PORT` | PostgreSQL port | `5432` | Database |
| `SERVER_PORT` | API server port | `8080` | API |
| `UI_PORT` | UI server port | `80` | UI |
| `VITE_API_BASE_URL` | API URL for UI | `http://localhost:8080/api/v1` | UI |

## 📁 Project Structure

```
ui/
├── src/
│   ├── components/          # Reusable UI components
│   │   ├── accessibility/   # Accessibility utilities
│   │   ├── common/          # Common components (skeletons, states)
│   │   └── ...              # Feature-specific components
│   ├── lib/                 # Core utilities and API
│   │   ├── api/            # API clients and hooks
│   │   ├── types/          # TypeScript type definitions
│   │   └── toast/          # Toast notification system
│   ├── i18n/               # Internationalization
│   │   ├── locales/        # Translation files (en, ka, ru)
│   │   └── config.ts       # i18n configuration
│   ├── pages/              # Page components
│   ├── providers/          # React context providers
│   └── main.tsx            # Application entry point
├── dist/                   # Production build output
├── Dockerfile             # Multi-stage Docker build
├── nginx.conf            # Nginx configuration with SPA fallback
├── vite.config.ts        # Vite configuration
└── package.json          # Dependencies and scripts
```

## 🏗️ Build Configuration

### Vite Configuration Features
- **Path Aliases**: `@/` maps to `src/`
- **Code Splitting**: Vendor chunks for better caching
- **Minification**: Terser for optimal bundle size
- **Environment Variables**: `VITE_*` variables exposed to client
- **Source Maps**: Disabled in production for smaller bundles

### Nginx Configuration
- **SPA Fallback**: All routes fallback to `index.html`
- **Static Asset Caching**: 1-year cache for JS/CSS/images
- **Security Headers**: XSS protection, content type sniffing prevention
- **Gzip Compression**: Automatic compression for text assets
- **Health Checks**: `/health` endpoint for container orchestration

## 🔍 Development Commands

```bash
# Type checking
npm run type-check

# Linting
npm run lint

# Preview production build locally
npm run preview
```

## 🌐 Internationalization (i18n)

The application supports three languages:
- **English (en)** - Default
- **Georgian (ka)** - ქართული
- **Russian (ru)** - Русский

Language files are located in `src/i18n/locales/` and are automatically loaded based on user preference or browser settings.

## ♿ Accessibility

The application implements WCAG 2.1 AA compliance with:
- **Semantic HTML**: Proper heading hierarchy and ARIA labels
- **Keyboard Navigation**: Full keyboard accessibility
- **Screen Reader Support**: Live regions and announcements
- **Focus Management**: Visible focus indicators and logical tab order
- **Color Contrast**: 4.5:1 ratio for text and interactive elements

## 🔒 Security

### Docker Security
- **Non-root user**: Application runs as non-privileged user
- **Minimal base images**: Alpine Linux for smaller attack surface
- **Security headers**: XSS protection and content security policy

### Build Security
- **Dependency scanning**: Regular security audits of npm packages
- **Source maps disabled**: Prevents source code exposure in production
- **Environment isolation**: Sensitive data only in environment variables

## 📊 Monitoring

### Health Checks
- **API Health**: `/actuator/health` endpoint
- **UI Health**: `/health` endpoint
- **Database Health**: PostgreSQL connection checks

### Logs
- **API Logs**: Available in `./logs/` directory
- **Nginx Logs**: Available in container logs
- **Application Logs**: Browser console in development

## 🚀 Deployment Options

### Option 1: Docker Compose (Recommended)
```bash
# Full stack deployment
docker compose --profile fullstack up -d

# Access the application
# UI: http://localhost
# API: http://localhost:8080
```

### Option 2: Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pku-ui
spec:
  replicas: 3
  selector:
    matchLabels:
      app: pku-ui
  template:
    metadata:
      labels:
        app: pku-ui
    spec:
      containers:
      - name: ui
        image: pku-diet-app-ui:latest
        ports:
        - containerPort: 80
        env:
        - name: VITE_API_BASE_URL
          value: "http://api-service:8080/api/v1"
        livenessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 10
```

### Option 3: Cloud Platforms
The application can be deployed to:
- **Vercel**: Automatic deployments from Git
- **Netlify**: Static hosting with CDN
- **AWS S3 + CloudFront**: Scalable static hosting
- **Azure Static Web Apps**: Integrated CI/CD

## 🔧 Troubleshooting

### Common Issues

#### Build Fails
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

#### Docker Build Issues
```bash
# Build with no cache
docker build --no-cache -t pku-diet-app-ui .

# Check build logs
docker build -t pku-diet-app-ui . 2>&1 | tee build.log
```

#### Port Conflicts
```bash
# Check what's using port 80
lsof -i :80

# Use different port
docker run -p 8080:80 pku-diet-app-ui
```

#### API Connection Issues
```bash
# Test API connectivity
curl http://localhost:8080/actuator/health

# Check environment variables
docker compose exec ui env | grep VITE_API_BASE_URL
```

## 📝 Contributing

1. **Development Setup**:
   ```bash
   git clone <repository-url>
   cd pku-diet-app/ui
   npm install
   npm run dev
   ```

2. **Code Quality**:
   ```bash
   npm run lint
   npm run type-check
   ```

3. **Testing Build**:
   ```bash
   npm run build
   npm run preview
   ```

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.