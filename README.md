# Supply Gate - B2B Marketplace Platform

A robust full-stack B2B marketplace application connecting businesses with verified manufacturers and suppliers. The platform enables suppliers to showcase their products, allows industry workers to verify supplier credentials, and facilitates seamless business connections.

## 🏗️ Architecture

- **Frontend**: React (JavaScript) - Create React App
- **Backend**: Spring Boot 3.x (Java 17+)
- **Database**: PostgreSQL 13+
- **Authentication**: JWT (JSON Web Tokens)
- **File Storage**: Local file system

## 📁 Project Structure

```
SUPPLYGATE_WEB_APP/
├── supply_gate_react/          # React Frontend Application
│   ├── src/
│   │   ├── components/         # Reusable UI components
│   │   ├── contexts/          # React contexts (Notifications, etc.)
│   │   ├── lib/               # API client and utilities
│   │   ├── pages/             # Page components
│   │   │   ├── auth/         # Authentication pages
│   │   │   ├── dashboard/     # Supplier dashboard
│   │   │   ├── industry/      # Industry worker dashboard
│   │   │   └── website/       # Public website pages
│   │   └── App.js            # Main app with routing
│   ├── public/                # Static assets
│   └── package.json
│
└── supply_gate_26514/          # Spring Boot Backend Application
    ├── src/main/java/
    │   └── org/example/supply_gate_26514/
    │       ├── config/        # Configuration classes
    │       ├── contoller/      # REST controllers
    │       ├── dto/            # Data Transfer Objects
    │       ├── model/          # Entity models
    │       ├── repository/     # Data repositories
    │       ├── service/        # Business logic
    │       └── util/           # Utility classes
    ├── src/main/resources/
    │   └── application.yml     # Application configuration
    └── pom.xml                 # Maven dependencies
```

## 🚀 Quick Start

### Prerequisites

- **Java**: JDK 17 or higher
- **Node.js**: v14 or higher
- **PostgreSQL**: 13 or higher
- **Maven**: 3.6+ (or use included Maven wrapper)
- **npm** or **yarn**

### 1. Database Setup

1. Install and start PostgreSQL
2. Create a database:
   ```sql
   CREATE DATABASE supplygate_db;
   ```
3. Update database credentials in `supply_gate_26514/src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/supplygate_db
       username: your_username
       password: your_password
   ```

### 2. Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd supply_gate_26514
   ```

2. Build the project:
   ```bash
   ./mvnw clean install
   # Or on Windows:
   mvnw.cmd clean install
   ```

3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   # Or on Windows:
   mvnw.cmd spring-boot:run
   ```

4. The backend will start on `http://localhost:8080`

5. Access API documentation:
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/v3/api-docs`

### 3. Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd supply_gate_react
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create a `.env` file in the frontend root:
   ```env
   REACT_APP_API_URL=http://localhost:8080
   ```

4. Start the development server:
   ```bash
   npm start
   ```

5. The frontend will open at `http://localhost:3000`

## 🔑 Environment Variables

### Backend (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/supplygate_db
    username: postgres
    password: your_password

# Email Configuration (for 2FA and password reset)
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: ${EMAIL_PASSWORD}  # Set as environment variable

# JWT Secret (use a strong random key in production)
jwt:
  secret: YourSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong...
```

**Important**: Set the `EMAIL_PASSWORD` environment variable with your Gmail App Password for email functionality.

### Frontend (`.env`)

```env
REACT_APP_API_URL=http://localhost:8080
```

## 👥 User Roles

The platform supports multiple user roles:

- **SUPPLIER**: Can create stores, add products, submit verification documents
- **INDUSTRY_WORKER**: Can review and approve/reject supplier verifications
- **CLIENT**: Can browse products and contact suppliers
- **ADMIN**: Full system access

## ✨ Key Features

### Supplier Features
- ✅ User registration and authentication
- ✅ Two-factor authentication (2FA)
- ✅ Store management (create, update, delete)
- ✅ Product catalog management
- ✅ Product image uploads
- ✅ Supplier verification document submission
- ✅ Dashboard with analytics (views, likes, impressions)
- ✅ Message management
- ✅ Notification system

### Industry Worker Features
- ✅ Industry-specific verification review
- ✅ Approve/reject supplier verifications
- ✅ View verification documents
- ✅ Dashboard with verification statistics
- ✅ Search and filter verifications

### Public Website Features
- ✅ Product browsing
- ✅ Product search
- ✅ Contact supplier via email
- ✅ Responsive design

## 🔐 Authentication Flow

1. **Registration**: Users register with email, password, and role
2. **Login**: Users log in with credentials
3. **2FA**: Two-factor authentication via email code
4. **JWT Token**: Upon successful authentication, a JWT token is issued
5. **Protected Routes**: Frontend uses JWT token for authenticated API calls

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-2fa` - Verify 2FA code
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password

### Verification
- `GET /api/verification` - Get all verifications (paginated)
- `GET /api/verification/status-counts` - Get verification status counts
- `GET /api/verification/my-verification` - Get current user's verification
- `POST /api/verification/submit` - Submit verification documents
- `POST /api/verification/{id}/review` - Review verification (approve/reject)

### Products
- `GET /api/products/getProducts` - Get public product listings
- `POST /api/products` - Create product (authenticated)
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `POST /api/products/{id}/images` - Upload product images

### Stores
- `GET /api/stores` - Get user's stores
- `POST /api/stores` - Create store
- `PUT /api/stores/{id}` - Update store
- `DELETE /api/stores/{id}` - Delete store

### Dashboard
- `GET /api/dashboard/stats` - Get dashboard statistics

For complete API documentation, visit `http://localhost:8080/swagger-ui.html` when the backend is running.

## 🛠️ Development

### Backend Development

- **Build**: `./mvnw clean install`
- **Run**: `./mvnw spring-boot:run`
- **Test**: `./mvnw test`
- **Package**: `./mvnw package` (creates JAR in `target/`)

### Frontend Development

- **Start Dev Server**: `npm start`
- **Build for Production**: `npm run build`
- **Run Tests**: `npm test`
- **Lint**: ESLint is configured via Create React App

## 📦 Production Build

### Backend

```bash
cd supply_gate_26514
./mvnw clean package
java -jar target/supply_gate_26514-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd supply_gate_react
npm run build
# Production build will be in the 'build' folder
# Serve with: npx serve -s build
```

## 🔧 Configuration

### CORS Configuration

The backend is configured to accept requests from:
- `http://localhost:3000` (React dev server)
- `http://127.0.0.1:3000`

Update `CorsConfig.java` or `SecurityConfig.java` for production URLs.

### File Upload Configuration

- **Max file size**: 10MB per file
- **Max request size**: 50MB (for multiple files)
- **Upload directory**: `uploads/` (created automatically)

### Email Configuration

For Gmail:
1. Enable 2-factor authentication
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Set `EMAIL_PASSWORD` environment variable with the app password

## 🐛 Troubleshooting

### Backend Issues

**Port already in use:**
- Change port in `application.yml`: `server.port: 8081`

**Database connection failed:**
- Verify PostgreSQL is running
- Check database credentials in `application.yml`
- Ensure database exists

**Email not sending:**
- Verify `EMAIL_PASSWORD` environment variable is set
- Check Gmail App Password is correct
- Verify SMTP settings in `application.yml`

### Frontend Issues

**API connection errors:**
- Verify backend is running on `http://localhost:8080`
- Check `REACT_APP_API_URL` in `.env` file
- Verify CORS configuration in backend

**Build errors:**
- Clear `node_modules` and reinstall: `rm -rf node_modules && npm install`
- Check Node.js version: `node --version` (should be v14+)

**Styling issues:**
- Verify Tailwind CSS is configured
- Check `tailwind.config.js` exists
- Ensure `index.css` imports Tailwind directives

## 📝 Code Style

- **Backend**: Follow Java naming conventions, Spring Boot best practices
- **Frontend**: ESLint configured via Create React App
- **Formatting**: Use consistent indentation (2 spaces for frontend, 4 for backend)

## 🔒 Security Features

- JWT-based authentication
- Password encryption (BCrypt)
- Two-factor authentication
- Role-based access control (RBAC)
- CORS configuration
- File upload validation
- SQL injection prevention (JPA)



**Built with love using React and Spring Boot**
