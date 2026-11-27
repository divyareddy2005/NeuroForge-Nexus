import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { auth } from './auth';

type Workspace = { id: number; name: string; role: string };
type Member = { username: string; role: string };
type Project = { id: number; name: string; description: string; status: string; workspaceId: number };
type Stage = 'TODO' | 'IN_PROGRESS' | 'DONE';
type Task = { id: number; projectId: number; title: string; assignee: string; stage: Stage; dueDate: string | null };

const API = 'http://localhost:8081/api';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html'
})
export class AppComponent implements OnInit {
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  username = auth.tokenParsed?.['preferred_username'] || 'User';
  workspaces: Workspace[] = [];
  current: Workspace | null = null;
  projects: Project[] = [];
  members: Member[] = [];
  selectedProject: Project | null = null;
  tasks: Task[] = [];
  activeTab: 'projects' | 'board' | 'list' | 'planning' | 'team' = 'projects';
  busy = false;
  error = '';
  notice = '';
  showProjectForm = false;
  companyName = '';
  joinCode = '';
  projectName = '';
  projectDescription = '';
  taskName = '';
  taskAssignee = '';
  taskDue = '';
  invitationRole = 'DEVELOPER';
  inviteCode = '';
  stages: Stage[] = ['TODO', 'IN_PROGRESS', 'DONE'];
  roles = ['ADMIN', 'PROJECT_MANAGER', 'DEVELOPER', 'TESTER', 'DEVOPS_ENGINEER'];

  get canManage() { return ['ADMIN', 'PROJECT_MANAGER'].includes(this.current?.role || ''); }
  get canInvite() { return this.current?.role === 'ADMIN'; }
  get canMoveTask() { return ['ADMIN','PROJECT_MANAGER','DEVELOPER','TESTER'].includes(this.current?.role || ''); }

  async ngOnInit() { await this.loadWorkspaces(); }

  private async request<T>(method: string, path: string, data?: unknown): Promise<T> {
    await auth.updateToken(30);
    if (!auth.token) throw new Error('Login expired. Please sign in again.');
    return firstValueFrom(this.http.request<T>(method, API + path, {
      body: data, headers: { Authorization: 'Bearer ' + auth.token }
    }));
  }

  private fail(err: unknown) {
    console.error(err);
    const e = err as { status?: number; error?: {detail?: string; message?: string} };
    this.error = e?.status === 403 ? 'You do not have permission for this action.' :
      e?.status === 409 ? 'You are already a member of this workspace.' :
      e?.status === 400 ? 'Please check your input or invitation code.' :
      e?.status === 0 ? 'Cannot reach Spring Boot. Check the backend terminal.' :
      (e?.error?.detail || 'Something went wrong. Check your connection and try again.');
  }

  async loadWorkspaces() {
    this.busy = true; this.error = '';
    try {
      this.workspaces = await this.request<Workspace[]>('GET','/workspaces');
      if (this.workspaces.length) {
        const saved = this.current?.id;
        await this.selectWorkspace(this.workspaces.find(x => x.id === saved) || this.workspaces[0]);
      } else { this.current = null; this.projects = []; }
    } catch (err) { this.fail(err); }
    finally { this.busy = false; this.cdr.detectChanges(); }
  }

  async createWorkspace() {
    if (!this.companyName.trim()) return;
    this.busy = true; this.error = '';
    try {
      const created = await this.request<Workspace>('POST','/workspaces',{name: this.companyName.trim()});
      this.companyName = '';
      this.current = created;
      await this.loadWorkspaces();
      this.notice = 'Workspace created. You are its Admin.';
    } catch (err) { this.fail(err); }
    finally { this.busy = false; this.cdr.detectChanges(); }
  }

  async joinWorkspace() {
    if (!this.joinCode.trim()) return;
    this.busy = true; this.error = '';
    try {
      const joined = await this.request<Workspace>('POST','/workspaces/join',{code: this.joinCode.trim()});
      this.joinCode = '';
      this.current = joined;
      await this.loadWorkspaces();
      this.notice = 'You have joined the workspace.';
    } catch (err) { this.fail(err); }
    finally { this.busy = false; this.cdr.detectChanges(); }
  }

  selectWorkspaceById(id: number) { const w = this.workspaces.find(x => x.id === Number(id)); if (w) void this.selectWorkspace(w); }

  async selectWorkspace(w: Workspace) {
    this.current = w; this.activeTab = 'projects'; this.selectedProject = null;
    this.tasks = []; this.projects = []; this.members = []; this.inviteCode = '';
    this.error = ''; this.notice = '';
    try {
      this.projects = await this.request<Project[]>('GET',`/workspaces/${w.id}/projects`);
      this.members = await this.request<Member[]>('GET',`/workspaces/${w.id}/members`);
    } catch(err) { this.fail(err); }
    finally { this.cdr.detectChanges(); }
  }

  async createProject() {
    if (!this.current || !this.projectName.trim() || !this.canManage) return;
    this.busy = true; this.error = ''; this.notice = '';
    try {
      await this.request<Project>('POST',`/workspaces/${this.current.id}/projects`,{
        name: this.projectName.trim(), description: this.projectDescription
      });
      this.projectName = ''; this.projectDescription = ''; this.showProjectForm = false;
      this.projects = await this.request<Project[]>('GET',`/workspaces/${this.current.id}/projects`);
      this.notice = 'Project saved successfully.';
    } catch(err) { this.fail(err); }
    finally { this.busy = false; this.cdr.detectChanges(); }
  }

  async openProject(project: Project, tab: 'board' | 'list' | 'planning' = 'board') {
    if (!this.current) return;
    this.selectedProject = project; this.activeTab = tab; this.error = '';
    try {
      this.tasks = await this.request<Task[]>('GET',`/workspaces/${this.current.id}/projects/${project.id}/tasks`);
    } catch(err) { this.fail(err); }
    finally { this.cdr.detectChanges(); }
  }

  async createTask() {
    if (!this.current || !this.selectedProject || !this.taskName.trim() || !this.canManage) return;
    this.busy = true; this.error = '';
    try {
      const base = `/workspaces/${this.current.id}/projects/${this.selectedProject.id}/tasks`;
      await this.request<Task>('POST',base,{ title:this.taskName.trim(),assignee:this.taskAssignee,dueDate:this.taskDue || null });
      this.tasks = await this.request<Task[]>('GET',base);
      this.taskName = ''; this.taskAssignee = ''; this.taskDue = '';
      this.notice = 'Task added.';
    } catch(err) { this.fail(err); }
    finally { this.busy = false; this.cdr.detectChanges(); }
  }

  async moveTask(t: Task, stage: Stage) {
    if (!this.current || !this.selectedProject || !this.canMoveTask) return;
    this.error = '';
    try {
      const base = `/workspaces/${this.current.id}/projects/${this.selectedProject.id}/tasks`;
      const updated = await this.request<Task>('PATCH',`${base}/${t.id}/stage`,{stage});
      this.tasks = this.tasks.map(item => item.id === updated.id ? updated : item);
    } catch(err) { this.fail(err); }
    finally { this.cdr.detectChanges(); }
  }

  stageTasks(stage: Stage) { return this.tasks.filter(t=>t.stage===stage); }
  stageName(stage: Stage) { return ({TODO:'To Do',IN_PROGRESS:'In Progress',DONE:'Done'})[stage]; }
  dueTasks() { return this.tasks.filter(t=>!!t.dueDate).sort((a,b)=>(a.dueDate||'').localeCompare(b.dueDate||'')); }

  async createInvite() {
    if (!this.current || !this.canInvite) return;
    this.busy = true; this.error = '';
    try {
      const res = await this.request<{code:string}>('POST',`/workspaces/${this.current.id}/invites`,{role:this.invitationRole});
      this.inviteCode = res.code;
      this.notice = 'Invitation created. Share this code privately; it expires in 24 hours and works once.';
    } catch(err) { this.fail(err); }
    finally { this.busy = false; this.cdr.detectChanges(); }
  }
  logout() { void auth.logout({redirectUri:'http://localhost:4200'}); }
}
