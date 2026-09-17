## GitHub Settings

A few GitHub settings must be provisioned on this repository and on the account
that owns it.

## Personal Access Token (PAT)

Create a PAT on the GitHub account that owns this repository, then copy and save
the token value for the next step.

- Under the account settings (not the repository settings):

    ```text
    Settings -- Developer settings -- Personal access tokens -- Tokens (classic)
    ```

## GitHub Actions Secret

### RUBENS_PAT_TOKEN

The workflows in this repository read an Action secret named
`RUBENS_PAT_TOKEN` during the build to read packages from `rubensgomes-org`
[Maven Packages](https://github.com/rubensgomes-org/mvn-pkgs).

- Create an Action repository secret in this repository and name it
  RUBENS_PAT_TOKEN storing the value from previously created PAT::

   ```text
   Repo's Settings -- Secrets and variables -- Actions -- New repository secret
   ```

### AZURE_CLIENT_SECRET

The workflows in this repository read an Action secret named
`AZURE_CLIENT_SECRET` which is used during signing in to Azure Cloud.

- Create an Action repository secret in this repository and name it
  AZURE_CLIENT_SECRET storing the value the Azure Service Principal password:

   ```text
   Repo's Settings -- Secrets and variables -- Actions -- New repository secret
   ```

### SONAR_TOKEN

The workflows in this repository read an Action secret named
`SONAR_TOKEN` which is used during SonarQube analysis.

- Create an Action repository secret in this repository and name it
  SONAR_TOKEN storing the SonarQube authentication token:

   ```text
   Repo's Settings -- Secrets and variables -- Actions -- New repository secret
   ```

## GitHub Actions Variables

### AZURE_CLIENT_ID

The workflows in this repository read an Action variable named
`AZURE_CLIENT_ID` which is used during signing in to Azure Cloud.

- Create an Action repository secret in this repository and name it
  AZURE_CLIENT_ID storing the value of the Azure Service Principal username:

   ```text
   Repo's Settings -- Secrets and variables -- Actions -- New repository secret
   ```

### AZURE_SUBSCRIPTION_ID

The workflows in this repository read an Action variable named
`AZURE_SUBSCRIPTION_ID` which is used during signing in to Azure Cloud.

- Create an Action repository secret in this repository and name it
  AZURE_SUBSCRIPTION_ID storing the value of the Azure Tenant ID:

   ```text
   Repo's Settings -- Secrets and variables -- Actions -- New repository secret
   ```

### AZURE_TENANT_ID

The workflows in this repository read an Action variable named
`AZURE_TENANT_ID` which is used during signing in to Azure Cloud.

- Create an Action repository secret in this repository and name it
  AZURE_TENANT_ID storing the value of the Azure Tenant ID:

   ```text
   Repo's Settings -- Secrets and variables -- Actions -- New repository secret
   ```

---
Author: [Rubens Gomes](https://rubensgomes.com/)
