-- 프로젝트 최초 실행 전 이 파일을 PostgreSQL 에서 직접 실행해주세요.

-- 데이터베이스 생성
CREATE DATABASE world_tale_talk;

-- vector 익스텐션 설치 (pgvector - Spring AI RAG 사용)
-- world_tale_talk 데이터베이스에 접속 후 실행해주세요.
CREATE EXTENSION IF NOT EXISTS vector;
