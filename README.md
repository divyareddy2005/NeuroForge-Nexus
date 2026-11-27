# NeuroForge Nexus

## About the Project
NeuroForge Nexus is a web application for managing software projects and team members in a company. It is being developed as part of my Infosys internship project.

## Milestone 1 Objective
The objective of Milestone 1 is to create the basic application with user login, company workspaces, project management, and role-based access.

## Technologies Used
- **Angular:** Frontend pages and forms
- **Java and Spring Boot:** Backend REST APIs and application logic
- **PostgreSQL:** Stores workspaces, projects, members, and invitations
- **Keycloak:** User registration and login
- **Docker Compose:** Runs PostgreSQL and Keycloak locally
- **Maven and npm:** Run the backend and frontend

## Features Implemented in Milestone 1
- User registration and login using Keycloak
- Create a company workspace
- Invite users to a workspace using invitation codes
- Roles: Admin, Project Manager, Developer, Tester, and DevOps Engineer
- Create and view projects
- Check user permissions before allowing project creation

## Project Folders
- `frontend/` - Angular user interface
- `backend/` - Spring Boot APIs and database code
- `keycloak/` - Authentication configuration and login theme
- `docker-compose.yml` - PostgreSQL and Keycloak services

## How to Run Locally
Make sure Java, Maven, Node.js, npm, and Docker Desktop are installed.

1. Copy `.env.example` to `.env` and configure local development credentials where required.
2. From the main project folder, start the services:
   ```powershell
   docker compose up -d
   ```
3. In the `backend` folder, run:
   ```powershell
   $env:JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Kolkata"
   mvn spring-boot:run
   ```
4. In the `frontend` folder, run:
   ```powershell
   npm.cmd install
   npm.cmd start
   ```
5. Open `http://localhost:4200` in a browser.

This is a local development project, not a publicly deployed application.

## Next Steps
In Milestone 2, I plan to develop task management, task assignment, sprint planning, and a Kanban board. Basic task-related code is present in this project, but the full Milestone 2 workflow is still being developed.
