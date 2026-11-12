ALTER TABLE User
    ADD CONSTRAINT UserEmailUniqueKey UNIQUE (email);

CREATE INDEX UserIndexByEmail ON User (email);

CREATE TABLE PasswordResetToken (
    id BIGINT NOT NULL AUTO_INCREMENT,
    userId BIGINT NOT NULL,
    tokenHash VARCHAR(64) NOT NULL,
    expiresAt TIMESTAMP NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usedAt TIMESTAMP NULL,
    CONSTRAINT PasswordResetTokenPK PRIMARY KEY (id),
    CONSTRAINT PasswordResetTokenHashUniqueKey UNIQUE (tokenHash),
    CONSTRAINT FK_PasswordResetToken_User FOREIGN KEY (userId) REFERENCES User(id)
) ENGINE = InnoDB;

CREATE INDEX PasswordResetTokenIndexByUser ON PasswordResetToken (userId);
