BEGIN;
-- Create Role table
CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT valid_role_name CHECK (name IN ('ADMIN', 'ANALYST', 'OPERATOR'))
);

-- Create UserRole table
CREATE TABLE user_role (
	user_id INT,
    role_id INT,
    section_id INT,
    PRIMARY KEY (user_id, role_id, section_id),
    FOREIGN KEY (user_id) REFERENCES "users"(id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (section_id) REFERENCES section(id)
);

INSERT INTO audit_script (run_date, process, version, schema)
VALUES
    (CURRENT_DATE, '0004_role_user_role.sql', '1.0.1', '1.0.1_0004');

COMMIT;