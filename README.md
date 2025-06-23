# 🌟 PromptDex: AI Prompt Collaboration Platform 🌟

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
<!-- Optional: Add build status, code coverage, etc. badges here -->
<!-- [![Build Status](https://travis-ci.org/your-username/promptdex.svg?branch=main)](https://travis-ci.org/your-username/promptdex) -->

Welcome to PromptDex, your ultimate hub for discovering, creating, and sharing AI prompts! Whether you're a seasoned AI enthusiast or just starting, PromptDex provides the tools and community to elevate your prompt engineering skills.

---

## 🚀 Features

### User-Facing Platform:
*   👤 **Authentication:** Secure user registration and login (local & OAuth2/OIDC via Google).
*   ✨ **Prompt Creation & Management:** Intuitive interface to create, edit, and manage your AI prompts (title, prompt text, description, target AI model, category).
*   🔍 **Advanced Search & Discovery:**
    *   Keyword search across prompt titles, descriptions, and text.
    *   Filter prompts by tags and categories.
    *   Paginated and sortable prompt listings.
*   🌟 **Reviews & Ratings:** Share your experience by reviewing and rating prompts. Average ratings help identify quality content.
*   🔖 **Bookmarking:** Save your favorite prompts for quick access.
*   🏷️ **Tagging System:** Organize and find prompts easily with a flexible tagging system.
*   👥 **User Profiles:** Showcase your contributions and discover prompts by other users.
*   👣 **Follow System:** (Backend support) Follow other users to keep up with their creations.

### 👑 Administration Panel:
*   🛡️ **Secure Access:** Dedicated admin login and role-based access control.
*   📊 **Dashboard Overview:** At-a-glance statistics (total users, prompts, reviews).
*   🧑‍💼 **User Management:**
    *   View all registered users with pagination.
    *   Search users by username or email.
    *   Edit user roles (e.g., assign/revoke `ROLE_ADMIN`, `ROLE_MODERATOR`).
*   📝 **Prompt Management:**
    *   View all prompts with pagination.
    *   Search prompts by title, content, author, category, or tags.
    *   Delete prompts.
*   💬 **Review Management:**
    *   View all reviews with pagination.
    *   Search reviews by comment content, author, or prompt title.
    *   Delete reviews.

---

## 🛠️ Technology Stack

### Backend (Spring Boot API - `promptdex-api`)
*   **Language:** Java 17+
*   **Framework:** Spring Boot 3.x
    *   Spring Web (RESTful APIs)
    *   Spring Data JPA (with Hibernate as ORM)
    *   Spring Security (Authentication, Authorization, JWT, OAuth2/OIDC)
*   **Database:** PostgreSQL (or any JPA-compatible relational database like MySQL, H2)
*   **Build Tool:** Maven
*   **Key Dependencies:**
    *   `spring-boot-starter-data-jpa`
    *   `spring-boot-starter-web`
    *   `spring-boot-starter-security`
    *   `spring-boot-starter-validation`
    *   `jjwt` (for JWT handling)
    *   `spring-boot-starter-oauth2-client`
    *   `lombok` (for boilerplate code reduction)
    *   `postgresql` (or your chosen DB driver)

### Main Frontend (User-Facing - `promptdex-frontend` - *Conceptual*)
*   **Framework/Library:** Vite + React (TypeScript)
*   **Styling:** CSS Modules / Styled-Components / Tailwind CSS (TBD)
*   **State Management:** Zustand (or Redux Toolkit, Context API)
*   **Routing:** React Router DOM
*   **API Client:** Axios

### Admin Panel Frontend (`promptdex-admin-frontend`)
*   **Framework/Library:** Vite + React (TypeScript)
*   **Styling:** Inline CSS / CSS Modules (Potential for UI Library integration)
*   **State Management:** Zustand
*   **Routing:** React Router DOM
*   **API Client:** Axios

---

## 📚 Project Structure (Simplified)



promptdex/
├── promptdex-api/ # Backend Spring Boot application
│ ├── src/main/java/com/promptdex/api/
│ │ ├── config/ # Security, CORS, etc.
│ │ ├── controller/ # REST API controllers (user & admin)
│ │ ├── dto/ # Data Transfer Objects
│ │ ├── exception/ # Custom exceptions & handlers
│ │ ├── mapper/ # Entity-DTO mappers
│ │ ├── model/ # JPA Entities
│ │ ├── repository/ # Spring Data JPA repositories
│ │ ├── security/ # JWT providers, UserDetails services
│ │ └── service/ # Business logic
│ └── pom.xml
│
├── promptdex-frontend/ # Main User-Facing Frontend (Conceptual)
│ ├── public/
│ ├── src/
│ │ ├── assets/
│ │ ├── components/
│ │ ├── pages/
│ │ ├── services/
│ │ ├── stores/
│ │ └── App.tsx
│ └── package.json
│
└── promptdex-admin-frontend/ # Admin Panel Frontend
├── public/
├── src/
│ ├── components/
│ ├── layouts/ # AdminLayout
│ ├── pages/ # LoginPage, DashboardPage, UserManagementPage, etc.
│ ├── services/ # apiClient.ts
│ ├── stores/ # authStore.ts
│ └── App.tsx
└── package.json




---

## 🚀 Getting Started

### Prerequisites
*   Java JDK 17 or newer
*   Maven 3.6+
*   Node.js 18.x or newer (with npm or yarn)
*   A running PostgreSQL instance (or other configured relational database)

### 1. Backend Setup (`promptdex-api`)

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/promptdex.git
    cd promptdex/promptdex-api
    ```

2.  **Configure Database:**
    *   Open `src/main/resources/application.properties` (or `application.yml`).
    *   Update the `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` properties to match your database setup.
    *   Ensure your database (e.g., `promptdex_db`) is created.

3.  **Configure OAuth2 (Optional - for Google Login):**
    *   If you want to use Google login, obtain OAuth2 credentials from Google Cloud Console.
    *   Update the `spring.security.oauth2.client.registration.google.*` properties in `application.properties`.

4.  **Build and Run:**
    ```bash
    mvn spring-boot:run
    ```
    The backend API will typically start on `http://localhost:8080`.

### 2. Admin Panel Frontend Setup (`promptdex-admin-frontend`)

1.  **Navigate to the admin frontend directory:**
    ```bash
    cd ../promptdex-admin-frontend # From promptdex-api directory, or cd promptdex/promptdex-admin-frontend from root
    ```

2.  **Install dependencies:**
    ```bash
    npm install
    # or
    # yarn install
    ```

3.  **Configure API Base URL (if needed):**
    *   The `apiClient.ts` (`src/services/apiClient.ts`) is configured to point to `http://localhost:8080/api`. If your backend runs on a different port or domain, update this.

4.  **Run the development server:**
    ```bash
    npm run dev
    # or
    # yarn dev
    ```
    The admin panel will typically start on `http://localhost:5173` (or another port specified by Vite).

### 3. (Conceptual) Main Frontend Setup (`promptdex-frontend`)
*(Instructions would be similar to the admin panel setup once this part is developed)*

1.  Navigate to `promptdex/promptdex-frontend`.
2.  Run `npm install` or `yarn install`.
3.  Run `npm run dev` or `yarn dev`.

---

## 🔑 Initial Admin User

To access the admin panel, you'll need an admin user.
1.  Register a new user through the main application's registration flow (if available) or directly if the backend supports it.
2.  Manually update the user's role in the database:
    *   Connect to your database.
    *   Locate the `users` table and the `user_roles` join table (or the `roles` column in the `users` table if roles are stored directly as a collection of strings/enums).
    *   Assign the `ROLE_ADMIN` to your chosen user.
    *   *Example SQL (if roles are in a separate join table `user_roles` linking `users.id` to a `roles.id` where `roles.name` is 'ROLE_ADMIN'):*
        ```sql
        -- Find your user_id and the role_id for 'ROLE_ADMIN'
        INSERT INTO user_roles (user_id, role_id) VALUES ('your_user_uuid', 'admin_role_uuid');
        ```
    *   *Example SQL (if roles are an element collection directly in the `users` table, e.g., a `roles` text array in PostgreSQL):*
        ```sql
        UPDATE users SET roles = array_append(roles, 'ROLE_ADMIN') WHERE username = 'your_admin_username';
        -- Or if it's a new user and roles column allows direct set:
        -- UPDATE users SET roles = '{ROLE_USER,ROLE_ADMIN}' WHERE username = 'your_admin_username';
        ```
    Consult your specific database schema for the exact SQL.

---

## 🛣️ Roadmap & Future Enhancements

*   **Tier 3.5 (Admin Panel - Ongoing):**
    *   [⬜] Advanced Filtering (by role, category, rating).
    *   [⬜] Enhanced UI/UX (Component library integration, improved error notifications).
    *   [⬜] Reusable UI Components (DataTable, Modals).
    *   [⬜] More detailed statistics on the Dashboard.
*   **Tier 4 (Deployment & Operations):**
    *   [⬜] Containerization with Docker (`Dockerfile` for backend & frontends, `docker-compose.yml`).
    *   [⬜] CI/CD Pipeline (e.g., GitHub Actions).
    *   [⬜] Cloud Deployment (AWS, GCP, Azure).
*   **User-Facing Enhancements:**
    *   [⬜] Real-time notifications.
    *   [⬜] Advanced user profile customization.
    *   [⬜] Prompt versioning.
    *   [⬜] Private prompts / collections.

---

## 🤝 Contributing

Contributions are welcome! If you'd like to contribute, please follow these steps:
1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/your-feature-name`).
3.  Make your changes.
4.  Commit your changes (`git commit -m 'Add some feature'`).
5.  Push to the branch (`git push origin feature/your-feature-name`).
6.  Open a Pull Request.

Please ensure your code adheres to the project's coding standards and includes tests where applicable.


---

## 🙏 Acknowledgements (Optional)

*   Thanks to the Spring Boot, React, and Vite communities.
*   Any other libraries or resources you found particularly helpful.