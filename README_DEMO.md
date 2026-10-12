# PayTaxiHub

PayTaxiHub is a demo taxi payments and fleet-management app. It has portals for commuters, drivers, fleet owners, and administrators.

This is a school/demo project, not a production payment service. It does not process real card or bank payments, send real push notifications, or verify identities with an external service.

## Requirements

- Java 21
- Maven 3.9 or later
- MySQL 8 or later
- Node.js 20 or later and npm 10 or later (for frontend builds and tests)

## Set up and run

1. Create a MySQL database and an application user.
2. Set these environment variables before starting the app:
   - `DB_URL` — JDBC URL for your database
   - `DB_USERNAME` — database username
   - `DB_PASSWORD` — database password
   - `SESSION_COOKIE_SECURE=false` for local HTTP testing only. Use HTTPS and keep secure cookies enabled for deployment.
3. From the project folder, run `mvn spring-boot:run`.
4. Open `http://localhost:8080/index.html`.

Flyway applies the database migrations when the app starts. **Do not run `RESET_AND_CREATE_PAYTAXIHUB.sql` on a database with data you want to keep; it deletes the PayTaxiHub tables and data.**

The generated Tailwind stylesheet is already included. To rebuild it and run the frontend checks, first run `npm ci`, then `npm run build`.

## Deploy on Railway

Railway can host the app and its MySQL database and provide a public HTTPS address.

1. Sign in to [Railway](https://railway.com/) and create a project using **Deploy from GitHub repo**. Select `Bongani1232/PayTaxiHub` and authorize access if asked.
2. In the project, add a MySQL database service. Keep its service name `MySQL` so the variable references below work.
3. In the app service's **Variables**, add:

   ```text
   DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?serverTimezone=Africa/Johannesburg&sslMode=REQUIRED
   DB_USERNAME=${{MySQL.MYSQLUSER}}
   DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   SESSION_COOKIE_SECURE=true
   ```

   Railway builds the app from the included `Dockerfile`. Do not add real credentials to GitHub or turn on `RESET_CODE_EXPOSE`.
4. Wait for the app deployment to finish. In the app service, open **Settings → Networking → Public Networking** and choose **Generate Domain**.
5. Open the generated `https://…railway.app` address. The home page is `/index.html`.

The app uses Flyway migrations to create its tables on first startup. Do not run the reset SQL script against a database containing data you want to keep. This demo is not intended for real payments or sensitive personal information. Check Railway's current pricing and database backup options before using it.

## Tests

Run backend unit tests:

```text
mvn test
```

Run frontend tests:

```text
npm run test:frontend
```

Build the frontend and run its tests together:

```text
npm run build
```

Java unit tests are in `src/test/java/com/paytaxihub/`. Frontend tests are in `src/test/frontend/static-site.test.cjs`. Maven writes Java test results to `target/surefire-reports/`.

These tests do not replace testing the app against a real MySQL database.

## Demo limitations

- Password reset codes are hidden by default. The project has no email or SMS delivery provider. `RESET_CODE_EXPOSE=true` reveals codes only for an isolated local demo; never enable it on a shared or deployed app.
- Optional fleet-owner MFA uses one shared demo code, not a unique code for each person.
- Notifications are stored in memory and are lost when the app stops.
- Driver identity checks and administrator approvals are not connected to an external verification service.
- Do not use this app to handle real payments or sensitive production data.
