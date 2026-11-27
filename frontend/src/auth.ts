import Keycloak from 'keycloak-js';
export const auth = new Keycloak({
  url: 'http://localhost:8180', realm: 'neuroforge', clientId: 'neuroforge-web'
});

export function userRoles(): string[] {
  const claims = auth.tokenParsed as { realm_access?: { roles?: string[] } } | undefined;
  return claims?.realm_access?.roles ?? [];
}
