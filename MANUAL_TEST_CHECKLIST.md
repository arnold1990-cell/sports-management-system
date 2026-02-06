# Manual Test Checklist (Login, Roles, CRUD)

## Prerequisites
- Backend running on `http://localhost:8080`.
- Frontend running on `http://localhost:5173`.
- Admin credentials available (or create a user and elevate roles via DB).

## Authentication & Roles
1. **Register as Viewer**
   - **Step:** Navigate to `/register`, submit a new email/password/full name.
   - **Expected:** User is registered, logged in, and assigned the `VIEWER` role by default.
2. **Login as Admin**
   - **Step:** Navigate to `/login`, authenticate with an admin account.
   - **Expected:** Admin reaches dashboard; admin routes are accessible.
3. **Unauthorized Access**
   - **Step:** Open `/dashboard` in a private window (no token).
   - **Expected:** Redirected to login (401 guard); API returns 401 with error payload.
4. **Forbidden Role**
   - **Step:** Login as viewer and attempt to access `/admin/posts` or `/users`.
   - **Expected:** UI blocks route; API returns 403 if invoked directly.

## UI Role Visibility
1. **Admin Actions Visible**
   - **Step:** Login as admin; open Clubs, Teams, Players, Fixtures.
   - **Expected:** Create/edit/delete controls visible.
2. **Viewer Read-Only**
   - **Step:** Login as viewer; open same pages.
   - **Expected:** Admin-only controls hidden; only list views available.

## CRUD Scenarios
1. **Clubs**
   - **Create:** Add club with name/city.
   - **Update:** Edit club city.
   - **Delete:** Remove club.
   - **Expected:** API returns 200/204; UI refreshes with latest data.
2. **Teams**
   - **Create:** Add team tied to a club.
   - **Update:** Change coach/home ground.
   - **Delete:** Remove team.
   - **Expected:** Changes persist and lists update.
3. **Players**
   - **Create:** Add player with team and jersey number.
   - **Update:** Modify stats summary/position.
   - **Delete:** Remove player.
   - **Expected:** Changes persist and lists update.
4. **Competitions & Seasons**
   - **Create Season:** Add season with valid start/end dates.
   - **Create Competition:** Add competition with at least one team.
   - **Expected:** Seasons/competitions show in selectors and lists.
5. **Fixtures**
   - **Create Fixture:** Provide home/away teams, competition, season, venue, kickoff.
   - **Expected:** Fixture appears in fixtures list and dashboard.
6. **Dashboard**
   - **Step:** Visit `/dashboard` as admin.
   - **Expected:** Summary counts + latest fixtures/posts load without errors.

## Role-Specific Security Checks
1. **Admin Create/Update/Delete**
   - **Step:** Use admin to perform all CRUD operations.
   - **Expected:** API returns 200/201/204; UI reflects changes.
2. **Viewer Read-Only**
   - **Step:** Use viewer to attempt create/update/delete endpoints (via UI or API).
   - **Expected:** API returns 403; UI should not show admin controls.

## Fixture Rules & Validation
1. **Home vs Away**
   - **Step:** Try creating fixture where homeTeamId == awayTeamId.
   - **Expected:** API returns 400 with message `Home team and away team must be different`.
2. **Season Required**
   - **Step:** Try creating fixture without seasonId.
   - **Expected:** API returns 400 validation error.
3. **Competition Required**
   - **Step:** Try creating fixture without competitionId.
   - **Expected:** API returns 400 validation error.

## Error Handling
1. **Consistent Error Shape**
   - **Step:** Request a missing club (`/api/clubs/{badId}`).
   - **Expected:** JSON response includes `{ timestamp, status, error, message, path }`.
2. **Frontend Errors**
   - **Step:** Stop backend and load dashboard or posts.
   - **Expected:** Visible error messages with retry options, no infinite spinners.
