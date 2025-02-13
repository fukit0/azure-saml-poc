# SAML SSO with Azure Entra ID and Spring

This page provides a comprehensive overview of how Microsoft Entra Single Sign-On (SSO) functions, detailing its authentication flow, integration process, and key benefits.

## What is SSO?

Single Sign-On (SSO) is an authentication mechanism that allows users to log in once and gain access to multiple applications without needing to re-enter credentials. It improves security and user experience by centralizing authentication through an identity provider (IdP). In the context of Azure Entra ID (formerly Azure AD), SSO enables seamless access to applications using OAuth 2.0, OpenID Connect (OIDC), or SAML.

![image](https://github.com/user-attachments/assets/bddea6d7-1230-4e75-8c7c-c96d21675490)


## What is SAML?

SAML (Security Assertion Markup Language) is an XML-based authentication protocol used for Single Sign-On (SSO). It enables secure exchange of authentication and authorization data between an Identity Provider (IdP) (e.g., Azure Entra ID) and a Service Provider (SP) (e.g., our Spring application).


### How SAML Works in SSO:

1. User Requests Access → The user tries to access the Spring application (SP).
2. SP Redirects to IdP → The SP sends an authentication request to Azure Entra (IdP).
3. User Authenticates → Azure Entra prompts the user to log in (if not already logged in).
4. IdP Sends SAML Assertion → After successful authentication, Azure Entra generates a SAML assertion (token) and sends it back to the SP.
5. SP Validates Assertion → The Spring app verifies the assertion and grants access.

### Key Components in SAML:

• SAML Assertion → Contains user identity and attributes.
• Identity Provider (IdP) → The system that authenticates users (Azure Entra ID).
• Service Provider (SP) → The application that users want to access (Spring app).
• Metadata XML → Defines endpoints, certificates, and configuration details for both SP and IdP.

![image](https://github.com/user-attachments/assets/d00d6585-4296-40e0-98a0-1c30eed819c3)


## Configuring Azure Side

I will not delve into the details since this is related to SysOps(not sure). Only the required configurations will be emphasized here.

### Basic SAML Configuration

![image](https://github.com/user-attachments/assets/7aefa289-f652-41b6-a623-7a51c947fdad)

**Identifier (Entity ID):** The unique ID that identifies your application to Microsoft Entra ID. This value must be unique across all applications in your Microsoft Entra tenant. The default identifier will be the audience of the SAML response for IDP-initiated SSO.

**Reply URL (Assertion Consumer Service URL):** The reply URL is where the application expects to receive the authentication token. This is also referred to as the “Assertion Consumer Service” (ACS) in SAML. We don't need to manually implement this endpoint. Spring Security automatically registers the /login/saml2/sso/{registrationId} endpoint when SAML authentication is enabled.

**Sign on URL (Optional):** Sign on URL is used if you would like to perform service provider-initiated single sign-on. This value is the sign-in page URL for your application. This field is unnecessary if you want to perform identity provider-initiated single sign-on.

**Relay State (Optional):** The Relay State instructs the application where to redirect users after authentication is completed, and the value is typically a URL or URL path that takes users to a specific location within the application.

**Logout Url(Optional):** This URL is used to send the SAML logout response back to the application.

### Attributes & Claims
![image](https://github.com/user-attachments/assets/c8c1722e-5961-4b7b-8ec6-07ccb1426dc7)

These are the claims that we can access in the application via Saml2AuthenticatedPrincipal object. Claims can be extended.
![image](https://github.com/user-attachments/assets/de7cc8fb-660e-4dce-92fd-2543a9016b59)

## Configuring The Application Side

Please check the following files:
- `SecurityConfig.java`
- `application.properties`


## Demo

Single Sign-On (SSO) and Logout Workflow with Azure Entra ID

Authentication Workflow
1. User Accesses the Application
The user navigates to https://localhost:8443.

2. Initiating SSO Login
The user clicks the “Login SSO” button to authenticate.

3. Account Selection
If multiple accounts are available, the user is prompted to select the account to use for authentication.

4. User Authentication
The user enters credentials (e.g., password) to sign in via Azure Entra ID.

5. Redirection to Application
Upon successful authentication, the user is redirected to /logged-in, indicating a successful login session.


### Logout Workflow
1. User Initiates Logout
The user clicks the “Logout” button.

2. Account Selection for Sign-Out
If multiple accounts are active, the user is prompted to select which account to log out.

3. Azure Entra ID Logout Processing
Azure Entra ID terminates the user’s session and redirects to the configured post-logout URL (/logout-success).

4. Accessing the Application Post-Logout
If the user attempts to access the application again, they are redirected to the login page, requiring authentication to proceed.


---------------- 
Service Provider-Initiated (SP-Initiated) SSO

The user initiates the login process from the Service Provider (SP) (e.g., the Spring application), which then redirects the user to the Identity Provider (IdP) (e.g., Azure Entra ID) for authentication.

Identity Provider-Initiated (IdP-Initiated) SSO

The user begins authentication directly from the IdP (e.g., Azure Entra ID), which then generates a SAML response and redirects the user to the Service Provider for access.


----------------
Video recording of the demo can be found at [drive](https://drive.google.com/file/d/1Y0dnOgE63Sx_KgjhPgv8h3tr2jRgAa0Z/view?usp=sharing)
