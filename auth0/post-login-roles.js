exports.onExecutePostLogin = async (event, api) => {
  if (event.authorization) {
    api.accessToken.setCustomClaim('https://restaurant-api/roles', event.authorization.roles);
  }
};
