CREATE EXTENSION IF NOT EXISTS "vector";

CREATE TABLE "document_knowledge" (
  "id" character varying(100) NOT NULL,
  "title" text,
  "content" text,
  "vector" vector(1024),
  "metadata" jsonb NOT NULL DEFAULT '{}'::jsonb,
  PRIMARY KEY ("id")
);
CREATE INDEX "document_knowledge_vector_idx" ON "document_knowledge" USING hnsw ("vector" vector_cosine_ops);

