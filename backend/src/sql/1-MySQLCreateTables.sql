DROP TABLE IF EXISTS User;

CREATE TABLE User (
    id BIGINT NOT NULL AUTO_INCREMENT,
    userName VARCHAR(60) COLLATE latin1_bin NOT NULL,
    password VARCHAR(60) NOT NULL, 
    firstName VARCHAR(60) NOT NULL,
    lastName VARCHAR(60) NOT NULL, 
    email VARCHAR(60) NOT NULL,
    role TINYINT NOT NULL,
    CONSTRAINT UserPK PRIMARY KEY (id),
    CONSTRAINT UserNameUniqueKey UNIQUE (userName)
) ENGINE = InnoDB;

CREATE INDEX UserIndexByUserName ON User (userName);

DROP TABLE IF EXISTS Activity;
CREATE TABLE Activity (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    isPublic BOOLEAN DEFAULT TRUE,
    CONSTRAINT ActivityPK PRIMARY KEY (id)
) ENGINE = InnoDB;

DROP TABLE IF EXISTS Enrollment;
CREATE TABLE Enrollment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    userId BIGINT NOT NULL,
    activityId BIGINT NOT NULL,
    paid BOOLEAN DEFAULT FALSE,
    paymentDate DATE,
    CONSTRAINT EnrollmentPK PRIMARY KEY (id),
    FOREIGN KEY (userId) REFERENCES User(id),
    FOREIGN KEY (activityId) REFERENCES Activity(id)
) ENGINE = InnoDB;

CREATE INDEX EnrollmentIndexByUser ON Enrollment (userId);
CREATE INDEX EnrollmentIndexByActivity ON Enrollment (activityId);

DROP TABLE IF EXISTS Post;
CREATE TABLE Post (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    category VARCHAR(50),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    createdBy BIGINT,
    CONSTRAINT PostPK PRIMARY KEY (id),
    FOREIGN KEY (createdBy) REFERENCES User(id)
) ENGINE = InnoDB;

CREATE INDEX PostIndexByAuthor ON Post (createdBy);

DROP TABLE IF EXISTS Document;
CREATE TABLE Document (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100),
    filePath VARCHAR(255),
    uploadedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT DocumentPK PRIMARY KEY (id)
) ENGINE = InnoDB;

DROP TABLE IF EXISTS Message;
CREATE TABLE Message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    userId BIGINT NOT NULL,
    subject VARCHAR(100),
    content TEXT NOT NULL,
    sentAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    response TEXT,
    respondedAt TIMESTAMP,
    CONSTRAINT MessagePK PRIMARY KEY (id),
    FOREIGN KEY (userId) REFERENCES User(id)
) ENGINE = InnoDB;

CREATE INDEX MessageIndexByUser ON Message (userId);
