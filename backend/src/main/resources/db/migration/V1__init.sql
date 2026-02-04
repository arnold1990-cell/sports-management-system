CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE clubs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    city VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    club_id UUID REFERENCES clubs(id) ON DELETE SET NULL,
    coach_name VARCHAR(255),
    home_ground VARCHAR(255),
    logo_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE players (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID REFERENCES teams(id) ON DELETE SET NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    dob DATE NOT NULL,
    position VARCHAR(100) NOT NULL,
    jersey_number INTEGER,
    status VARCHAR(50) NOT NULL,
    stats_summary TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE seasons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL
);

CREATE TABLE competitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL
);

CREATE TABLE competition_teams (
    competition_id UUID NOT NULL REFERENCES competitions(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    PRIMARY KEY (competition_id, team_id)
);

CREATE TABLE fixtures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    home_team_id UUID NOT NULL REFERENCES teams(id),
    away_team_id UUID NOT NULL REFERENCES teams(id),
    competition_id UUID NOT NULL REFERENCES competitions(id),
    season_id UUID NOT NULL REFERENCES seasons(id),
    referee_id UUID REFERENCES users(id),
    venue VARCHAR(255) NOT NULL,
    match_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    home_score INTEGER,
    away_score INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE fixture_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fixture_id UUID NOT NULL REFERENCES fixtures(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    scorer_name VARCHAR(255) NOT NULL,
    minute INTEGER NOT NULL
);

CREATE TABLE posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    cover_image_url VARCHAR(500),
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    author_name VARCHAR(255),
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_posts_status_created ON posts(status, created_at DESC);
CREATE INDEX idx_comments_post_created ON comments(post_id, created_at DESC);
CREATE INDEX idx_fixtures_competition_season ON fixtures(competition_id, season_id);

INSERT INTO users (id, email, password, full_name)
VALUES ('11111111-1111-1111-1111-111111111111', 'admin@sportsms.com', '$2b$12$J5uIWMvS3I5DAANq7Q6/RO9KfV4F94VO5/o0.W2.7THDMtZeqLzVq', 'System Admin');

INSERT INTO user_roles (user_id, role) VALUES
('11111111-1111-1111-1111-111111111111', 'ADMIN');

INSERT INTO clubs (id, name, city) VALUES
('22222222-2222-2222-2222-222222222222', 'Metro FC', 'Metro City');

INSERT INTO teams (id, name, club_id, coach_name, home_ground, logo_url) VALUES
('33333333-3333-3333-3333-333333333333', 'Metro FC Seniors', '22222222-2222-2222-2222-222222222222', 'Alex Turner', 'Metro Stadium', NULL),
('44444444-4444-4444-4444-444444444444', 'Harbor United', NULL, 'Jordan Lee', 'Harbor Arena', NULL);

INSERT INTO players (team_id, first_name, last_name, dob, position, jersey_number, status, stats_summary)
VALUES
('33333333-3333-3333-3333-333333333333', 'Liam', 'Stone', '1998-04-12', 'Forward', 9, 'ACTIVE', '10 goals this season'),
('33333333-3333-3333-3333-333333333333', 'Ethan', 'Cole', '2000-08-22', 'Midfielder', 8, 'ACTIVE', '5 assists this season');

INSERT INTO seasons (id, name, start_date, end_date) VALUES
('55555555-5555-5555-5555-555555555555', '2025/2026', '2025-08-01', '2026-05-31');

INSERT INTO competitions (id, name, type) VALUES
('66666666-6666-6666-6666-666666666666', 'Premier League', 'LEAGUE');

INSERT INTO competition_teams (competition_id, team_id) VALUES
('66666666-6666-6666-6666-666666666666', '33333333-3333-3333-3333-333333333333'),
('66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444');

INSERT INTO fixtures (id, home_team_id, away_team_id, competition_id, season_id, referee_id, venue, match_date, status, home_score, away_score)
VALUES
('77777777-7777-7777-7777-777777777777', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', '66666666-6666-6666-6666-666666666666', '55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', 'Metro Stadium', NOW() + INTERVAL '7 days', 'SCHEDULED', NULL, NULL);

INSERT INTO posts (id, title, content, cover_image_url, author_id, status)
VALUES
('88888888-8888-8888-8888-888888888888', 'Season Kickoff', 'The new season is here! Get ready for exciting matches.', NULL, '11111111-1111-1111-1111-111111111111', 'PUBLISHED');

INSERT INTO comments (post_id, author_id, author_name, content)
VALUES
('88888888-8888-8888-8888-888888888888', '11111111-1111-1111-1111-111111111111', 'System Admin', 'Welcome everyone!');
