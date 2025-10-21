-- ============================================================================
-- 마당 온라인 서점 - CRUD SQL 쿼리 모음
-- 학습 목적: 기본적인 CRUD 연산 및 JOIN, GROUP BY, 집계 함수 연습
-- ============================================================================

-- ============================================================================
-- 1. CREATE (INSERT) - 데이터 생성
-- ============================================================================

-- 1.1 주문 생성 (가장 중요한 INSERT 쿼리)
-- 새로운 orderid는 기존 최대값 + 1로 자동 생성
INSERT INTO Orders (orderid, custid, bookid, saleprice, orderdate)
VALUES (
    (SELECT IFNULL(MAX(orderid), 0) + 1 FROM Orders AS o),
    1,          -- 고객 ID
    1,          -- 도서 ID
    6000,       -- 판매가
    CURDATE()   -- 오늘 날짜
);

-- 1.2 특정 날짜로 주문 생성
INSERT INTO Orders (orderid, custid, bookid, saleprice, orderdate)
VALUES (
    (SELECT IFNULL(MAX(orderid), 0) + 1 FROM Orders AS o),
    2,
    3,
    20000,
    '2025-10-02'
);


-- ============================================================================
-- 2. READ (SELECT) - 데이터 조회
-- ============================================================================

-- ────────────────────────────────────────────────────────────────────────────
-- 2.1 Book 테이블 조회
-- ────────────────────────────────────────────────────────────────────────────

-- 2.1.1 전체 도서 조회
SELECT bookid, bookname, publisher, price
FROM Book
ORDER BY bookid;

-- 2.1.2 특정 도서 상세 조회
SELECT bookid, bookname, publisher, price
FROM Book
WHERE bookid = 1;

-- 2.1.3 도서명으로 검색 (LIKE 사용)
SELECT bookid, bookname, publisher, price
FROM Book
WHERE bookname LIKE '%축구%'
ORDER BY bookid;

-- 2.1.4 출판사별 도서 조회
SELECT bookid, bookname, publisher, price
FROM Book
WHERE publisher = '굿스포츠'
ORDER BY bookid;

-- 2.1.5 가격 범위로 도서 검색
SELECT bookid, bookname, publisher, price
FROM Book
WHERE price BETWEEN 10000 AND 20000
ORDER BY price;

-- 2.1.6 출판사 목록 조회 (중복 제거)
SELECT DISTINCT publisher
FROM Book
ORDER BY publisher;

-- 2.1.7 최신 도서 N권 조회
SELECT bookid, bookname, publisher, price
FROM Book
ORDER BY bookid DESC
LIMIT 3;

-- 2.1.8 가격이 높은 순으로 도서 조회
SELECT bookid, bookname, publisher, price
FROM Book
ORDER BY price DESC;


-- ────────────────────────────────────────────────────────────────────────────
-- 2.2 Customer 테이블 조회
-- ────────────────────────────────────────────────────────────────────────────

-- 2.2.1 전체 고객 조회
SELECT custid, name, address, phone, role
FROM Customer
ORDER BY custid;

-- 2.2.2 특정 고객 상세 조회
SELECT custid, name, address, phone, role
FROM Customer
WHERE custid = 1;

-- 2.2.3 고객명으로 검색
SELECT custid, name, address, phone, role
FROM Customer
WHERE name LIKE '%박%'
ORDER BY name;

-- 2.2.4 지역별 고객 조회
SELECT custid, name, address, phone, role
FROM Customer
WHERE address LIKE '%서울%'
ORDER BY name;


-- ────────────────────────────────────────────────────────────────────────────
-- 2.3 Orders 테이블 조회
-- ────────────────────────────────────────────────────────────────────────────

-- 2.3.1 전체 주문 조회
SELECT orderid, custid, bookid, saleprice, orderdate
FROM Orders
ORDER BY orderdate DESC;

-- 2.3.2 특정 고객의 주문 내역
SELECT orderid, custid, bookid, saleprice, orderdate
FROM Orders
WHERE custid = 1
ORDER BY orderdate DESC;

-- 2.3.3 특정 도서의 주문 내역
SELECT orderid, custid, bookid, saleprice, orderdate
FROM Orders
WHERE bookid = 1
ORDER BY orderdate DESC;

-- 2.3.4 날짜 범위로 주문 조회
SELECT orderid, custid, bookid, saleprice, orderdate
FROM Orders
WHERE orderdate BETWEEN '2024-07-01' AND '2024-07-31'
ORDER BY orderdate DESC;

-- 2.3.5 금액 범위로 주문 조회
SELECT orderid, custid, bookid, saleprice, orderdate
FROM Orders
WHERE saleprice >= 10000
ORDER BY saleprice DESC;


-- ────────────────────────────────────────────────────────────────────────────
-- 2.4 JOIN 쿼리 (여러 테이블 결합)
-- ────────────────────────────────────────────────────────────────────────────

-- 2.4.1 고객의 주문 내역 (고객명, 도서명 포함)
SELECT o.orderid,
       c.name AS customer_name,
       b.bookname,
       b.publisher,
       o.saleprice,
       o.orderdate
FROM Orders o
JOIN Customer c ON o.custid = c.custid
JOIN Book b ON o.bookid = b.bookid
ORDER BY o.orderdate DESC;

-- 2.4.2 특정 고객의 주문 내역 (JOIN 버전)
SELECT o.orderid,
       o.saleprice,
       o.orderdate,
       b.bookname,
       b.publisher,
       b.price
FROM Orders o
JOIN Book b ON o.bookid = b.bookid
WHERE o.custid = 1
ORDER BY o.orderdate DESC;

-- 2.4.3 특정 도서를 구매한 고객 목록
SELECT DISTINCT c.custid,
       c.name,
       c.address,
       c.phone
FROM Customer c
JOIN Orders o ON c.custid = o.custid
WHERE o.bookid = 1;

-- 2.4.4 모든 고객과 그들의 주문 (LEFT JOIN - 주문 없는 고객도 포함)
SELECT c.custid,
       c.name,
       COUNT(o.orderid) AS order_count,
       SUM(o.saleprice) AS total_amount
FROM Customer c
LEFT JOIN Orders o ON c.custid = o.custid
GROUP BY c.custid, c.name
ORDER BY total_amount DESC;

-- 2.4.5 모든 도서와 판매 현황 (LEFT JOIN - 판매되지 않은 도서도 포함)
SELECT b.bookid,
       b.bookname,
       b.publisher,
       b.price,
       COUNT(o.orderid) AS sales_count,
       IFNULL(SUM(o.saleprice), 0) AS total_revenue,
       IFNULL(AVG(o.saleprice), 0) AS avg_sale_price
FROM Book b
LEFT JOIN Orders o ON b.bookid = o.bookid
GROUP BY b.bookid, b.bookname, b.publisher, b.price
ORDER BY sales_count DESC;


-- ────────────────────────────────────────────────────────────────────────────
-- 2.5 집계 함수 (COUNT, SUM, AVG, MAX, MIN)
-- ────────────────────────────────────────────────────────────────────────────

-- 2.5.1 전체 통계 (한 번에 여러 집계)
SELECT
    (SELECT COUNT(*) FROM Book) AS total_books,
    (SELECT COUNT(*) FROM Customer) AS total_customers,
    (SELECT COUNT(*) FROM Orders) AS total_orders,
    (SELECT SUM(saleprice) FROM Orders) AS total_revenue,
    (SELECT AVG(saleprice) FROM Orders) AS avg_sale_price;

-- 2.5.2 도서 수 조회
SELECT COUNT(*) AS book_count
FROM Book;

-- 2.5.3 특정 출판사의 도서 수
SELECT COUNT(*) AS book_count
FROM Book
WHERE publisher = '굿스포츠';

-- 2.5.4 총 주문 수와 총 매출
SELECT COUNT(*) AS total_orders,
       SUM(saleprice) AS total_revenue
FROM Orders;

-- 2.5.5 평균, 최대, 최소 판매가
SELECT AVG(saleprice) AS avg_price,
       MAX(saleprice) AS max_price,
       MIN(saleprice) AS min_price
FROM Orders;

-- 2.5.6 특정 도서의 판매 통계
SELECT COUNT(*) AS sales_count,
       AVG(saleprice) AS avg_sale_price,
       MAX(saleprice) AS max_sale_price,
       MIN(saleprice) AS min_sale_price
FROM Orders
WHERE bookid = 1;

-- 2.5.7 특정 고객의 주문 통계
SELECT COUNT(*) AS order_count,
       SUM(saleprice) AS total_amount,
       AVG(saleprice) AS avg_amount
FROM Orders
WHERE custid = 1;


-- ────────────────────────────────────────────────────────────────────────────
-- 2.6 GROUP BY (그룹화)
-- ────────────────────────────────────────────────────────────────────────────

-- 2.6.1 고객별 주문 수 및 총 구매액
SELECT c.custid,
       c.name,
       COUNT(o.orderid) AS order_count,
       IFNULL(SUM(o.saleprice), 0) AS total_amount,
       IFNULL(AVG(o.saleprice), 0) AS avg_amount
FROM Customer c
LEFT JOIN Orders o ON c.custid = o.custid
GROUP BY c.custid, c.name
ORDER BY total_amount DESC;

-- 2.6.2 출판사별 도서 수
SELECT publisher,
       COUNT(*) AS book_count
FROM Book
GROUP BY publisher
ORDER BY book_count DESC;

-- 2.6.3 출판사별 판매 통계
SELECT b.publisher,
       COUNT(DISTINCT b.bookid) AS book_count,
       COUNT(o.orderid) AS sales_count,
       IFNULL(SUM(o.saleprice), 0) AS total_revenue,
       IFNULL(AVG(o.saleprice), 0) AS avg_sale_price
FROM Book b
LEFT JOIN Orders o ON b.bookid = o.bookid
GROUP BY b.publisher
ORDER BY total_revenue DESC;

-- 2.6.4 도서별 판매 수량 (베스트셀러)
SELECT b.bookid,
       b.bookname,
       b.publisher,
       COUNT(o.orderid) AS sales_count
FROM Book b
LEFT JOIN Orders o ON b.bookid = o.bookid
GROUP BY b.bookid, b.bookname, b.publisher
ORDER BY sales_count DESC;

-- 2.6.5 날짜별 주문 수 및 매출
SELECT orderdate,
       COUNT(*) AS order_count,
       SUM(saleprice) AS daily_revenue
FROM Orders
GROUP BY orderdate
ORDER BY orderdate DESC;

-- 2.6.6 월별 매출 통계
SELECT DATE_FORMAT(orderdate, '%Y-%m') AS month,
       COUNT(*) AS order_count,
       SUM(saleprice) AS monthly_revenue
FROM Orders
GROUP BY DATE_FORMAT(orderdate, '%Y-%m')
ORDER BY month DESC;


-- ────────────────────────────────────────────────────────────────────────────
-- 2.7 HAVING (그룹화 후 조건 필터링)
-- ────────────────────────────────────────────────────────────────────────────

-- 2.7.1 2건 이상 판매된 도서만 조회
SELECT b.bookname,
       COUNT(o.orderid) AS sales_count
FROM Book b
JOIN Orders o ON b.bookid = o.bookid
GROUP BY b.bookid, b.bookname
HAVING COUNT(o.orderid) >= 2
ORDER BY sales_count DESC;

-- 2.7.2 총 구매액이 10,000원 이상인 고객만 조회
SELECT c.name,
       SUM(o.saleprice) AS total_amount
FROM Customer c
JOIN Orders o ON c.custid = o.custid
GROUP BY c.custid, c.name
HAVING SUM(o.saleprice) >= 10000
ORDER BY total_amount DESC;

-- 2.7.3 평균 판매가가 정가보다 낮은 도서 조회
SELECT b.bookname,
       b.price,
       AVG(o.saleprice) AS avg_sale_price
FROM Book b
JOIN Orders o ON b.bookid = o.bookid
GROUP BY b.bookid, b.bookname, b.price
HAVING AVG(o.saleprice) < b.price;


-- ────────────────────────────────────────────────────────────────────────────
-- 2.8 서브쿼리 (Subquery)
-- ────────────────────────────────────────────────────────────────────────────

-- 2.8.1 가장 비싼 도서 조회
SELECT bookid, bookname, publisher, price
FROM Book
WHERE price = (SELECT MAX(price) FROM Book);

-- 2.8.2 평균 가격보다 비싼 도서 조회
SELECT bookid, bookname, publisher, price
FROM Book
WHERE price > (SELECT AVG(price) FROM Book)
ORDER BY price DESC;

-- 2.8.3 한 번도 주문하지 않은 고객 조회
SELECT custid, name, address, phone
FROM Customer
WHERE custid NOT IN (SELECT DISTINCT custid FROM Orders);

-- 2.8.4 한 번도 판매되지 않은 도서 조회
SELECT bookid, bookname, publisher, price
FROM Book
WHERE bookid NOT IN (SELECT DISTINCT bookid FROM Orders);

-- 2.8.5 가장 많이 주문한 고객
SELECT c.custid, c.name, COUNT(o.orderid) AS order_count
FROM Customer c
JOIN Orders o ON c.custid = o.custid
GROUP BY c.custid, c.name
HAVING COUNT(o.orderid) = (
    SELECT MAX(cnt) FROM (
        SELECT COUNT(*) AS cnt
        FROM Orders
        GROUP BY custid
    ) AS counts
);


-- ────────────────────────────────────────────────────────────────────────────
-- 2.9 대시보드용 통계 쿼리
-- ────────────────────────────────────────────────────────────────────────────

-- 2.9.1 베스트셀러 TOP 5
SELECT b.bookname,
       b.publisher,
       COUNT(*) AS sales_count
FROM Orders o
JOIN Book b ON o.bookid = b.bookid
GROUP BY b.bookid, b.bookname, b.publisher
ORDER BY sales_count DESC
LIMIT 5;

-- 2.9.2 최근 주문 5건
SELECT o.orderdate,
       c.name AS customer_name,
       b.bookname,
       o.saleprice
FROM Orders o
JOIN Customer c ON o.custid = c.custid
JOIN Book b ON o.bookid = b.bookid
ORDER BY o.orderdate DESC, o.orderid DESC
LIMIT 5;

-- 2.9.3 VIP 고객 (구매액 상위 3명)
SELECT c.name,
       COUNT(o.orderid) AS order_count,
       SUM(o.saleprice) AS total_amount
FROM Customer c
JOIN Orders o ON c.custid = o.custid
GROUP BY c.custid, c.name
ORDER BY total_amount DESC
LIMIT 3;

-- 2.9.4 출판사별 매출 순위
SELECT b.publisher,
       COUNT(o.orderid) AS sales_count,
       SUM(o.saleprice) AS total_revenue
FROM Book b
LEFT JOIN Orders o ON b.bookid = o.bookid
GROUP BY b.publisher
ORDER BY total_revenue DESC;


-- ============================================================================
-- 3. UPDATE - 데이터 수정 (학생들이 아직 배우지 않아 주석 처리)
-- ============================================================================

-- 3.1 도서 가격 수정
-- UPDATE Book
-- SET price = 8000
-- WHERE bookid = 1;

-- 3.2 고객 정보 수정
-- UPDATE Customer
-- SET address = '대한민국 부산', phone = '000-5000-0002'
-- WHERE custid = 1;

-- 3.3 주문 판매가 수정
-- UPDATE Orders
-- SET saleprice = 7000
-- WHERE orderid = 1 AND custid = 1;


-- ============================================================================
-- 4. DELETE - 데이터 삭제
-- ============================================================================

-- 4.1 주문 취소 (가장 중요한 DELETE 쿼리)
-- 보안: custid도 함께 확인하여 본인 주문만 삭제 가능
DELETE FROM Orders
WHERE orderid = 1 AND custid = 1;

-- 4.2 특정 고객의 모든 주문 삭제 (주의!)
-- DELETE FROM Orders
-- WHERE custid = 1;

-- 4.3 특정 날짜 이전의 주문 삭제
-- DELETE FROM Orders
-- WHERE orderdate < '2024-01-01';


-- ============================================================================
-- 5. 유용한 쿼리 패턴
-- ============================================================================

-- 5.1 다음 orderid 가져오기
SELECT IFNULL(MAX(orderid), 0) + 1 AS next_orderid
FROM Orders;

-- 5.2 현재 날짜 가져오기
SELECT CURDATE() AS today;

-- 5.3 데이터 존재 여부 확인
SELECT EXISTS(
    SELECT 1 FROM Orders
    WHERE orderid = 1 AND custid = 1
) AS order_exists;

-- 5.4 조건부 정렬 (가격 내림차순, 같으면 도서명 오름차순)
SELECT bookid, bookname, publisher, price
FROM Book
ORDER BY price DESC, bookname ASC;

-- 5.5 LIMIT과 OFFSET을 사용한 페이징
SELECT bookid, bookname, publisher, price
FROM Book
ORDER BY bookid
LIMIT 10 OFFSET 0;  -- 1페이지 (0~9)
-- LIMIT 10 OFFSET 10;  -- 2페이지 (10~19)


-- ============================================================================
-- 6. 학습용 연습 문제 (답안 포함)
-- ============================================================================

-- 문제 1: '축구'가 포함된 도서의 평균 가격은?
SELECT AVG(price) AS avg_price
FROM Book
WHERE bookname LIKE '%축구%';

-- 문제 2: 각 고객이 구매한 도서의 총 권수는?
SELECT c.name, COUNT(o.orderid) AS book_count
FROM Customer c
LEFT JOIN Orders o ON c.custid = o.custid
GROUP BY c.custid, c.name
ORDER BY book_count DESC;

-- 문제 3: 판매 실적이 가장 좋은 출판사는?
SELECT b.publisher, SUM(o.saleprice) AS total_revenue
FROM Book b
JOIN Orders o ON b.bookid = o.bookid
GROUP BY b.publisher
ORDER BY total_revenue DESC
LIMIT 1;

-- 문제 4: 2024년 7월에 판매된 도서 목록과 판매 건수는?
SELECT b.bookname, COUNT(*) AS sales_count
FROM Orders o
JOIN Book b ON o.bookid = b.bookid
WHERE o.orderdate BETWEEN '2024-07-01' AND '2024-07-31'
GROUP BY b.bookid, b.bookname
ORDER BY sales_count DESC;

-- 문제 5: 정가보다 1000원 이상 할인된 주문 내역은?
SELECT o.orderid,
       c.name AS customer_name,
       b.bookname,
       b.price,
       o.saleprice,
       (b.price - o.saleprice) AS discount
FROM Orders o
JOIN Customer c ON o.custid = c.custid
JOIN Book b ON o.bookid = b.bookid
WHERE (b.price - o.saleprice) >= 1000
ORDER BY discount DESC;


-- ============================================================================
-- 7. 데이터 검증 쿼리 (개발/디버깅용)
-- ============================================================================

-- 7.1 모든 테이블의 레코드 수 확인
SELECT 'Book' AS table_name, COUNT(*) AS row_count FROM Book
UNION ALL
SELECT 'Customer', COUNT(*) FROM Customer
UNION ALL
SELECT 'Orders', COUNT(*) FROM Orders;

-- 7.2 외래키 무결성 확인 (Orders → Customer)
SELECT o.orderid, o.custid
FROM Orders o
LEFT JOIN Customer c ON o.custid = c.custid
WHERE c.custid IS NULL;

-- 7.3 외래키 무결성 확인 (Orders → Book)
SELECT o.orderid, o.bookid
FROM Orders o
LEFT JOIN Book b ON o.bookid = b.bookid
WHERE b.bookid IS NULL;

-- 7.4 판매가가 정가를 초과하는 비정상 데이터 확인
SELECT o.orderid, b.bookname, b.price, o.saleprice
FROM Orders o
JOIN Book b ON o.bookid = b.bookid
WHERE o.saleprice > b.price;

-- 7.5 NULL 값 확인
SELECT 'Book' AS table_name,
       SUM(CASE WHEN bookname IS NULL THEN 1 ELSE 0 END) AS null_bookname,
       SUM(CASE WHEN publisher IS NULL THEN 1 ELSE 0 END) AS null_publisher,
       SUM(CASE WHEN price IS NULL THEN 1 ELSE 0 END) AS null_price
FROM Book;


-- ============================================================================
-- 끝 - madang_bookstore SQL 쿼리 모음
-- 학습 포인트:
-- 1. CRUD 기본 연산 (INSERT, SELECT, UPDATE, DELETE)
-- 2. JOIN을 사용한 다중 테이블 조회
-- 3. GROUP BY와 집계 함수 (COUNT, SUM, AVG, MAX, MIN)
-- 4. HAVING을 사용한 그룹 필터링
-- 5. 서브쿼리 활용
-- 6. 실무에서 자주 사용하는 패턴
-- ============================================================================
