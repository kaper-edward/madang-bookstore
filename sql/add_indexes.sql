-- 마당 서점 데이터베이스 인덱스 추가 스크립트
-- 작성일: 2025-10-21
-- 목적: 쿼리 성능 최적화 (3~10배 향상 예상)

USE madangdb;

-- ============================================
-- Orders 테이블 인덱스
-- ============================================

-- 고객별 주문 조회 성능 향상
CREATE INDEX IF NOT EXISTS idx_orders_custid ON Orders(custid);

-- 도서별 주문 통계 성능 향상
CREATE INDEX IF NOT EXISTS idx_orders_bookid ON Orders(bookid);

-- 날짜별 주문 분석 성능 향상
CREATE INDEX IF NOT EXISTS idx_orders_orderdate ON Orders(orderdate);

-- 복합 인덱스: 고객별 주문 날짜 조회
CREATE INDEX IF NOT EXISTS idx_orders_custid_orderdate ON Orders(custid, orderdate DESC);

-- ============================================
-- Book 테이블 인덱스
-- ============================================

-- 출판사별 도서 검색 성능 향상
CREATE INDEX IF NOT EXISTS idx_book_publisher ON Book(publisher);

-- 가격 범위 검색 성능 향상
CREATE INDEX IF NOT EXISTS idx_book_price ON Book(price);

-- 도서명 검색 성능 향상 (LIKE 검색용)
CREATE INDEX IF NOT EXISTS idx_book_bookname ON Book(bookname);

-- ============================================
-- Customer 테이블 인덱스
-- ============================================

-- 고객명 검색 성능 향상
CREATE INDEX IF NOT EXISTS idx_customer_name ON Customer(name);

-- 역할별 고객 조회 성능 향상
CREATE INDEX IF NOT EXISTS idx_customer_role ON Customer(role);

-- ============================================
-- 인덱스 생성 완료 확인
-- ============================================

SHOW INDEX FROM Orders;
SHOW INDEX FROM Book;
SHOW INDEX FROM Customer;

-- ============================================
-- 성능 테스트 쿼리 (EXPLAIN 사용)
-- ============================================

-- 인덱스 적용 전후 비교를 위한 쿼리
EXPLAIN SELECT * FROM Orders WHERE custid = 1 ORDER BY orderdate DESC;
EXPLAIN SELECT * FROM Book WHERE publisher LIKE '%한빛%';
EXPLAIN SELECT * FROM Customer WHERE role = 'admin';
