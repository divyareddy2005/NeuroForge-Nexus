# NeuroForge Nexus — Workspaces + Tasks (learning upgrade)

This is an **educational upgrade** of the earlier `NeuroForge_Milestone1_Starter` project. It is intended for local development and your Infosys learning presentation, **not for public deployment**. It implements Milestone 1 features and a simple preview of Milestone 2.

## Features included

- Keycloak login and self-registration (same realm/client as your original starter).
- Any authenticated user can create a **company workspace** and becomes **Admin of that workspace**, not a global system admin.
- Company Admin creates **single-use, 24-hour invitation codes** that assign workspace roles to members.
- The same user may belong to multiple workspaces, potentially with different roles.
- Members can only view projects, task boards, and team lists in their own workspaces. Workspace membership is checked by the Java backend on every API request.
- Admin and Project Manager can create projects and tasks.
- Admin, Project Manager, Developer, and Tester can update the status of tasks.
- Projects dashboard, Kanban board, task list, simple due-date planning view, team and invitations.
- Create-project and create-task actions display success or failure feedback.
- CI/CD and deployment screens are **not implemented** yet (Milestones 3–4).

## Important: this differs from your old project

Previously, `GET /api/projects` returned **every project** and roles were global Keycloak realm roles. The new API uses paths like `/api/workspaces/1/projects`, which enforce workspace membership. **The previous unscoped project records are preserved in PostgreSQL but are not shown in new workspaces** until you explicitly migrate them. This avoids exposing old records to random new companies. Back up your database before any manual migration.

Do **not** delete Docker volumes or run `docker compose down -v`: that would delete your PostgreSQL data. This upgrade keeps the same Docker Compose configuration and volume name, but Docker Compose project-name differences can create a separate volume. The safest approach is to copy **the updated frontend, backend and README into your existing `NeuroForge_Milestone1_Starter` directory**, keeping its original `docker-compose.yml` and existing `keycloak/` folder. First back up the whole project and your PostgreSQL data.

## Versions

- Java **21**; Maven **3.9.x**
- Spring Boot **3.5.16**
- Angular **22.x**; Node.js **22.22.3+ on the Node 22 line**
- Docker Desktop with WSL 2
- PostgreSQL 17 and Keycloak 26.8.2 via Docker Compose

## Start in Windows VS Code

**Before changing code**:
1. Stop the current Angular and Spring Boot terminals with `Ctrl+C`.
2. Make a copy of your working old project folder as backup.
3. Copy `backend` and `frontend` from this upgrade into the old project's folder, replacing their source files. Keep the old `docker-compose.yml` and `keycloak/` settings. Do not copy `node_modules` or `target` directories.
4. Make sure Docker Desktop is running.

**Terminal 1 — in the project root**:

```powershell
docker compose up -d
docker compose ps
```

**Terminal 2 — in `backend`**:

```powershell
cd backend
$env:JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Kolkata"
mvn spring-boot:run
```

**Terminal 3 — in `frontend`**:

```powershell
cd frontend
npm.cmd install
npm.cmd start
```

Open http://localhost:4200. The frontend uses `ChangeDetectorRef.detectChanges()` after its network calls to prevent the "Loading projects / 0 projects" issue you encountered earlier.

## Demo: two companies, two admins

1. Log in as your existing test user `manager` / `(not included in GitHub snapshot)` in the local Keycloak realm. You will see **Create workspace**, since earlier Keycloak realm roles do not automatically grant workspace membership.
2. Create `BrightTech Solutions` and become workspace **ADMIN**. Create a project such as `College Management Website`.
3. Open **Team & access**, select `DEVELOPER`, create an invitation, and copy its code privately.
4. Log out. In Keycloak, use **Register** to make a second user account (if your realm has self-registration enabled), or use your existing `developer` / `(not included in GitHub snapshot)` demo account.
5. On the onboarding screen, use **Join workspace** with the invitation code. This user can see projects but cannot create one.
6. Sign out. Register or use a different account, create a second workspace, and verify it has a **separate** project list.
7. If the same person needs to administer multiple companies, they can create another workspace or accept an Admin invitation into another workspace, then switch workspaces in the sidebar.

If Register doesn't show, check Keycloak's `neuroforge` realm setting for user registration. Updating `keycloak/neuroforge-realm.json` alone will **not** change an already-imported realm.

## User role rules

| Action | ADMIN | PROJECT_MANAGER | DEVELOPER | TESTER | DEVOPS_ENGINEER |
|---|---|---|---|---|---|
| View own workspace projects/tasks | Yes | Yes | Yes | Yes | Yes |
| Create project or task | Yes | Yes | No | No | No |
| Change task status | Yes | Yes | Yes | Yes | No |
| Invite people into workspace | Yes | No | No | No | No |

**Keycloak system admin (`http://localhost:8180/admin/`) is separate from a company workspace Admin**. Do not give normal company administrators access to the Keycloak system console.

## API reference (authenticated bearer token)

- `GET /api/workspaces` — your workspaces
- `POST /api/workspaces` — create one and become its Admin
- `POST /api/workspaces/join` — redeem invitation
- `GET /api/workspaces/{id}/members` — workspace members
- `POST /api/workspaces/{id}/invites` — Admin invitation
- `GET /api/workspaces/{id}/projects` — workspace projects
- `POST /api/workspaces/{id}/projects` — create project (Admin / PM)
- `GET /api/workspaces/{id}/projects/{projectId}/tasks` — project tasks
- `POST /api/workspaces/{id}/projects/{projectId}/tasks` — create task (Admin / PM)
- `PATCH /api/workspaces/{id}/projects/{projectId}/tasks/{taskId}/stage` — update status (Admin / PM / Developer / Tester)

### Example DTOs

```json
{"name":"BrightTech Solutions"}
```

```json
{"name":"College Management Website","description":"Student and attendance system"}
```

```json
{"title":"Create login page","assignee":"developer","dueDate":"2026-12-10"}
```

```json
{"stage":"IN_PROGRESS"}
```

## Your previous projects: optional manual migration

The previous schema has projects with `workspace_id` = NULL. That is intentional. **Only after you create your own workspace**, you may assign existing records to it if they truly belong there. First back up the database, check the selected workspace ID, and preview the old rows. Using `psql` in the PostgreSQL container, an example is:

```sql
SELECT id, name, workspace_id FROM projects ORDER BY id;
-- Replace 1 with the actual workspace id you own, and check the rows first:
UPDATE projects SET workspace_id = 1 WHERE workspace_id IS NULL;
```

Do not perform this update blindly on shared or production data; it would mix all legacy projects into a single workspace. If the old database was shared, migrate each project only to its verified owner.

## Known limits / important security work before public use

This upgrade is **not a production-ready worldwide SaaS application**. It uses one locally hosted Keycloak realm, Keycloak development mode, HTTP, sample passwords, PostgreSQL automatic schema update, and a simple workspace invitation mechanism. Before external deployment you would need:

- Production Keycloak and PostgreSQL with persistent, backed-up storage; HTTPS; secrets management; verification of company ownership and email; abuse/rate limiting.
- Stronger membership onboarding, email invitations, audit logs, member removal, role edits, account recovery, session policies, and tests for cross-workspace isolation.
- Row-level locking/atomic redemption for invitation codes (currently single-use logically, but not concurrency-hardened).
- Database migrations (Flyway/Liquibase) instead of Hibernate `ddl-auto=update`.
- Reliable ID-based task assignment, project-level membership/permissions, notifications, comprehensive input handling and API tests.
- Actual CI/CD pipelines, builds, releases and metrics for Milestones 3–4, rather than demonstration placeholders.

### About originality and company submissions

This is new example code for you to study, adapt, and test. **It was generated with AI assistance**, and no one can guarantee that a plagiarism or AI-detection system will classify it a certain way. Make meaningful changes you understand, add tests and your own documentation, and follow your company's rules for disclosing assistance and submitting code.
