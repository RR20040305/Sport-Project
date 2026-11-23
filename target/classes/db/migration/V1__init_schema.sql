-- Таблица ролей
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Таблица пользователей
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    password_change_required BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP
);

-- Связь пользователей и ролей (многие ко многим)
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Серверные сессии для Refresh-токенов
CREATE TABLE refresh_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

-- Справочная сущность: Площадки (Facilities)
CREATE TABLE facilities (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Основная сущность: Бронирования (Bookings)
CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    booking_code VARCHAR(50) UNIQUE NOT NULL,
    facility_id UUID NOT NULL REFERENCES facilities(id),
    client_name VARCHAR(255) NOT NULL,
    client_phone VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    priority_level VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    archived BOOLEAN NOT NULL DEFAULT FALSE
);

-- Теги основной сущности
CREATE TABLE tags (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    color VARCHAR(20)
);

-- Связь бронирований и тегов
CREATE TABLE booking_tags (
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (booking_id, tag_id)
);

-- Заметки к бронированиям
CREATE TABLE notes (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Журнал аудита
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id UUID,
    correlation_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    description TEXT
);

-- Индексы для полей поиска и фильтрации
CREATE INDEX idx_bookings_facility ON bookings(facility_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_archived ON bookings(archived);
CREATE INDEX idx_bookings_client_name ON bookings(client_name);
CREATE INDEX idx_bookings_start_time ON bookings(start_time);
CREATE INDEX idx_audit_events_user ON audit_events(user_id);
CREATE INDEX idx_audit_events_created_at ON audit_events(created_at);