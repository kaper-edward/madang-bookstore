/**
 * 공통 헤더/푸터 렌더링 모듈
 * 모든 페이지에서 일관된 헤더와 푸터를 표시합니다.
 */

/**
 * 공통 헤더 렌더링
 * @param {string} currentPage - 현재 활성화된 메뉴 (books, customer, orders, admin)
 */
function renderHeader(currentPage = '') {
  const header = document.querySelector('header');
  if (!header) return;

  const customer = getCustomerInfo();

  header.innerHTML = `
    <div class="max-w-6xl mx-auto flex items-center justify-between py-4 px-6">
      <a href="index.html" class="text-xl font-semibold tracking-tight flex items-center gap-2">
        <span class="text-primary-300">📚</span> 마당 온라인 서점
      </a>
      <nav class="flex items-center gap-3 text-sm font-medium">
        <a href="books.html" class="nav-link ${currentPage === 'books' ? 'active' : ''}">도서</a>
        <a href="customer-login.html" class="nav-link ${currentPage === 'customer' ? 'active' : ''}">고객</a>
        <a href="my-orders.html" class="nav-link ${currentPage === 'orders' ? 'active' : ''}">내 주문</a>
        <a href="admin/index.html" class="btn-outline ${currentPage === 'admin' ? 'active' : ''}">관리자</a>
      </nav>
      <div id="user-info" class="user-pill">
        ${customer ? `
          <div class="flex items-center gap-2">
            <span class="text-secondary">로그인: <strong>${customer.name}</strong>님 👤</span>
            <button onclick="logout()" class="btn-secondary">로그아웃</button>
          </div>
        ` : ''}
      </div>
    </div>
  `;
}

/**
 * 공통 푸터 렌더링
 */
function renderFooter() {
  const footer = document.querySelector('footer');
  if (!footer) return;

  footer.innerHTML = `
    <div class="max-w-6xl mx-auto px-6 py-12">
      <div class="grid md:grid-cols-4 gap-8 mb-8">
        <div>
          <h3 class="text-lg font-semibold mb-4 flex items-center gap-2">
            <span class="text-primary-300">📚</span> 마당 서점
          </h3>
          <p class="text-sm text-slate-400 leading-relaxed">
            교육용 온라인 서점 시스템<br>
            SQL과 웹 개발을 학습하기 위한<br>
            실습 프로젝트입니다.
          </p>
        </div>
        <div>
          <h4 class="text-sm font-semibold uppercase tracking-wider text-slate-300 mb-4">서비스</h4>
          <ul class="space-y-2 text-sm">
            <li><a href="books.html" class="text-slate-400 hover:text-primary-300 transition">도서 목록</a></li>
            <li><a href="my-orders.html" class="text-slate-400 hover:text-primary-300 transition">주문 내역</a></li>
            <li><a href="admin/index.html" class="text-slate-400 hover:text-primary-300 transition">관리자</a></li>
          </ul>
        </div>
        <div>
          <h4 class="text-sm font-semibold uppercase tracking-wider text-slate-300 mb-4">고객 지원</h4>
          <ul class="space-y-2 text-sm">
            <li><a href="customer-login.html" class="text-slate-400 hover:text-primary-300 transition">로그인</a></li>
            <li><a href="#" class="text-slate-400 hover:text-primary-300 transition">FAQ</a></li>
            <li><a href="#" class="text-slate-400 hover:text-primary-300 transition">고객센터</a></li>
            <li><a href="#" class="text-slate-400 hover:text-primary-300 transition">문의하기</a></li>
          </ul>
        </div>
        <div>
          <h4 class="text-sm font-semibold uppercase tracking-wider text-slate-300 mb-4">회사 정보</h4>
          <ul class="space-y-2 text-sm text-slate-400">
            <li>(46241) 부산광역시 금정구</li>
            <li>부산대학로63번길 2 (장전동)</li>
            <li>부산대학교 소프트웨어융합교육원</li>
            <li>TEL: 051-510-xxxx</li>
          </ul>
        </div>
      </div>
      <div class="pt-8 border-t border-white/5">
        <div class="flex flex-col md:flex-row justify-between items-center gap-4">
          <p class="text-sm text-slate-500">
            COPYRIGHT(C) PUSAN NATIONAL UNIVERSITY. ALL RIGHTS RESERVED
          </p>
          <div class="flex gap-4 text-sm">
            <a href="#" class="text-slate-500 hover:text-primary-300 transition">이용약관</a>
            <span class="text-slate-700">|</span>
            <a href="#" class="text-slate-500 hover:text-primary-300 transition">개인정보처리방침</a>
            <span class="text-slate-700">|</span>
            <a href="#" class="text-slate-500 hover:text-primary-300 transition">고객센터</a>
          </div>
        </div>
      </div>
    </div>
  `;
}

/**
 * 관리자 페이지용 헤더 렌더링
 * @param {string} currentPage - 현재 활성화된 메뉴
 */
function renderAdminHeader(currentPage = '') {
  const header = document.querySelector('header');
  if (!header) return;

  const customer = getCustomerInfo();

  // 페이지 위치 감지 (admin 폴더 내부인지 확인)
  const isInAdminFolder = window.location.pathname.includes('/admin/');
  const prefix = isInAdminFolder ? '' : 'admin/';
  const adminPortalLink = isInAdminFolder ? 'index.html' : 'admin/index.html';
  const storeHomeLink = isInAdminFolder ? '../index.html' : 'index.html';
  const dashboardLink = isInAdminFolder ? '../dashboard.html' : 'dashboard.html';

  header.innerHTML = `
    <div class="max-w-6xl mx-auto flex items-center justify-between py-4 px-6">
      <a href="${adminPortalLink}" class="text-xl font-semibold tracking-tight flex items-center gap-2">
        <span class="text-primary-300">🔧</span> 관리자 포털
      </a>
      <nav class="flex items-center gap-3 text-sm font-medium">
        <a href="${dashboardLink}" class="nav-link ${currentPage === 'dashboard' ? 'active' : ''}">대시보드</a>
        <a href="${prefix}books-admin.html" class="nav-link ${currentPage === 'books' ? 'active' : ''}">도서 관리</a>
        <a href="${prefix}customers-admin.html" class="nav-link ${currentPage === 'customers' ? 'active' : ''}">고객 관리</a>
        <a href="${storeHomeLink}" class="btn-outline">서점 홈</a>
      </nav>
      <div id="user-info" class="user-pill">
        ${customer ? `
          <div class="flex items-center gap-2">
            <span class="text-secondary">로그인: <strong>${customer.name}</strong>님 👤</span>
            <button onclick="logout()" class="btn-secondary">로그아웃</button>
          </div>
        ` : ''}
      </div>
    </div>
  `;
}

/**
 * 관리자 페이지용 푸터 렌더링
 */
function renderAdminFooter() {
  const footer = document.querySelector('footer');
  if (!footer) return;

  // 페이지 위치 감지 (admin 폴더 내부인지 확인)
  const isInAdminFolder = window.location.pathname.includes('/admin/');
  const adminPrefix = isInAdminFolder ? '' : 'admin/';
  const parentPrefix = isInAdminFolder ? '../' : '';
  const dashboardLink = isInAdminFolder ? '../dashboard.html' : 'dashboard.html';
  const booksLink = isInAdminFolder ? '../books.html' : 'books.html';
  const ordersLink = isInAdminFolder ? '../my-orders.html' : 'my-orders.html';
  const loginLink = isInAdminFolder ? '../customer-login.html' : 'customer-login.html';

  footer.innerHTML = `
    <div class="max-w-6xl mx-auto px-6 py-12">
      <div class="grid md:grid-cols-4 gap-8 mb-8">
        <div>
          <h3 class="text-lg font-semibold mb-4 flex items-center gap-2">
            <span class="text-primary-300">📚</span> 마당 서점
          </h3>
          <p class="text-sm text-slate-400 leading-relaxed">
            교육용 온라인 서점 시스템<br>
            SQL과 웹 개발을 학습하기 위한<br>
            실습 프로젝트입니다.
          </p>
        </div>
        <div>
          <h4 class="text-sm font-semibold uppercase tracking-wider text-slate-300 mb-4">관리자 메뉴</h4>
          <ul class="space-y-2 text-sm">
            <li><a href="${dashboardLink}" class="text-slate-400 hover:text-primary-300 transition">대시보드</a></li>
            <li><a href="${adminPrefix}books-admin.html" class="text-slate-400 hover:text-primary-300 transition">도서 관리</a></li>
            <li><a href="${adminPrefix}customers-admin.html" class="text-slate-400 hover:text-primary-300 transition">고객 관리</a></li>
          </ul>
        </div>
        <div>
          <h4 class="text-sm font-semibold uppercase tracking-wider text-slate-300 mb-4">사용자 메뉴</h4>
          <ul class="space-y-2 text-sm">
            <li><a href="${booksLink}" class="text-slate-400 hover:text-primary-300 transition">도서 목록</a></li>
            <li><a href="${ordersLink}" class="text-slate-400 hover:text-primary-300 transition">주문 내역</a></li>
            <li><a href="${loginLink}" class="text-slate-400 hover:text-primary-300 transition">로그인</a></li>
          </ul>
        </div>
        <div>
          <h4 class="text-sm font-semibold uppercase tracking-wider text-slate-300 mb-4">회사 정보</h4>
          <ul class="space-y-2 text-sm text-slate-400">
            <li>(46241) 부산광역시 금정구</li>
            <li>부산대학로63번길 2 (장전동)</li>
            <li>부산대학교 소프트웨어융합교육원</li>
            <li>TEL: 051-510-xxxx</li>
          </ul>
        </div>
      </div>
      <div class="pt-8 border-t border-white/5">
        <div class="flex flex-col md:flex-row justify-between items-center gap-4">
          <p class="text-sm text-slate-500">
            COPYRIGHT(C) PUSAN NATIONAL UNIVERSITY. ALL RIGHTS RESERVED
          </p>
          <div class="flex gap-4 text-sm">
            <a href="#" class="text-slate-500 hover:text-primary-300 transition">이용약관</a>
            <span class="text-slate-700">|</span>
            <a href="#" class="text-slate-500 hover:text-primary-300 transition">개인정보처리방침</a>
            <span class="text-slate-700">|</span>
            <a href="#" class="text-slate-500 hover:text-primary-300 transition">고객센터</a>
          </div>
        </div>
      </div>
    </div>
  `;
}

// 페이지 로드 시 자동으로 로그인 상태 확인
document.addEventListener('DOMContentLoaded', function() {
  // 기존 checkLoginStatus 함수를 사용하여 user-info 업데이트
  // (header.js로 헤더가 렌더링된 경우에만)
  const customer = getCustomerInfo();
  const userInfoElement = document.getElementById('user-info');

  if (customer && userInfoElement && !userInfoElement.querySelector('button')) {
    userInfoElement.innerHTML = `
      <div class="flex items-center gap-2">
        <span class="text-secondary">로그인: <strong>${customer.name}</strong>님 👤</span>
        <button onclick="logout()" class="btn-secondary">로그아웃</button>
      </div>
    `;
  }
});
