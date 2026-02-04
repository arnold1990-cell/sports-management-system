UPDATE users
SET password = crypt('Admin123!', gen_salt('bf'))
WHERE email = 'admin@sportsms.com';
