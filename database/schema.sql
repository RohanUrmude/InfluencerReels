-- ViralForge AI - PostgreSQL Database Schema
-- Complete schema with all tables, indexes, and relationships

-- Create Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    profile_picture_url VARCHAR(500),
    bio TEXT,
    niche VARCHAR(100),
    target_audience VARCHAR(100),
    preferred_platform VARCHAR(50),
    api_usage_count INTEGER DEFAULT 0,
    max_monthly_api_calls INTEGER DEFAULT 1000,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Content Requests Table
CREATE TABLE content_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    niche VARCHAR(100) NOT NULL,
    target_audience VARCHAR(100) NOT NULL,
    vibe VARCHAR(100),
    platform VARCHAR(50) NOT NULL,
    topic_idea TEXT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    creator_goal TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Generated Content Table
CREATE TABLE generated_content (
    id BIGSERIAL PRIMARY KEY,
    content_request_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    audience_analysis JSONB,
    script_content TEXT,
    hashtags TEXT[],
    thumbnail_text VARCHAR(200),
    captions TEXT,
    seo_hashtags TEXT[],
    posting_schedule VARCHAR(500),
    engagement_strategy TEXT,
    growth_tips TEXT,
    best_posting_time VARCHAR(50),
    platform_optimization TEXT,
    viral_score DECIMAL(5,2),
    confidence_score DECIMAL(5,2),
    primary_model_used VARCHAR(100),
    fallback_model_used VARCHAR(100),
    audience_type VARCHAR(100),
    recommended_tone VARCHAR(100),
    content_style VARCHAR(100),
    engagement_triggers TEXT[],
    trend_alignment VARCHAR(100),
    viral_hooks TEXT[],
    recommended_cta VARCHAR(200),
    generation_latency_ms INTEGER,
    is_favorited BOOLEAN DEFAULT false,
    is_published BOOLEAN DEFAULT false,
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (content_request_id) REFERENCES content_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- AI Usage Logs Table
CREATE TABLE ai_usage_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content_request_id BIGINT,
    model_name VARCHAR(100) NOT NULL,
    model_type VARCHAR(50),
    input_tokens INTEGER,
    output_tokens INTEGER,
    total_tokens INTEGER,
    latency_ms INTEGER,
    cost_estimate DECIMAL(10,6),
    status VARCHAR(50),
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    fallback_used BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (content_request_id) REFERENCES content_requests(id) ON DELETE SET NULL
);

-- Model Performance Logs Table
CREATE TABLE model_performance_logs (
    id BIGSERIAL PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL UNIQUE,
    request_count INTEGER DEFAULT 0,
    success_count INTEGER DEFAULT 0,
    failure_count INTEGER DEFAULT 0,
    average_latency_ms INTEGER,
    average_tokens_used INTEGER,
    last_used TIMESTAMP,
    reliability_score DECIMAL(5,2),
    is_healthy BOOLEAN DEFAULT true,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Analytics Table (User Dashboard Data)
CREATE TABLE analytics_snapshots (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_content_generated INTEGER DEFAULT 0,
    average_viral_score DECIMAL(5,2),
    average_confidence_score DECIMAL(5,2),
    total_api_calls INTEGER DEFAULT 0,
    favorite_content_count INTEGER DEFAULT 0,
    published_content_count INTEGER DEFAULT 0,
    most_used_model VARCHAR(100),
    most_successful_platform VARCHAR(50),
    monthly_usage_percentage DECIMAL(5,2),
    snapshot_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create Indexes (After table creation)
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_content_requests_user_id ON content_requests(user_id);
CREATE INDEX idx_content_requests_status ON content_requests(status);
CREATE INDEX idx_content_requests_created_at ON content_requests(created_at);
CREATE INDEX idx_content_requests_user_platform ON content_requests(user_id, platform);
CREATE INDEX idx_generated_content_user_id ON generated_content(user_id);
CREATE INDEX idx_generated_content_is_favorited ON generated_content(is_favorited);
CREATE INDEX idx_generated_content_is_published ON generated_content(is_published);
CREATE INDEX idx_generated_content_created_at ON generated_content(created_at DESC);
CREATE INDEX idx_generated_content_user_date ON generated_content(user_id, created_at DESC);
CREATE INDEX idx_ai_usage_logs_user_id ON ai_usage_logs(user_id);
CREATE INDEX idx_ai_usage_logs_model_name ON ai_usage_logs(model_name);
CREATE INDEX idx_ai_usage_logs_status ON ai_usage_logs(status);
CREATE INDEX idx_ai_usage_logs_created_at ON ai_usage_logs(created_at);
CREATE INDEX idx_ai_usage_logs_user_date ON ai_usage_logs(user_id, created_at DESC);
CREATE INDEX idx_model_performance_is_healthy ON model_performance_logs(is_healthy);
CREATE INDEX idx_model_performance_health ON model_performance_logs(is_healthy, average_latency_ms);
CREATE INDEX idx_analytics_snapshots_user_id ON analytics_snapshots(user_id);
CREATE INDEX idx_analytics_snapshots_date ON analytics_snapshots(snapshot_date);

-- Create Updated Timestamp Trigger Function
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply Triggers
CREATE TRIGGER update_users_timestamp BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_content_requests_timestamp BEFORE UPDATE ON content_requests
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- Comments for documentation
COMMENT ON TABLE users IS 'Stores creator user information and preferences';
COMMENT ON TABLE content_requests IS 'Tracks all content generation requests from users';
COMMENT ON TABLE generated_content IS 'Stores AI-generated content and analysis results';
COMMENT ON TABLE ai_usage_logs IS 'Logs all AI API calls for monitoring and billing';
COMMENT ON TABLE model_performance_logs IS 'Tracks performance metrics for each AI model';
COMMENT ON TABLE analytics_snapshots IS 'Stores user analytics data for dashboard display';
