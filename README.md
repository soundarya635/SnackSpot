# 🍔 SnackSpot

SnackSpot is a full-stack, responsive food ordering and management application built with **React 19**, **Vite**, **Spring Boot 3**, **Spring Data JPA**, and **PostgreSQL**. Designed with a modern **Warm Cream & Brown** palette, it features food menu browsing, real-time search, cart management, simulated checkout, and a role-based Admin Portal.

---

## 🚀 Features

### 👤 Customer Features
- **User Registration & Login**: Account registration and authentication with role assignment.
- **Food Browsing**: Responsive grid displaying food items, prices, categories, and images.
- **Real-Time Search**: Instant search filtering by food name.
- **Cart & Checkout Simulation**: Dynamic cart management with UPI and Cash on Delivery (COD) options.

### 🛡️ Admin Features
- **Dedicated Admin Login**: Separate login portal checking `ADMIN` role privileges.
- **Role-Based Access Control (RBAC)**: Enforces access isolation between `USER` and `ADMIN` accounts.
- **Automatic Admin Seeding**: Default Admin account initialized automatically on backend startup.
- **Menu Management**: Add new food items and delete existing items.
- **Registered User Tracking**: View all registered user accounts and their assigned roles.

---

## 🛠️ Tech Stack

| Domain | Technology | Purpose |
| :--- | :--- | :--- |
| **Frontend** | React 19, Vite 7, Vanilla CSS | Component-based SPA styled with custom Cream & Brown theme rules. |
| **Backend** | Spring Boot 3.4.2, Java 17+ | RESTful API server with embedded Tomcat and global CORS handling. |
| **Persistence** | Spring Data JPA, Hibernate 6.6 | ORM layer mapping domain entities to relational PostgreSQL tables. |
| **Database** | PostgreSQL | Enterprise relational database for user and food item storage. |
| **Build Tools** | Maven Wrapper, npm | Multi-module build lifecycle and dependency management. |

---

## 📐 System Architecture

```
[ Client Browser (React 19 + Vite) ]
               │
               ▼  HTTP REST Requests (JSON)
[ Spring Boot 3 Backend Server (:8080) ]
               │
               ▼  JDBC / Spring Data JPA
  [ PostgreSQL Database (:5432) ]
```

---

## 📁 Project Structure

```
SnackSpot/
├── frontend/                   # React 19 + Vite SPA
│   ├── src/
│   │   ├── AdminDashboard.jsx  # Admin Portal component
│   │   ├── App.jsx             # Main container & session role router
│   │   ├── Home.jsx            # Food menu & cart component
│   │   ├── Login.jsx           # User & Admin authentication modal
│   │   ├── App.css             # Cream & Brown design system styles
│   │   └── main.jsx            # React DOM entry point
│   ├── .env.example            # Frontend environment variable template
│   └── package.json
│
├── backend/                    # Spring Boot 3 REST Service
│   ├── src/main/java/com/example/snackspotApi/
│   │   ├── Config/             # CORS config & Admin DataInitializer
│   │   ├── Controller/         # AuthController & FoodController
│   │   ├── Dto/                # AuthResponse & UserDto
│   │   ├── Entity/             # Food, User, and Role enum
│   │   └── Repository/         # UserRepository & FoodRepository
│   ├── src/main/resources/
│   │   └── application.properties # Environment-driven properties
│   └── pom.xml
│
├── .gitignore                  # Git exclusion rules
├── README.md                   # Portfolio documentation
├── PROJECT_STUDY_GUIDE.md      # Interview preparation guide
└── INTERVIEW_CHEATSHEET.md     # 10-minute quick revision guide
```

---

## 🔑 Environment Variables

### Backend (`backend/src/main/resources/application.properties`)
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/snackspotdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_postgres_password
CORS_ALLOWED_ORIGINS=http://localhost:5173
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
```

### Frontend (`frontend/.env.example`)
```properties
VITE_API_BASE_URL=http://localhost:8080
```

---

## 🔌 API Overview

### Authentication & Users
- `POST /auth/register` - Register a new customer account (`USER` role).
- `POST /auth/login` - Authenticate customer account.
- `POST /auth/admin/login` - Authenticate administrator account (`ADMIN` role).
- `GET /auth/users` - Fetch list of registered accounts (Admin only).

### Food Menu
- `GET /foods` - Fetch all food items.
- `POST /foods` - Create new food item (Admin).
- `DELETE /foods/{id}` - Delete food item by ID (Admin).
- `GET /foods/search?name=` - Search food items by keyword.

---

## 💻 Local Setup & Execution

### Prerequisites
- Java JDK 17+
- Node.js 18+ & npm
- PostgreSQL 14+ running on port `5432`

### 1. Database Creation
```sql
CREATE DATABASE snackspotdb;
```

### 2. Backend Setup
```cmd
cd backend
mvnw.cmd spring-boot:run
```

### 3. Frontend Setup
```cmd
cd frontend
npm install
npm run dev
```

Open your browser at **`http://localhost:5173`**.

---

## 🖼️ Screenshots & Demo Placeholders

- **Home Page (Cream & Brown Theme)**: `docs/screenshots/home.png`
- **Auth Modal (User & Admin Login)**: `docs/screenshots/auth.png`
- **Admin Dashboard**: `docs/screenshots/admin.png`

---

## 🔮 Future Enhancements

- **JWT Authentication**: Upgrade plaintext password validation to BCrypt hashing and JSON Web Tokens.
- **Image Upload Integration**: Direct file upload to AWS S3 or Cloudinary.
- **Payment Gateway**: Real Stripe / Razorpay API integration.
- **Docker Containerization**: Dockerfile and docker-compose orchestration.
