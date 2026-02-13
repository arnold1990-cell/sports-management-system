CREATE TABLE IF NOT EXISTS head_offices (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    region VARCHAR(120),
    logo_url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS organization_roles (
    id UUID PRIMARY KEY,
    head_office_id UUID NOT NULL REFERENCES head_offices(id) ON DELETE CASCADE,
    role_name VARCHAR(80) NOT NULL,
    person_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    term_start DATE,
    term_end DATE
);

CREATE TABLE IF NOT EXISTS club_contacts (
    id UUID PRIMARY KEY,
    club_id UUID UNIQUE NOT NULL REFERENCES clubs(id) ON DELETE CASCADE,
    admin_name VARCHAR(255) NOT NULL,
    admin_phone VARCHAR(50),
    admin_email VARCHAR(255),
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS facilities (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sport_id UUID,
    location VARCHAR(255),
    capacity INTEGER,
    price_per_hour NUMERIC(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    owner_club_id UUID REFERENCES clubs(id)
);

CREATE TABLE IF NOT EXISTS maintenance_schedules (
    id UUID PRIMARY KEY,
    facility_id UUID NOT NULL REFERENCES facilities(id) ON DELETE CASCADE,
    start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    reason VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS subscription_plans (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(60) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    billing_period VARCHAR(20) NOT NULL,
    grace_days INTEGER NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY,
    subscriber_type VARCHAR(30) NOT NULL,
    subscriber_id UUID NOT NULL,
    plan_id UUID NOT NULL REFERENCES subscription_plans(id),
    status VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    grace_end_date DATE,
    auto_renew BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    provider VARCHAR(30) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    reference VARCHAR(120),
    status VARCHAR(30) NOT NULL,
    paid_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    invoice_number VARCHAR(60) NOT NULL UNIQUE,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    pdf_url VARCHAR(500),
    html_content TEXT
);

CREATE TABLE IF NOT EXISTS penalties (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    reason VARCHAR(255) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS reminder_logs (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    channel VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_rooms (
    id UUID PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    league_id UUID,
    club_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_participants (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    club_id UUID,
    role_snapshot VARCHAR(40)
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
    edited_at TIMESTAMP WITH TIME ZONE,
    deleted BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS message_read_receipts (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    read_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(message_id, user_id)
);

CREATE TABLE IF NOT EXISTS facility_bookings (
    id UUID PRIMARY KEY,
    facility_id UUID NOT NULL REFERENCES facilities(id) ON DELETE CASCADE,
    requested_by_user_id UUID NOT NULL REFERENCES users(id),
    club_id UUID REFERENCES clubs(id),
    start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_required BOOLEAN NOT NULL,
    payment_id UUID REFERENCES payments(id),
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS tournament_fixtures (
    id UUID PRIMARY KEY,
    competition_id UUID REFERENCES competitions(id) ON DELETE CASCADE,
    round_number INTEGER NOT NULL,
    home_team_id UUID REFERENCES teams(id),
    away_team_id UUID REFERENCES teams(id),
    fixture_id UUID REFERENCES fixtures(id),
    bracket_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(60) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE
);
