# 마당 온라인 서점 - 웹페이지 디자인 문서

## 목차
1. [메인 페이지](#1-메인-페이지-indexhtml)
2. [고객 로그인](#2-고객-로그인-customer-loginhtml)
3. [도서 목록](#3-도서-목록-bookshtml)
4. [도서 상세](#4-도서-상세-book-detailhtml)
5. [주문하기](#5-주문하기-orderhtml)
6. [내 주문 내역](#6-내-주문-내역-my-ordershtml)
7. [관리자 대시보드](#7-관리자-대시보드-dashboardhtml)

---

## 1. 메인 페이지 (index.html)

### 목적
서점 소개 및 주요 기능 안내, 최신 도서 표시

### 와이어프레임
```
┌──────────────────────────────────────────────────────────┐
│  ┌────────┐  마당 온라인 서점              [로그인 버튼]  │
│  │  로고  │                              [대시보드 →]   │
└──────────────────────────────────────────────────────────┘
│                                                          │
│              환영합니다! 마당 온라인 서점입니다           │
│              최고의 도서를 만나보세요                     │
│                                                          │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │  📚 도서 목록     │  │  📋 내 주문 내역  │            │
│  │   전체 도서 보기  │  │   주문 확인하기   │            │
│  └──────────────────┘  └──────────────────┘            │
│                                                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                          │
│  최신 도서 📖                                            │
│                                                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │
│  │ 축구의 역사  │ │ 축구 아는    │ │ 축구의 이해  │       │
│  │             │ │   여자       │ │             │       │
│  │ 굿스포츠    │ │ 나무수       │ │ 대한미디어   │       │
│  │ 7,000원     │ │ 13,000원     │ │ 22,000원     │       │
│  │             │ │              │ │              │       │
│  │ [상세보기]  │ │ [상세보기]   │ │ [상세보기]   │       │
│  └─────────────┘ └─────────────┘ └─────────────┘       │
│                                                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                          │
│  관리자: [대시보드 바로가기 →]                           │
│                                                          │
│  푸터: ⓒ 2025 마당 서점. All rights reserved.           │
└──────────────────────────────────────────────────────────┘
```

### HTML 구조
```html
<header class="header">
  <div class="container">
    <div class="logo">마당 온라인 서점</div>
    <nav>
      <a href="customer-login.html" class="btn-primary">로그인</a>
      <a href="dashboard.html" class="btn-secondary">대시보드</a>
    </nav>
  </div>
</header>

<main class="container">
  <section class="hero">
    <h1>환영합니다! 마당 온라인 서점입니다</h1>
    <p>최고의 도서를 만나보세요</p>
  </section>

  <section class="quick-links">
    <div class="card">
      <h3>📚 도서 목록</h3>
      <a href="books.html">전체 도서 보기</a>
    </div>
    <div class="card">
      <h3>📋 내 주문 내역</h3>
      <a href="my-orders.html">주문 확인하기</a>
    </div>
  </section>

  <section class="latest-books">
    <h2>최신 도서 📖</h2>
    <div id="latest-books-grid" class="grid-3"></div>
  </section>
</main>

<footer class="footer">
  <p>ⓒ 2025 마당 서점. All rights reserved.</p>
</footer>
```

### CSS 클래스
```css
.header          /* 상단 네비게이션 */
.hero            /* 히어로 섹션 */
.quick-links     /* 빠른 링크 카드 */
.latest-books    /* 최신 도서 섹션 */
.grid-3          /* 3열 그리드 */
.card            /* 카드 컴포넌트 */
.btn-primary     /* 주요 버튼 */
.btn-secondary   /* 보조 버튼 */
```

### JavaScript 로직
```javascript
// 페이지 로드 시 실행
window.addEventListener('DOMContentLoaded', async () => {
  await loadLatestBooks();
  checkLoginStatus();
});

async function loadLatestBooks() {
  const response = await fetchAPI('/api/books?action=list');
  const books = response.data.slice(0, 3); // 최신 3권
  renderBookCards(books);
}

function checkLoginStatus() {
  const custid = localStorage.getItem('custid');
  if (custid) {
    // 로그인 버튼을 "내 정보"로 변경
  }
}
```

### API 호출
```
GET /api/books?action=list
→ 전체 도서 목록 조회 (최신 3권만 표시)
```

### SQL 쿼리
```sql
-- 최신 도서 3권 조회
SELECT bookid, bookname, publisher, price
FROM Book
ORDER BY bookid DESC
LIMIT 3;
```

---

## 2. 고객 로그인 (customer-login.html)

### 목적
기존 고객 선택하여 로그인 (세션 기반)

### 와이어프레임
```
┌──────────────────────────────────────────────────────────┐
│  ← 메인으로                                               │
└──────────────────────────────────────────────────────────┘
│                                                          │
│                    🔐 고객 로그인                         │
│                                                          │
│              고객 정보를 선택하세요                       │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │                                                    │ │
│  │  고객 선택:                                        │ │
│  │  ┌─────────────────────────────────────────────┐  │ │
│  │  │ ▼ 박지성 (영국 맨체스타, 000-5000-0001)     │  │ │
│  │  └─────────────────────────────────────────────┘  │ │
│  │                                                    │ │
│  │  ┌─────────────────────────────────────────────┐  │ │
│  │  │          [로그인하기]                       │  │ │
│  │  └─────────────────────────────────────────────┘  │ │
│  │                                                    │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  💡 안내: 교육용 시스템으로 간단한 로그인 방식입니다       │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### HTML 구조
```html
<main class="container">
  <div class="login-card">
    <h1>🔐 고객 로그인</h1>
    <p>고객 정보를 선택하세요</p>

    <form id="login-form">
      <label for="customer-select">고객 선택:</label>
      <select id="customer-select" required>
        <option value="">-- 선택하세요 --</option>
        <!-- JavaScript로 동적 생성 -->
      </select>

      <button type="submit" class="btn-primary">로그인하기</button>
    </form>

    <div class="info-message">
      💡 안내: 교육용 시스템으로 간단한 로그인 방식입니다
    </div>
  </div>
</main>
```

### JavaScript 로직
```javascript
window.addEventListener('DOMContentLoaded', async () => {
  await loadCustomers();
});

async function loadCustomers() {
  const response = await fetchAPI('/api/customers?action=list');
  const customers = response.data;

  const selectElement = document.getElementById('customer-select');
  customers.forEach(customer => {
    const option = document.createElement('option');
    option.value = customer.custid;
    option.textContent = `${customer.name} (${customer.address}, ${customer.phone})`;
    selectElement.appendChild(option);
  });
}

document.getElementById('login-form').addEventListener('submit', (e) => {
  e.preventDefault();
  const custid = document.getElementById('customer-select').value;

  // localStorage에 저장
  localStorage.setItem('custid', custid);

  // 도서 목록으로 이동
  window.location.href = 'books.html';
});
```

### API 호출
```
GET /api/customers?action=list
→ 전체 고객 목록 조회
```

### SQL 쿼리
```sql
-- 전체 고객 조회
SELECT custid, name, address, phone
FROM Customer
ORDER BY custid;
```

---

## 3. 도서 목록 (books.html)

### 목적
전체 도서 조회, 검색, 필터링

### 와이어프레임
```
┌──────────────────────────────────────────────────────────┐
│  마당 서점     [로그인: 박지성님 👤]  [로그아웃]  [홈]    │
└──────────────────────────────────────────────────────────┘
│                                                          │
│  📚 도서 목록                                            │
│                                                          │
│  ┌──────────────────────────┐  ┌───────────────────┐   │
│  │ 🔍 [검색어 입력...]      │  │ 출판사: [전체 ▼]  │   │
│  └──────────────────────────┘  └───────────────────┘   │
│                                                          │
│  총 10권의 도서가 있습니다                               │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  1. 축구의 역사                                    │ │
│  │     출판사: 굿스포츠  |  가격: 7,000원             │ │
│  │     판매: 5건  |  평균 판매가: 6,200원             │ │
│  │                                    [상세보기 →]    │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  2. 축구 아는 여자                                 │ │
│  │     출판사: 나무수  |  가격: 13,000원              │ │
│  │     판매: 2건  |  평균 판매가: 12,500원            │ │
│  │                                    [상세보기 →]    │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ... (계속)                                              │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### HTML 구조
```html
<header class="header">
  <div class="container">
    <div class="logo">마당 서점</div>
    <div id="user-info"></div>
  </div>
</header>

<main class="container">
  <h1>📚 도서 목록</h1>

  <div class="filters">
    <input type="text" id="search-input" placeholder="🔍 검색어 입력...">
    <select id="publisher-filter">
      <option value="">전체 출판사</option>
      <!-- JavaScript로 동적 생성 -->
    </select>
  </div>

  <div id="book-count" class="info-text"></div>

  <div id="book-list" class="book-list"></div>
</main>
```

### JavaScript 로직
```javascript
let allBooks = [];
let currentFilter = { keyword: '', publisher: '' };

window.addEventListener('DOMContentLoaded', async () => {
  checkLogin();
  await loadPublishers();
  await loadBooks();

  setupEventListeners();
});

async function loadBooks() {
  let url = '/api/books?action=list';

  if (currentFilter.keyword) {
    url = `/api/books?action=search&keyword=${currentFilter.keyword}`;
  } else if (currentFilter.publisher) {
    url = `/api/books?action=publisher&name=${currentFilter.publisher}`;
  }

  const response = await fetchAPI(url);
  allBooks = response.data;
  renderBookList(allBooks);
}

async function loadPublishers() {
  const response = await fetchAPI('/api/books?action=publishers');
  const publishers = response.data;

  const selectElement = document.getElementById('publisher-filter');
  publishers.forEach(pub => {
    const option = document.createElement('option');
    option.value = pub;
    option.textContent = pub;
    selectElement.appendChild(option);
  });
}

function renderBookList(books) {
  const listElement = document.getElementById('book-list');
  document.getElementById('book-count').textContent =
    `총 ${books.length}권의 도서가 있습니다`;

  listElement.innerHTML = books.map(book => `
    <div class="book-item card">
      <h3>${book.bookid}. ${book.bookname}</h3>
      <p>출판사: ${book.publisher} | 가격: ${book.price.toLocaleString()}원</p>
      <p class="text-secondary">판매: ${book.salesCount || 0}건 |
         평균 판매가: ${book.avgPrice ? book.avgPrice.toLocaleString() : '-'}원</p>
      <a href="book-detail.html?id=${book.bookid}" class="btn-primary">상세보기 →</a>
    </div>
  `).join('');
}

function setupEventListeners() {
  document.getElementById('search-input').addEventListener('input',
    debounce(async (e) => {
      currentFilter.keyword = e.target.value;
      currentFilter.publisher = '';
      await loadBooks();
    }, 500)
  );

  document.getElementById('publisher-filter').addEventListener('change',
    async (e) => {
      currentFilter.publisher = e.target.value;
      currentFilter.keyword = '';
      document.getElementById('search-input').value = '';
      await loadBooks();
    }
  );
}
```

### API 호출
```
GET /api/books?action=list
GET /api/books?action=search&keyword=축구
GET /api/books?action=publisher&name=굿스포츠
GET /api/books?action=publishers
```

### SQL 쿼리
```sql
-- 전체 도서 조회
SELECT bookid, bookname, publisher, price
FROM Book
ORDER BY bookid;

-- 도서명 검색
SELECT bookid, bookname, publisher, price
FROM Book
WHERE bookname LIKE CONCAT('%', ?, '%')
ORDER BY bookid;

-- 출판사별 필터
SELECT bookid, bookname, publisher, price
FROM Book
WHERE publisher = ?
ORDER BY bookid;

-- 출판사 목록
SELECT DISTINCT publisher
FROM Book
ORDER BY publisher;
```

---

## 4. 도서 상세 (book-detail.html)

### 목적
도서 상세 정보 확인 및 구매

### 와이어프레임
```
┌──────────────────────────────────────────────────────────┐
│  ← 목록으로                [로그인: 박지성님 👤]           │
└──────────────────────────────────────────────────────────┘
│                                                          │
│  📖 도서 상세 정보                                        │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │                                                    │ │
│  │  축구의 역사                                       │ │
│  │                                                    │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │                                                    │ │
│  │  📌 도서 ID: 1                                     │ │
│  │  🏢 출판사: 굿스포츠                               │ │
│  │  💰 정가: 7,000원                                  │ │
│  │                                                    │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │                                                    │ │
│  │  🛒 구매하기                                       │ │
│  │                                                    │ │
│  │  할인가 입력:                                      │ │
│  │  ┌──────────────────┐                             │ │
│  │  │ 6000            │ 원                           │ │
│  │  └──────────────────┘                             │ │
│  │  ⚠️ 정가(7,000원) 이하로 입력하세요                │ │
│  │                                                    │ │
│  │  ┌─────────────────────────────────────────────┐  │ │
│  │  │          [구매하기 🛒]                      │  │ │
│  │  └─────────────────────────────────────────────┘  │ │
│  │                                                    │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │                                                    │ │
│  │  📊 이 도서의 판매 통계                            │ │
│  │                                                    │ │
│  │  • 총 판매 수량: 5건                              │ │
│  │  • 평균 판매가: 6,200원                           │ │
│  │  • 최고 판매가: 6,500원                           │ │
│  │  • 최저 판매가: 6,000원                           │ │
│  │                                                    │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### HTML 구조
```html
<main class="container">
  <a href="books.html" class="back-link">← 목록으로</a>

  <h1>📖 도서 상세 정보</h1>

  <div id="book-detail" class="detail-card"></div>
</main>
```

### JavaScript 로직
```javascript
let currentBook = null;

window.addEventListener('DOMContentLoaded', async () => {
  checkLogin();

  const bookId = new URLSearchParams(window.location.search).get('id');
  if (!bookId) {
    alert('잘못된 접근입니다.');
    window.location.href = 'books.html';
    return;
  }

  await loadBookDetail(bookId);
});

async function loadBookDetail(bookId) {
  const response = await fetchAPI(`/api/books?action=detail&id=${bookId}`);
  currentBook = response.data;

  const statsResponse = await fetchAPI(`/api/books?action=stats&id=${bookId}`);
  const stats = statsResponse.data;

  renderBookDetail(currentBook, stats);
}

function renderBookDetail(book, stats) {
  const detailElement = document.getElementById('book-detail');

  detailElement.innerHTML = `
    <div class="card">
      <h2>${book.bookname}</h2>
      <hr>
      <p>📌 도서 ID: ${book.bookid}</p>
      <p>🏢 출판사: ${book.publisher}</p>
      <p>💰 정가: ${book.price.toLocaleString()}원</p>
      <hr>

      <h3>🛒 구매하기</h3>
      <form id="order-form">
        <label for="saleprice">할인가 입력:</label>
        <input type="number" id="saleprice"
               max="${book.price}"
               value="${book.price}"
               required> 원
        <p class="warning">⚠️ 정가(${book.price.toLocaleString()}원) 이하로 입력하세요</p>
        <button type="submit" class="btn-primary">구매하기 🛒</button>
      </form>

      <hr>

      <h3>📊 이 도서의 판매 통계</h3>
      <ul>
        <li>총 판매 수량: ${stats.salesCount || 0}건</li>
        <li>평균 판매가: ${stats.avgPrice ? stats.avgPrice.toLocaleString() : '-'}원</li>
        <li>최고 판매가: ${stats.maxPrice ? stats.maxPrice.toLocaleString() : '-'}원</li>
        <li>최저 판매가: ${stats.minPrice ? stats.minPrice.toLocaleString() : '-'}원</li>
      </ul>
    </div>
  `;

  document.getElementById('order-form').addEventListener('submit', handleOrder);
}

async function handleOrder(e) {
  e.preventDefault();

  const custid = localStorage.getItem('custid');
  if (!custid) {
    alert('로그인이 필요합니다.');
    window.location.href = 'customer-login.html';
    return;
  }

  const saleprice = parseInt(document.getElementById('saleprice').value);

  if (saleprice > currentBook.price) {
    alert('판매가는 정가를 초과할 수 없습니다.');
    return;
  }

  // 주문 페이지로 이동
  window.location.href =
    `order.html?bookid=${currentBook.bookid}&saleprice=${saleprice}`;
}
```

### API 호출
```
GET /api/books?action=detail&id=1
GET /api/books?action=stats&id=1
```

### SQL 쿼리
```sql
-- 도서 상세 조회
SELECT bookid, bookname, publisher, price
FROM Book
WHERE bookid = ?;

-- 도서별 판매 통계
SELECT
  COUNT(*) as salesCount,
  AVG(saleprice) as avgPrice,
  MAX(saleprice) as maxPrice,
  MIN(saleprice) as minPrice
FROM Orders
WHERE bookid = ?;
```

---

## 5. 주문하기 (order.html)

### 목적
주문 정보 확인 및 주문 완료

### 와이어프레임
```
┌──────────────────────────────────────────────────────────┐
│  ← 취소                    [로그인: 박지성님 👤]           │
└──────────────────────────────────────────────────────────┘
│                                                          │
│  🛒 주문 확인                                            │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │                                                    │ │
│  │  👤 주문자 정보                                    │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │  • 이름: 박지성                                    │ │
│  │  • 주소: 영국 맨체스타                             │ │
│  │  • 전화: 000-5000-0001                             │ │
│  │                                                    │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │                                                    │ │
│  │  📖 주문 도서                                      │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │  • 도서명: 축구의 역사                             │ │
│  │  • 출판사: 굿스포츠                                │ │
│  │  • 정가: 7,000원                                   │ │
│  │  • 구매가: 6,000원 ✅                              │ │
│  │                                                    │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │                                                    │ │
│  │  📅 주문일: 2025-10-02                             │ │
│  │                                                    │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │                                                    │ │
│  │  ┌──────────────────┐  ┌──────────────────┐      │ │
│  │  │ [주문 완료하기 ✅]│  │   [취소 ❌]     │      │ │
│  │  └──────────────────┘  └──────────────────┘      │ │
│  │                                                    │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### HTML 구조
```html
<main class="container">
  <h1>🛒 주문 확인</h1>

  <div id="order-summary" class="card"></div>
</main>
```

### JavaScript 로직
```javascript
let orderData = {
  custid: null,
  bookid: null,
  saleprice: null
};

window.addEventListener('DOMContentLoaded', async () => {
  checkLogin();

  const params = new URLSearchParams(window.location.search);
  orderData.custid = parseInt(localStorage.getItem('custid'));
  orderData.bookid = parseInt(params.get('bookid'));
  orderData.saleprice = parseInt(params.get('saleprice'));

  if (!orderData.custid || !orderData.bookid || !orderData.saleprice) {
    alert('잘못된 접근입니다.');
    window.location.href = 'books.html';
    return;
  }

  await loadOrderSummary();
});

async function loadOrderSummary() {
  const customerResponse = await fetchAPI(
    `/api/customers?action=detail&id=${orderData.custid}`
  );
  const customer = customerResponse.data;

  const bookResponse = await fetchAPI(
    `/api/books?action=detail&id=${orderData.bookid}`
  );
  const book = bookResponse.data;

  renderOrderSummary(customer, book);
}

function renderOrderSummary(customer, book) {
  const summaryElement = document.getElementById('order-summary');
  const today = new Date().toISOString().split('T')[0];

  summaryElement.innerHTML = `
    <h3>👤 주문자 정보</h3>
    <hr>
    <ul>
      <li>이름: ${customer.name}</li>
      <li>주소: ${customer.address}</li>
      <li>전화: ${customer.phone}</li>
    </ul>

    <hr>

    <h3>📖 주문 도서</h3>
    <hr>
    <ul>
      <li>도서명: ${book.bookname}</li>
      <li>출판사: ${book.publisher}</li>
      <li>정가: ${book.price.toLocaleString()}원</li>
      <li class="highlight">구매가: ${orderData.saleprice.toLocaleString()}원 ✅</li>
    </ul>

    <hr>

    <p>📅 주문일: ${today}</p>

    <hr>

    <div class="button-group">
      <button id="confirm-btn" class="btn-primary">주문 완료하기 ✅</button>
      <button id="cancel-btn" class="btn-secondary">취소 ❌</button>
    </div>
  `;

  document.getElementById('confirm-btn').addEventListener('click', confirmOrder);
  document.getElementById('cancel-btn').addEventListener('click', () => {
    history.back();
  });
}

async function confirmOrder() {
  try {
    const response = await fetchAPI('/api/orders?action=create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(orderData)
    });

    if (response.success) {
      alert('주문이 완료되었습니다! 🎉');
      window.location.href = 'my-orders.html';
    }
  } catch (error) {
    alert('주문 중 오류가 발생했습니다: ' + error.message);
  }
}
```

### API 호출
```
GET /api/customers?action=detail&id=1
GET /api/books?action=detail&id=1
POST /api/orders?action=create
  Body: { custid, bookid, saleprice }
```

### SQL 쿼리
```sql
-- 주문자 정보 조회
SELECT custid, name, address, phone
FROM Customer
WHERE custid = ?;

-- 도서 정보 조회
SELECT bookid, bookname, publisher, price
FROM Book
WHERE bookid = ?;

-- 주문 생성
INSERT INTO Orders (orderid, custid, bookid, saleprice, orderdate)
VALUES (
  (SELECT IFNULL(MAX(orderid), 0) + 1 FROM Orders AS o),
  ?,
  ?,
  ?,
  CURDATE()
);
```

---

## 6. 내 주문 내역 (my-orders.html)

### 목적
로그인한 고객의 주문 내역 조회 및 취소

### 와이어프레임
```
┌──────────────────────────────────────────────────────────┐
│  마당 서점     [로그인: 박지성님 👤]  [로그아웃]  [홈]    │
└──────────────────────────────────────────────────────────┘
│                                                          │
│  📋 내 주문 내역                                         │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  📊 주문 통계                                      │ │
│  │  • 총 주문 수: 3건                                 │ │
│  │  • 총 구매 금액: 35,000원                          │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  주문번호: #1                        2024-07-01    │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │  📖 축구의 역사 (굿스포츠)                        │ │
│  │  💰 구매가: 6,000원                               │ │
│  │                                                    │ │
│  │                              [주문 취소 🗑️]       │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  주문번호: #2                        2024-07-03    │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │  📖 축구의 이해 (대한미디어)                      │ │
│  │  💰 구매가: 21,000원                              │ │
│  │                                                    │ │
│  │                              [주문 취소 🗑️]       │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ... (계속)                                              │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### HTML 구조
```html
<header class="header">
  <div class="container">
    <div class="logo">마당 서점</div>
    <div id="user-info"></div>
  </div>
</header>

<main class="container">
  <h1>📋 내 주문 내역</h1>

  <div id="order-stats" class="stats-card"></div>

  <hr>

  <div id="order-list" class="order-list"></div>
</main>
```

### JavaScript 로직
```javascript
let myOrders = [];
let custid = null;

window.addEventListener('DOMContentLoaded', async () => {
  checkLogin();

  custid = parseInt(localStorage.getItem('custid'));
  if (!custid) {
    alert('로그인이 필요합니다.');
    window.location.href = 'customer-login.html';
    return;
  }

  await loadMyOrders();
});

async function loadMyOrders() {
  const response = await fetchAPI(`/api/orders?action=list&custid=${custid}`);
  myOrders = response.data;

  const statsResponse = await fetchAPI(`/api/orders?action=stats&custid=${custid}`);
  const stats = statsResponse.data;

  renderOrderStats(stats);
  renderOrderList(myOrders);
}

function renderOrderStats(stats) {
  const statsElement = document.getElementById('order-stats');

  statsElement.innerHTML = `
    <h3>📊 주문 통계</h3>
    <ul>
      <li>총 주문 수: ${stats.orderCount || 0}건</li>
      <li>총 구매 금액: ${stats.totalAmount ? stats.totalAmount.toLocaleString() : 0}원</li>
    </ul>
  `;
}

function renderOrderList(orders) {
  const listElement = document.getElementById('order-list');

  if (orders.length === 0) {
    listElement.innerHTML = '<p class="empty-message">주문 내역이 없습니다.</p>';
    return;
  }

  listElement.innerHTML = orders.map(order => `
    <div class="order-item card">
      <div class="order-header">
        <span>주문번호: #${order.orderid}</span>
        <span>${order.orderdate}</span>
      </div>
      <hr>
      <p>📖 ${order.bookname} (${order.publisher})</p>
      <p>💰 구매가: ${order.saleprice.toLocaleString()}원</p>
      <button class="btn-danger" onclick="cancelOrder(${order.orderid})">
        주문 취소 🗑️
      </button>
    </div>
  `).join('');
}

async function cancelOrder(orderid) {
  if (!confirm('정말 이 주문을 취소하시겠습니까?')) {
    return;
  }

  try {
    const response = await fetchAPI(
      `/api/orders?action=delete&id=${orderid}&custid=${custid}`,
      { method: 'DELETE' }
    );

    if (response.success) {
      alert('주문이 취소되었습니다.');
      await loadMyOrders(); // 새로고침
    }
  } catch (error) {
    alert('주문 취소 중 오류가 발생했습니다: ' + error.message);
  }
}
```

### API 호출
```
GET /api/orders?action=list&custid=1
GET /api/orders?action=stats&custid=1
DELETE /api/orders?action=delete&id=1&custid=1
```

### SQL 쿼리
```sql
-- 내 주문 목록 (JOIN)
SELECT o.orderid, o.saleprice, o.orderdate,
       b.bookname, b.publisher
FROM Orders o
JOIN Book b ON o.bookid = b.bookid
WHERE o.custid = ?
ORDER BY o.orderdate DESC;

-- 주문 통계
SELECT
  COUNT(*) as orderCount,
  SUM(saleprice) as totalAmount
FROM Orders
WHERE custid = ?;

-- 주문 취소 (DELETE)
DELETE FROM Orders
WHERE orderid = ? AND custid = ?;
```

---

## 7. 관리자 대시보드 (dashboard.html)

### 목적
전체 통계 및 분석 (CEO, 관리자, 개발자용)

### 와이어프레임
```
┌──────────────────────────────────────────────────────────┐
│  📊 마당 서점 관리 대시보드                     [홈으로]  │
└──────────────────────────────────────────────────────────┘
│                                                          │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐         │
│  │ 전체 │ │고객별│ │출판사│ │도서별│ │최근  │         │
│  │ 통계 │ │ 통계 │ │ 별   │ │ 통계 │ │주문  │         │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘         │
│   [Active]                                               │
│                                                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                          │
│  📈 전체 통계 개요                                       │
│                                                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │
│  │ 📚 총 도서   │ │ 👥 총 고객   │ │ 📋 총 주문   │       │
│  │    10권     │ │    5명      │ │    10건     │       │
│  └─────────────┘ └─────────────┘ └─────────────┘       │
│                                                          │
│  ┌─────────────┐ ┌─────────────┐                        │
│  │ 💰 총 매출액 │ │ 📊 평균판매가│                        │
│  │  94,000원   │ │   9,400원   │                        │
│  └─────────────┘ └─────────────┘                        │
│                                                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                          │
│  🏆 베스트셀러 TOP 5                                     │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │ 1위. 축구의 역사 (굿스포츠)           5건 판매     │ │
│  └────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────┐ │
│  │ 2위. 축구의 이해 (대한미디어)         3건 판매     │ │
│  └────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────┐ │
│  │ 3위. 축구 아는 여자 (나무수)          2건 판매     │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                          │
│  🕐 최근 주문 (5건)                                      │
│                                                          │
│  2024-07-03 | 김연아 | 축구 아는 여자 | 8,000원         │
│  2024-07-03 | 박지성 | 축구의 이해 | 21,000원            │
│  2024-07-01 | 박지성 | 축구의 역사 | 6,000원             │
│  ...                                                     │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### HTML 구조
```html
<header class="dashboard-header">
  <h1>📊 마당 서점 관리 대시보드</h1>
  <a href="index.html" class="btn-secondary">홈으로</a>
</header>

<main class="container">
  <nav class="tabs">
    <button class="tab active" data-tab="overview">전체 통계</button>
    <button class="tab" data-tab="customers">고객별 통계</button>
    <button class="tab" data-tab="publishers">출판사별 통계</button>
    <button class="tab" data-tab="books">도서별 통계</button>
    <button class="tab" data-tab="recent">최근 주문</button>
  </nav>

  <div id="overview-tab" class="tab-content active">
    <div id="overview-stats"></div>
    <hr>
    <div id="bestsellers"></div>
    <hr>
    <div id="recent-orders"></div>
  </div>

  <div id="customers-tab" class="tab-content">
    <div id="customer-stats"></div>
  </div>

  <div id="publishers-tab" class="tab-content">
    <div id="publisher-stats"></div>
  </div>

  <div id="books-tab" class="tab-content">
    <div id="book-stats"></div>
  </div>

  <div id="recent-tab" class="tab-content">
    <div id="all-recent-orders"></div>
  </div>
</main>
```

### JavaScript 로직
```javascript
window.addEventListener('DOMContentLoaded', async () => {
  await loadOverviewTab();
  setupTabs();
});

function setupTabs() {
  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', async (e) => {
      const tabName = e.target.dataset.tab;

      // 탭 활성화
      document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

      e.target.classList.add('active');
      document.getElementById(`${tabName}-tab`).classList.add('active');

      // 데이터 로드
      switch(tabName) {
        case 'overview':
          await loadOverviewTab();
          break;
        case 'customers':
          await loadCustomerStats();
          break;
        case 'publishers':
          await loadPublisherStats();
          break;
        case 'books':
          await loadBookStats();
          break;
        case 'recent':
          await loadRecentOrders();
          break;
      }
    });
  });
}

async function loadOverviewTab() {
  const response = await fetchAPI('/api/stats?action=overview');
  const stats = response.data;

  const bestsellersResponse = await fetchAPI('/api/stats?action=bestsellers&limit=5');
  const bestsellers = bestsellersResponse.data;

  const recentResponse = await fetchAPI('/api/stats?action=recent&limit=5');
  const recentOrders = recentResponse.data;

  renderOverviewStats(stats);
  renderBestsellers(bestsellers);
  renderRecentOrders(recentOrders);
}

function renderOverviewStats(stats) {
  const element = document.getElementById('overview-stats');

  element.innerHTML = `
    <h2>📈 전체 통계 개요</h2>
    <div class="stats-grid">
      <div class="stat-card">
        <h3>📚 총 도서</h3>
        <p class="stat-number">${stats.totalBooks}권</p>
      </div>
      <div class="stat-card">
        <h3>👥 총 고객</h3>
        <p class="stat-number">${stats.totalCustomers}명</p>
      </div>
      <div class="stat-card">
        <h3>📋 총 주문</h3>
        <p class="stat-number">${stats.totalOrders}건</p>
      </div>
      <div class="stat-card">
        <h3>💰 총 매출액</h3>
        <p class="stat-number">${stats.totalRevenue.toLocaleString()}원</p>
      </div>
      <div class="stat-card">
        <h3>📊 평균 판매가</h3>
        <p class="stat-number">${Math.round(stats.avgSalePrice).toLocaleString()}원</p>
      </div>
    </div>
  `;
}

function renderBestsellers(bestsellers) {
  const element = document.getElementById('bestsellers');

  element.innerHTML = `
    <h2>🏆 베스트셀러 TOP 5</h2>
    ${bestsellers.map((book, index) => `
      <div class="bestseller-item card">
        <span class="rank">${index + 1}위</span>
        <span class="book-info">${book.bookname} (${book.publisher})</span>
        <span class="sales">${book.salesCount}건 판매</span>
      </div>
    `).join('')}
  `;
}

function renderRecentOrders(orders) {
  const element = document.getElementById('recent-orders');

  element.innerHTML = `
    <h2>🕐 최근 주문 (5건)</h2>
    <table class="table">
      <thead>
        <tr>
          <th>주문일</th>
          <th>고객명</th>
          <th>도서명</th>
          <th>판매가</th>
        </tr>
      </thead>
      <tbody>
        ${orders.map(order => `
          <tr>
            <td>${order.orderdate}</td>
            <td>${order.customerName}</td>
            <td>${order.bookname}</td>
            <td>${order.saleprice.toLocaleString()}원</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}

async function loadCustomerStats() {
  const response = await fetchAPI('/api/stats?action=customers');
  const customers = response.data;

  const element = document.getElementById('customer-stats');

  element.innerHTML = `
    <h2>👥 고객별 구매 통계</h2>
    <table class="table">
      <thead>
        <tr>
          <th>고객명</th>
          <th>주문 수</th>
          <th>총 구매액</th>
          <th>평균 구매액</th>
        </tr>
      </thead>
      <tbody>
        ${customers.map(cust => `
          <tr>
            <td>${cust.name}</td>
            <td>${cust.orderCount}건</td>
            <td>${(cust.totalAmount || 0).toLocaleString()}원</td>
            <td>${(cust.avgAmount || 0).toLocaleString()}원</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}

async function loadPublisherStats() {
  const response = await fetchAPI('/api/stats?action=publishers');
  const publishers = response.data;

  const element = document.getElementById('publisher-stats');

  element.innerHTML = `
    <h2>🏢 출판사별 매출 통계</h2>
    <table class="table">
      <thead>
        <tr>
          <th>출판사</th>
          <th>도서 수</th>
          <th>판매 건수</th>
          <th>총 매출액</th>
        </tr>
      </thead>
      <tbody>
        ${publishers.map(pub => `
          <tr>
            <td>${pub.publisher}</td>
            <td>${pub.bookCount}권</td>
            <td>${pub.salesCount}건</td>
            <td>${(pub.totalRevenue || 0).toLocaleString()}원</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}

async function loadBookStats() {
  const response = await fetchAPI('/api/stats?action=books');
  const books = response.data;

  const element = document.getElementById('book-stats');

  element.innerHTML = `
    <h2>📖 도서별 판매 현황</h2>
    <table class="table">
      <thead>
        <tr>
          <th>도서명</th>
          <th>출판사</th>
          <th>정가</th>
          <th>판매 수</th>
          <th>평균 판매가</th>
        </tr>
      </thead>
      <tbody>
        ${books.map(book => `
          <tr>
            <td>${book.bookname}</td>
            <td>${book.publisher}</td>
            <td>${book.price.toLocaleString()}원</td>
            <td>${book.salesCount}건</td>
            <td>${(book.avgSalePrice || 0).toLocaleString()}원</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}
```

### API 호출
```
GET /api/stats?action=overview
GET /api/stats?action=bestsellers&limit=5
GET /api/stats?action=recent&limit=5
GET /api/stats?action=customers
GET /api/stats?action=publishers
GET /api/stats?action=books
```

### SQL 쿼리
```sql
-- 전체 통계
SELECT
  (SELECT COUNT(*) FROM Book) as totalBooks,
  (SELECT COUNT(*) FROM Customer) as totalCustomers,
  (SELECT COUNT(*) FROM Orders) as totalOrders,
  (SELECT SUM(saleprice) FROM Orders) as totalRevenue,
  (SELECT AVG(saleprice) FROM Orders) as avgSalePrice;

-- 베스트셀러
SELECT b.bookname, b.publisher, COUNT(*) as salesCount
FROM Orders o
JOIN Book b ON o.bookid = b.bookid
GROUP BY b.bookid, b.bookname, b.publisher
ORDER BY salesCount DESC
LIMIT ?;

-- 최근 주문
SELECT o.orderdate, c.name as customerName, b.bookname, o.saleprice
FROM Orders o
JOIN Customer c ON o.custid = c.custid
JOIN Book b ON o.bookid = b.bookid
ORDER BY o.orderdate DESC
LIMIT ?;

-- 고객별 통계
SELECT c.name,
       COUNT(o.orderid) as orderCount,
       SUM(o.saleprice) as totalAmount,
       AVG(o.saleprice) as avgAmount
FROM Customer c
LEFT JOIN Orders o ON c.custid = o.custid
GROUP BY c.custid, c.name
ORDER BY totalAmount DESC;

-- 출판사별 통계
SELECT b.publisher,
       COUNT(DISTINCT b.bookid) as bookCount,
       COUNT(o.orderid) as salesCount,
       SUM(o.saleprice) as totalRevenue
FROM Book b
LEFT JOIN Orders o ON b.bookid = o.bookid
GROUP BY b.publisher
ORDER BY totalRevenue DESC;

-- 도서별 판매 현황
SELECT b.bookname, b.publisher, b.price,
       COUNT(o.orderid) as salesCount,
       AVG(o.saleprice) as avgSalePrice
FROM Book b
LEFT JOIN Orders o ON b.bookid = o.bookid
GROUP BY b.bookid, b.bookname, b.publisher, b.price
ORDER BY salesCount DESC;
```

---

## 공통 컴포넌트 및 스타일

### CSS 클래스 네이밍 (Tailwind 스타일)

```css
/* Layout */
.container        /* 최대 너비 1200px, 중앙 정렬 */
.grid-3          /* 3열 그리드 */
.flex            /* Flexbox */
.space-between   /* justify-content: space-between */

/* Components */
.card            /* 카드 컴포넌트 (그림자, 패딩, 둥근 모서리) */
.btn-primary     /* 주요 버튼 (파란색) */
.btn-secondary   /* 보조 버튼 (회색) */
.btn-danger      /* 위험 버튼 (빨간색) */

/* Text */
.text-primary    /* 주요 텍스트 색상 */
.text-secondary  /* 보조 텍스트 색상 */
.text-danger     /* 위험 텍스트 색상 */

/* Spacing */
.p-4             /* padding: 1rem */
.m-4             /* margin: 1rem */
.gap-4           /* gap: 1rem */

/* Utilities */
.hidden          /* display: none */
.active          /* 활성 상태 */
```

### JavaScript 공통 함수 (api.js)

```javascript
async function fetchAPI(endpoint, options = {}) {
  const baseURL = 'http://localhost:8080';
  const response = await fetch(baseURL + endpoint, options);
  return await response.json();
}

function checkLogin() {
  const custid = localStorage.getItem('custid');
  if (custid) {
    const userInfo = document.getElementById('user-info');
    if (userInfo) {
      userInfo.innerHTML = `
        <span>로그인: ${localStorage.getItem('custname') || '고객'}님 👤</span>
        <button onclick="logout()">로그아웃</button>
      `;
    }
  }
}

function logout() {
  localStorage.removeItem('custid');
  localStorage.removeItem('custname');
  window.location.href = 'index.html';
}

function debounce(func, delay) {
  let timeout;
  return function(...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), delay);
  };
}
```

---

*최종 업데이트: 2025-10-02*
