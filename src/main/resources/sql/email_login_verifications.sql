CREATE TABLE email_login_verifications (
    email_login_verification_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    verification_code VARCHAR(10) NOT NULL,
    verified BIT NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (email_login_verification_id)
);

CREATE INDEX idx_email_login_verifications_email
    ON email_login_verifications (email);
