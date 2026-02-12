-- 智能客服 Markdown 文件存储表
CREATE TABLE t_ai_customer_service_md_storage (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  original_file_name VARCHAR(160) NOT NULL,
                                                  new_file_name VARCHAR(160) NOT NULL,
                                                  file_path VARCHAR(500) NOT NULL,
                                                  file_size BIGINT NOT NULL,
                                                  status SMALLINT DEFAULT 0,
                                                  remark VARCHAR(200),
                                                  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加表注释
COMMENT ON TABLE t_ai_customer_service_md_storage IS '问答 Markdown 文件存储表';

-- 添加字段注释
COMMENT ON COLUMN t_ai_customer_service_md_storage.id IS '主键ID';
COMMENT ON COLUMN t_ai_customer_service_md_storage.original_file_name IS '原始文件名称';
COMMENT ON COLUMN t_ai_customer_service_md_storage.new_file_name IS '新命名文件名称（防止名称相同导致覆盖）';
COMMENT ON COLUMN t_ai_customer_service_md_storage.file_path IS '文件存储路径';
COMMENT ON COLUMN t_ai_customer_service_md_storage.file_size IS '文件大小(字节)';
COMMENT ON COLUMN t_ai_customer_service_md_storage.status IS '处理状态：0-待处理 1-向量化中 2-已完成 3-失败';
COMMENT ON COLUMN t_ai_customer_service_md_storage.remark IS '备注信息';
COMMENT ON COLUMN t_ai_customer_service_md_storage.create_time IS '创建时间';
COMMENT ON COLUMN t_ai_customer_service_md_storage.update_time IS '更新时间';

-- 创建索引
CREATE INDEX idx_t_ai_customer_service_md_storage_status ON t_ai_customer_service_md_storage(status);
CREATE INDEX idx_t_ai_customer_service_md_storage_created_time ON t_ai_customer_service_md_storage(create_time);
CREATE INDEX idx_t_ai_customer_service_md_storage_original_file_name ON t_ai_customer_service_md_storage(original_file_name);


-- 启用扩展
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS t_vector_store (
                                              id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1536)
    );

-- 创建索引
CREATE INDEX ON t_vector_store USING HNSW (embedding vector_cosine_ops);

