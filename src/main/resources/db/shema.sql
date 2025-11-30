CREATE TYPE chat_status AS ENUM ('OPEN', 'CLOSED');

CREATE TABLE IF NOT EXISTS cities
(
    id     SERIAL PRIMARY KEY,
    region VARCHAR(55) NOT NULL,
    name   VARCHAR(55) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_region_city_name ON cities (region, name);

CREATE TABLE IF NOT EXISTS users
(
    id          UUID PRIMARY KEY,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    surname     VARCHAR(100) NOT NULL,
    name        VARCHAR(100) NOT NULL,
    patronymic  VARCHAR(100) NOT NULL,
    avatar_path TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    city        INT          NOT NULL REFERENCES cities ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS skills
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(55) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS masters_info
(
    master_id    UUID         NOT NULL REFERENCES users ON DELETE CASCADE,
    experience   INT          NOT NULL CHECK (experience > 0),
    description  TEXT         NULL,
    pseudonym    VARCHAR(100) NULL UNIQUE,
    phone_number VARCHAR(10)  NOT NULL UNIQUE,
    about        TEXT         NULL,
    days_of_week INT[]        NULL,
    start_time   VARCHAR(5)   NULL,
    end_time     VARCHAR(5)   NULL
);

CREATE TABLE IF NOT EXISTS master_skills
(
    master_id UUID NOT NULL REFERENCES users ON DELETE CASCADE,
    skill_id  INT  NOT NULL REFERENCES skills ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS categories
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(55) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tags
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(55) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS jobs
(
    id          UUID PRIMARY KEY,
    master_id   UUID                     NOT NULL REFERENCES users ON DELETE CASCADE,
    name        VARCHAR(100)             NOT NULL,
    description TEXT                     NOT NULL,
    price       DECIMAL(8, 2)            NOT NULL CHECK (price > 0),
    cover_url   TEXT                     NULL,
    category_id INT                      NOT NULL REFERENCES categories ON DELETE RESTRICT,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS job_tags
(
    job_id UUID NOT NULL REFERENCES jobs ON DELETE CASCADE,
    tag_id INT  NOT NULL REFERENCES tags ON DELETE RESTRICT,
    PRIMARY KEY (job_id, tag_id)
);

CREATE TABLE IF NOT EXISTS favourites
(
    user_id    UUID      NOT NULL REFERENCES users ON DELETE CASCADE,
    job_id     UUID      NOT NULL REFERENCES jobs ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, job_id)
);

CREATE TABLE IF NOT EXISTS orders
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           UUID                     NOT NULL REFERENCES users ON DELETE CASCADE,
    job_id            UUID                     NOT NULL REFERENCES jobs ON DELETE CASCADE,
    status            VARCHAR(15)              NOT NULL,
    ordered_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    status_changed_at TIMESTAMP WITH TIME ZONE NULL
);

CREATE TABLE IF NOT EXISTS chats
(
    order_id    BIGSERIAL PRIMARY KEY REFERENCES orders (id) ON DELETE CASCADE,
    master_id   UUID        NOT NULL REFERENCES users (id),
    customer_id UUID        NOT NULL REFERENCES users (id),
    status      chat_status NOT NULL DEFAULT 'OPEN'
);

CREATE TABLE IF NOT EXISTS messages
(
    id      UUID PRIMARY KEY,
    chat_id BIGSERIAL                NOT NULL REFERENCES chats (order_id) ON DELETE CASCADE,
    sender  UUID                     NOT NULL REFERENCES users (id),
    content TEXT                     NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- чаты, где пользователь мастер
CREATE INDEX IF NOT EXISTS idx_chats_master_status
    ON chats (master_id, status);

-- чаты, где пользователь кастомер
CREATE INDEX IF NOT EXISTS idx_chats_customer_status
    ON chats (customer_id, status);

-- сообщения по чату с сортировкой времени по убыванию
CREATE INDEX IF NOT EXISTS idx_messages_chat_sent_at
    ON messages (chat_id, sent_at DESC);
