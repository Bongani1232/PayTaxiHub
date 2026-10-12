# PayTaxiHub frontend

This folder contains the HTML/CSS/JavaScript frontend served by the Spring Boot application. It is a demo frontend, not a React.js implementation.

## Authentication

Login and registration use the backend `/api/auth/login` and `/api/auth/register` endpoints. Authenticated requests use the HttpOnly `PTH_SESSION` cookie or a Bearer token.

## Password recovery

Password reset codes are not returned by the API by default. The project does not include an email/SMS delivery provider. For an isolated localhost demo only, set `RESET_CODE_EXPOSE=true` to display a demo reset code in the UI. Do not enable this setting on a shared network or deployment.

## Install

Run the Spring Boot project and open `http://localhost:8080/`. Configure MySQL and environment variables as documented in `README_DEMO.md`.
