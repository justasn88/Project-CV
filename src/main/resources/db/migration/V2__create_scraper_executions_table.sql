CREATE TABLE scraper_executions (
                                    id BIGSERIAL PRIMARY KEY,
                                    scraper_name VARCHAR(50) NOT NULL,
                                    status VARCHAR(20) NOT NULL,
                                    jobs_found INT DEFAULT 0,
                                    error_message TEXT,
                                    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);