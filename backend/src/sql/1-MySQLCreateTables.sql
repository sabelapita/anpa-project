DROP TABLE IF EXISTS Enrollment;
DROP TABLE IF EXISTS Activity;
DROP TABLE IF EXISTS PostAttachment;
DROP TABLE IF EXISTS Post;
DROP TABLE IF EXISTS Document;
DROP TABLE IF EXISTS Message;
DROP TABLE IF EXISTS Event;
DROP TABLE IF EXISTS User;

CREATE TABLE User (
    id BIGINT NOT NULL AUTO_INCREMENT,
    userName VARCHAR(60) COLLATE latin1_bin NOT NULL,
    password VARCHAR(60) NOT NULL, 
    firstName VARCHAR(60) NOT NULL,
    lastName VARCHAR(60) NOT NULL, 
    email VARCHAR(60) NOT NULL,
    role TINYINT NOT NULL,
    membershipStatus VARCHAR(30) NOT NULL DEFAULT 'NON_MEMBER',
    CONSTRAINT UserPK PRIMARY KEY (id),
    CONSTRAINT UserNameUniqueKey UNIQUE (userName)
) ENGINE = InnoDB;

CREATE INDEX UserIndexByUserName ON User (userName);

CREATE TABLE Activity (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    price DOUBLE,
    imageUrl VARCHAR(255),
    type VARCHAR(50) NOT NULL, -- ACTIVITY o EVENT
    visibility VARCHAR(30) NOT NULL DEFAULT 'PUBLIC',
    createdBy_id BIGINT,
    CONSTRAINT ActivityPK PRIMARY KEY (id),
    CONSTRAINT FK_Activity_User FOREIGN KEY (createdBy_id) REFERENCES User(id)
) ENGINE = InnoDB;

CREATE INDEX ActivityIndexByCreator ON Activity (createdBy_id);

CREATE TABLE Enrollment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    userId BIGINT NOT NULL,
    activityId BIGINT NOT NULL,

    paid BOOLEAN DEFAULT FALSE,
    paymentDate DATE,

    paymentStatus VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    amountCents INT,
    stripeSessionId VARCHAR(255),
    stripePaymentIntentId VARCHAR(255),

    CONSTRAINT EnrollmentPK PRIMARY KEY (id),
    FOREIGN KEY (userId) REFERENCES User(id),
    FOREIGN KEY (activityId) REFERENCES Activity(id)
) ENGINE = InnoDB;

CREATE INDEX EnrollmentIndexByUser ON Enrollment (userId);
CREATE INDEX EnrollmentIndexByActivity ON Enrollment (activityId);
CREATE TABLE Post (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50),
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NULL,
    published BOOLEAN NOT NULL DEFAULT TRUE,
    createdBy BIGINT NOT NULL,
    CONSTRAINT PostPK PRIMARY KEY (id),
    CONSTRAINT FK_Post_User FOREIGN KEY (createdBy) REFERENCES User(id)
) ENGINE = InnoDB;

CREATE INDEX PostIndexByAuthor ON Post (createdBy);

CREATE TABLE PostAttachment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    postId BIGINT NOT NULL,
    fileName VARCHAR(255) NOT NULL,
    filePath VARCHAR(255) NOT NULL,
    contentType VARCHAR(100) NOT NULL,
    CONSTRAINT PostAttachmentPK PRIMARY KEY (id),
    CONSTRAINT FK_PostAttachment_Post FOREIGN KEY (postId) REFERENCES Post(id)
) ENGINE = InnoDB;

CREATE INDEX PostAttachmentIndexByPost ON PostAttachment (postId);

CREATE TABLE Document (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    fileName VARCHAR(255) NOT NULL,
    filePath VARCHAR(255) NOT NULL,
    contentType VARCHAR(100),
    uploadedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uploadedBy BIGINT NOT NULL,
    CONSTRAINT DocumentPK PRIMARY KEY (id),
    CONSTRAINT FK_Document_User FOREIGN KEY (uploadedBy) REFERENCES User(id)
) ENGINE = InnoDB;

CREATE TABLE Message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    userId BIGINT NOT NULL,
    subject VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    sentAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response TEXT,
    respondedAt TIMESTAMP,
    CONSTRAINT MessagePK PRIMARY KEY (id),
    CONSTRAINT FK_Message_User FOREIGN KEY (userId) REFERENCES User(id)
) ENGINE = InnoDB;

CREATE INDEX MessageIndexByUser ON Message (userId);
