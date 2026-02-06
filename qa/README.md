# API Collection (Postman)

## Import
1. Open Postman.
2. Click **Import** and select:
   - `qa/postman_collection.json`
   - `qa/postman_environment.json`

## Configure
1. Select the **Sports Management System Environment**.
2. Update `BASE_URL` if your backend runs elsewhere.
3. Run **Auth → Login** to populate `ADMIN_TOKEN`, or update it manually.
4. Run **Auth → Register** to populate `USER_TOKEN`, or update it manually.

## Run
- Execute folders in order (Auth → Clubs → Teams → Players → Competitions & Seasons → Fixtures → Standings → Dashboard).
- IDs captured from responses are stored in environment variables and used by subsequent requests.
