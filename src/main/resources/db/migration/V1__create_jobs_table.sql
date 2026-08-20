CREATE TABLE jobs (
                      id BIGSERIAL PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      url VARCHAR(500) NOT NULL UNIQUE,
                      scraper_name VARCHAR(50) NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);