const DOMAIN = 'restaurant-api-abbas.eu.auth0.com';
const ROLES_CLAIM = 'https://restaurant-api/roles';
const GUEST_ROLE = 'GUEST';
const GUEST_SCOPE = 'bookings:write';

exports.onExecutePostLogin = async (event, api) => {
  if (!event.authorization) {
    return;
  }

  let roles = event.authorization.roles;

  // A new user has no role, so RBAC removes every scope. Give the user GUEST in
  // Auth0, and also in this token, because event.authorization was read before the assignment.
  if (roles.length === 0) {
    await assignGuestRole(event);
    roles = [GUEST_ROLE];
    api.accessToken.addScope(GUEST_SCOPE);
  }

  api.accessToken.setCustomClaim(ROLES_CLAIM, roles);
};

async function assignGuestRole(event) {
  const token = await managementToken(event);
  const response = await fetch(
    `https://${DOMAIN}/api/v2/users/${encodeURIComponent(event.user.user_id)}/roles`,
    {
      method: 'POST',
      headers: { authorization: `Bearer ${token}`, 'content-type': 'application/json' },
      body: JSON.stringify({ roles: [event.secrets.GUEST_ROLE_ID] }),
    }
  );
  if (!response.ok) {
    throw new Error(`Could not assign GUEST role: ${response.status}`);
  }
}

async function managementToken(event) {
  const response = await fetch(`https://${DOMAIN}/oauth/token`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({
      grant_type: 'client_credentials',
      client_id: event.secrets.M2M_CLIENT_ID,
      client_secret: event.secrets.M2M_CLIENT_SECRET,
      audience: `https://${DOMAIN}/api/v2/`,
    }),
  });
  if (!response.ok) {
    throw new Error(`Could not get Management API token: ${response.status}`);
  }
  return (await response.json()).access_token;
}
