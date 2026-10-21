CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    employee_code VARCHAR(50) NOT NULL UNIQUE,

    full_name VARCHAR(150) NOT NULL,

    email VARCHAR(150) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL,

    role VARCHAR(30) NOT NULL,

    manager_id BIGINT,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_role
        CHECK (role IN ('ADMIN','MANAGER','EMPLOYEE')),

    CONSTRAINT fk_user_manager
        FOREIGN KEY (manager_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_users_email
ON users(email);

CREATE INDEX idx_users_role
ON users(role);

CREATE TABLE leave_balance (

    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    annual_balance INTEGER NOT NULL DEFAULT 24,

    used_leave INTEGER NOT NULL DEFAULT 0,

    remaining_leave INTEGER NOT NULL DEFAULT 24,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_leave_balance_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE leave_requests (

    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    leave_type VARCHAR(30) NOT NULL,

    start_date DATE NOT NULL,

    end_date DATE NOT NULL,

    total_days INTEGER NOT NULL,

    reason TEXT NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    approved_by BIGINT,

    manager_comments TEXT,

    action_date TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_leave_status
        CHECK (status IN ('PENDING','APPROVED','REJECTED')),

    CONSTRAINT chk_leave_type
        CHECK (leave_type IN ('CASUAL','MEDICAL','EARNED')),

    CONSTRAINT chk_leave_dates
        CHECK (end_date >= start_date),

    CONSTRAINT fk_leave_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_leave_approver
        FOREIGN KEY(approved_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_leave_user
ON leave_requests(user_id);

CREATE INDEX idx_leave_status
ON leave_requests(status);

CREATE TABLE audit_log (

    id BIGSERIAL PRIMARY KEY,

    action_type VARCHAR(100) NOT NULL,

    action_by BIGINT,

    entity_name VARCHAR(100),

    entity_id BIGINT,

    action_details TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user
        FOREIGN KEY(action_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_audit_created
ON audit_log(created_at);



SELECT setval('users_id_seq', 100, true);
SELECT setval('leave_balance_id_seq', 100, true);
SELECT setval('leave_requests_id_seq', 100, true);
SELECT setval('audit_log_id_seq', 100, true);