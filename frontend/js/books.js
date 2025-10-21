let booksCache = [];
let bookSort = { column: 'bookid', direction: 'asc' };
let bookFilters = { title: '', publisher: '', minPrice: '', maxPrice: '' };
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;
let totalItems = 0;
let searchDebounceTimer = null;

// Debounce 함수: 연속된 이벤트를 묶어서 마지막 이벤트만 실행
function debounce(func, delay) {
  return function (...args) {
    clearTimeout(searchDebounceTimer);
    searchDebounceTimer = setTimeout(() => func.apply(this, args), delay);
  };
}

async function initBooksPage() {
  checkLoginStatus();
  setupBooksEventListeners();
  updateBookSortIndicators();

  if (document.getElementById('publisher-filter')) {
    await loadPublishers();
  }
  await loadBooks();
}

function setupBooksEventListeners() {
  document.querySelectorAll('#book-table thead th.sortable').forEach((th) => {
    th.addEventListener('click', () => {
      const column = th.dataset.column;
      if (!column) return;

      if (bookSort.column === column) {
        bookSort.direction = bookSort.direction === 'asc' ? 'desc' : 'asc';
      } else {
        bookSort.column = column;
        bookSort.direction = 'asc';
      }

      currentPage = 1; // 정렬 변경 시 첫 페이지로
      updateBookSortIndicators();
      loadBooks();
    });
  });

  const filterForm = document.getElementById('book-filter-form');
  if (filterForm) {
    filterForm.addEventListener('submit', (event) => {
      event.preventDefault();
      bookFilters.title = valueOrEmpty('filter-title');
      bookFilters.publisher = valueOrEmpty('filter-publisher');
      bookFilters.minPrice = numericOrEmpty('filter-min-price');
      bookFilters.maxPrice = numericOrEmpty('filter-max-price');
      currentPage = 1; // 필터 변경 시 첫 페이지로
      loadBooks();
    });
  }

  const resetButton = document.getElementById('reset-filters') || document.getElementById('book-reset');
  if (resetButton) {
    resetButton.addEventListener('click', () => {
      bookFilters = { title: '', publisher: '', minPrice: '', maxPrice: '' };
      ['filter-title', 'filter-publisher', 'filter-min-price', 'filter-max-price'].forEach((id) => {
        const input = document.getElementById(id);
        if (input) input.value = '';
      });
      currentPage = 1; // 필터 초기화 시 첫 페이지로
      loadBooks();
    });
  }

  const createForm = document.getElementById('book-create-form');
  if (createForm) {
    createForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      const formData = new FormData(createForm);
      const payload = Object.fromEntries(formData.entries());
      payload.price = parseInt(payload.price, 10);

      try {
        toggleLoadingSpinner(true);
        await fetchAPI('/api/books?action=create', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        showToast('도서가 등록되었습니다.', 'success');
        createForm.reset();
        await Promise.all([loadPublishers(), loadBooks()]);
      } catch (error) {
        alert(`도서 등록 중 오류가 발생했습니다: ${error.message}`);
      } finally {
        toggleLoadingSpinner(false);
      }
    });
  }

  const editForm = document.getElementById('book-edit-form');
  if (editForm) {
    editForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      const formData = new FormData(editForm);
      const payload = Object.fromEntries(formData.entries());
      payload.bookid = parseInt(payload.bookid, 10);
      payload.price = parseInt(payload.price, 10);

      try {
        toggleLoadingSpinner(true);
        await fetchAPI('/api/books?action=update', {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
        showToast('도서 정보가 수정되었습니다.', 'success');
        hideBookEditPanel();
        await Promise.all([loadPublishers(), loadBooks()]);
      } catch (error) {
        alert(`도서 수정 중 오류가 발생했습니다: ${error.message}`);
      } finally {
        toggleLoadingSpinner(false);
      }
    });
  }

  const cancelEditBtn = document.getElementById('cancel-book-edit');
  if (cancelEditBtn) {
    cancelEditBtn.addEventListener('click', hideBookEditPanel);
  }

  const deleteBtn = document.getElementById('delete-book');
  if (deleteBtn) {
    deleteBtn.addEventListener('click', async () => {
      const bookIdField = document.getElementById('edit-bookid');
      if (!bookIdField || !bookIdField.value) return;

      if (!confirm('해당 도서를 삭제하시겠습니까? 관련 주문 데이터는 유지됩니다.')) {
        return;
      }

      try {
        toggleLoadingSpinner(true);
        await fetchAPI(`/api/books?action=delete&id=${bookIdField.value}`, { method: 'DELETE' });
        showToast('도서가 삭제되었습니다.', 'success');
        hideBookEditPanel();
        await Promise.all([loadPublishers(), loadBooks()]);
      } catch (error) {
        alert(`도서 삭제 중 오류가 발생했습니다: ${error.message}`);
      } finally {
        toggleLoadingSpinner(false);
      }
    });
  }

  // 페이지네이션 이벤트 리스너
  const firstPageBtn = document.getElementById('btn-first-page');
  if (firstPageBtn) {
    firstPageBtn.addEventListener('click', () => goToPage(1));
  }

  const prevPageBtn = document.getElementById('btn-prev-page');
  if (prevPageBtn) {
    prevPageBtn.addEventListener('click', () => goToPage(currentPage - 1));
  }

  const nextPageBtn = document.getElementById('btn-next-page');
  if (nextPageBtn) {
    nextPageBtn.addEventListener('click', () => goToPage(currentPage + 1));
  }

  const lastPageBtn = document.getElementById('btn-last-page');
  if (lastPageBtn) {
    lastPageBtn.addEventListener('click', () => goToPage(totalPages));
  }

  const pageSizeSelect = document.getElementById('page-size-select');
  if (pageSizeSelect) {
    pageSizeSelect.addEventListener('change', (event) => {
      pageSize = parseInt(event.target.value, 10);
      currentPage = 1; // 페이지 크기 변경 시 첫 페이지로
      loadBooks();
    });
  }

  // 실시간 검색 (Debounce 적용)
  const debouncedSearch = debounce(() => {
    bookFilters.title = valueOrEmpty('filter-title');
    bookFilters.publisher = valueOrEmpty('filter-publisher');
    bookFilters.minPrice = numericOrEmpty('filter-min-price');
    bookFilters.maxPrice = numericOrEmpty('filter-max-price');
    currentPage = 1;
    loadBooks();
  }, 500); // 500ms 대기

  const titleInput = document.getElementById('filter-title');
  if (titleInput) {
    titleInput.addEventListener('input', debouncedSearch);
  }

  const publisherInput = document.getElementById('filter-publisher');
  if (publisherInput) {
    publisherInput.addEventListener('input', debouncedSearch);
  }

  const minPriceInput = document.getElementById('filter-min-price');
  if (minPriceInput) {
    minPriceInput.addEventListener('input', debouncedSearch);
  }

  const maxPriceInput = document.getElementById('filter-max-price');
  if (maxPriceInput) {
    maxPriceInput.addEventListener('input', debouncedSearch);
  }
}

async function loadBooks() {
  try {
    toggleLoadingSpinner(true);
    const params = new URLSearchParams({
      action: 'list',
      sortBy: bookSort.column,
      direction: bookSort.direction,
      page: currentPage,
      pageSize: pageSize
    });

    if (bookFilters.title) {
      params.set('title', bookFilters.title);
    }
    if (bookFilters.publisher) {
      params.set('publisher', bookFilters.publisher);
    }
    if (bookFilters.minPrice) {
      params.set('priceMin', bookFilters.minPrice);
    }
    if (bookFilters.maxPrice) {
      params.set('priceMax', bookFilters.maxPrice);
    }

    const response = await fetchAPI(`/api/books?${params.toString()}`);

    // PageResponse 처리
    if (response.data && response.data.items) {
      booksCache = response.data.items || [];
      totalPages = response.data.totalPages || 1;
      totalItems = response.data.totalItems || 0;
      currentPage = response.data.page || 1;
    } else {
      // 기존 방식 (하위 호환성)
      booksCache = response.data || [];
      totalPages = 1;
      totalItems = booksCache.length;
    }

    renderBookTable();
    updateBookCount();
    updatePaginationUI();
  } catch (error) {
    const tbody = document.getElementById('book-table-body');
    if (tbody) {
      tbody.innerHTML = `
        <tr>
          <td colspan="5" class="py-12 text-center text-red-300">
            도서 목록을 불러오는 중 오류가 발생했습니다.<br>
            <button class="btn-outline mt-4" onclick="loadBooks()">다시 시도</button>
          </td>
        </tr>
      `;
    }
  } finally {
    toggleLoadingSpinner(false);
  }
}

async function loadPublishers() {
  try {
    const response = await fetchAPI('/api/books?action=publishers');
    const publishers = response.data || [];
    const select = document.getElementById('publisher-filter');
    if (!select) return;

    const options = ['<option value="">전체</option>']
      .concat(publishers.map((publisher) => `<option value="${escapeHtml(publisher)}">${escapeHtml(publisher)}</option>`));
    select.innerHTML = options.join('');
    if (bookFilters.publisher) {
      select.value = bookFilters.publisher;
    }
  } catch (error) {
    console.error('출판사 목록 로드 실패:', error);
  }
}

function renderBookTable() {
  const tbody = document.getElementById('book-table-body');
  if (!tbody) return;

  if (booksCache.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5" class="py-12 text-center text-slate-400">조건에 맞는 도서가 없습니다.</td>
      </tr>
    `;
    return;
  }

  const isAdminPage = Boolean(document.getElementById('book-edit-panel'));

  tbody.innerHTML = booksCache.map((book) => {
    const baseColumns = `
      <td class="whitespace-nowrap">${book.bookid}</td>
      <td>${escapeHtml(book.bookname)}</td>
      <td>${escapeHtml(book.publisher)}</td>
      <td class="whitespace-nowrap">${formatCurrency(book.price)}</td>
    `;

    if (!isAdminPage) {
      return `
        <tr>
          ${baseColumns}
          <td class="whitespace-nowrap"><a class="btn-outline" href="book-detail.html?id=${book.bookid}">상세</a></td>
        </tr>
      `;
    }

    return `
      <tr>
        ${baseColumns}
        <td class="whitespace-nowrap space-x-2">
          <a class="btn-ghost" href="../book-detail.html?id=${book.bookid}" target="_blank">상세</a>
          <button class="btn-outline" data-action="edit" data-id="${book.bookid}">수정</button>
        </td>
      </tr>
    `;
  }).join('');

  if (isAdminPage) {
    tbody.querySelectorAll('button[data-action="edit"]').forEach((button) => {
      button.addEventListener('click', () => {
        const bookId = parseInt(button.dataset.id || '0', 10);
        const book = booksCache.find((item) => item.bookid === bookId);
        if (!book) return;
        showBookEditPanel(book);
      });
    });
  }
}

function updateBookCount() {
  const countElement = document.getElementById('book-count');
  if (countElement) {
    countElement.textContent = `총 ${totalItems}권`;
  }
}

function goToPage(page) {
  if (page < 1 || page > totalPages) return;
  currentPage = page;
  loadBooks();
}

function updatePaginationUI() {
  // 페이지 정보 업데이트
  const pageInfo = document.getElementById('page-info');
  if (pageInfo) {
    pageInfo.textContent = `페이지 ${currentPage} / ${totalPages}`;
  }

  const itemsInfo = document.getElementById('items-info');
  if (itemsInfo) {
    itemsInfo.textContent = `전체 ${totalItems}건`;
  }

  // 버튼 활성화/비활성화
  const firstPageBtn = document.getElementById('btn-first-page');
  const prevPageBtn = document.getElementById('btn-prev-page');
  const nextPageBtn = document.getElementById('btn-next-page');
  const lastPageBtn = document.getElementById('btn-last-page');

  if (firstPageBtn) firstPageBtn.disabled = currentPage === 1;
  if (prevPageBtn) prevPageBtn.disabled = currentPage === 1;
  if (nextPageBtn) nextPageBtn.disabled = currentPage === totalPages;
  if (lastPageBtn) lastPageBtn.disabled = currentPage === totalPages;

  // 페이지 번호 버튼 렌더링
  renderPageNumbers();
}

function renderPageNumbers() {
  const container = document.getElementById('page-numbers');
  if (!container) return;

  const maxButtons = 5; // 최대 표시할 페이지 버튼 수
  let startPage = Math.max(1, currentPage - Math.floor(maxButtons / 2));
  let endPage = Math.min(totalPages, startPage + maxButtons - 1);

  // startPage 조정 (endPage가 totalPages에 가까울 때)
  if (endPage - startPage + 1 < maxButtons) {
    startPage = Math.max(1, endPage - maxButtons + 1);
  }

  const buttons = [];

  // 첫 페이지 버튼 (startPage가 1이 아닐 때)
  if (startPage > 1) {
    buttons.push(`
      <button class="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 transition text-sm font-medium" onclick="goToPage(1)">
        1
      </button>
    `);
    if (startPage > 2) {
      buttons.push(`<span class="px-2 text-slate-400">...</span>`);
    }
  }

  // 페이지 번호 버튼들
  for (let i = startPage; i <= endPage; i++) {
    const isActive = i === currentPage;
    buttons.push(`
      <button
        class="px-3 py-1.5 rounded-lg transition text-sm font-medium ${
          isActive
            ? 'bg-primary-500 text-white'
            : 'bg-white/5 hover:bg-white/10'
        }"
        onclick="goToPage(${i})"
        ${isActive ? 'disabled' : ''}
      >
        ${i}
      </button>
    `);
  }

  // 마지막 페이지 버튼 (endPage가 totalPages가 아닐 때)
  if (endPage < totalPages) {
    if (endPage < totalPages - 1) {
      buttons.push(`<span class="px-2 text-slate-400">...</span>`);
    }
    buttons.push(`
      <button class="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 transition text-sm font-medium" onclick="goToPage(${totalPages})">
        ${totalPages}
      </button>
    `);
  }

  container.innerHTML = buttons.join('');
}

function updateBookSortIndicators() {
  document.querySelectorAll('#book-table thead th.sortable').forEach((th) => {
    th.classList.remove('sorted-asc', 'sorted-desc');
    if (th.dataset.column === bookSort.column) {
      th.classList.add(bookSort.direction === 'asc' ? 'sorted-asc' : 'sorted-desc');
    }
  });
}

function showBookEditPanel(book) {
  const panel = document.getElementById('book-edit-panel');
  if (!panel) return;

  document.getElementById('edit-bookid').value = book.bookid;
  document.getElementById('edit-bookname').value = book.bookname;
  document.getElementById('edit-publisher').value = book.publisher;
  document.getElementById('edit-price').value = book.price;

  const detailLink = document.getElementById('view-detail');
  if (detailLink) {
    const base = detailLink.dataset.base || 'book-detail.html';
    detailLink.href = `${base}?id=${book.bookid}`;
  }

  panel.classList.remove('hidden');
  panel.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function hideBookEditPanel() {
  const panel = document.getElementById('book-edit-panel');
  if (panel) {
    panel.classList.add('hidden');
  }
}

// -------------------------
// Book detail page handlers
// -------------------------

async function initBookDetailPage() {
  checkLoginStatus();

  const bookId = getUrlParameter('id');
  if (!bookId) {
    alert('잘못된 접근입니다. 도서 ID가 없습니다.');
    window.location.href = 'books.html';
    return;
  }

  await loadBookDetail(bookId);
}

async function loadBookDetail(bookId) {
  try {
    toggleLoadingSpinner(true);
    const [bookResponse, statsResponse] = await Promise.all([
      fetchAPI(`/api/books?action=detail&id=${bookId}`),
      fetchAPI(`/api/books?action=stats&id=${bookId}`)
    ]);

    renderBookDetail(bookResponse.data, statsResponse.data);
  } catch (error) {
    const detailElement = document.getElementById('book-detail');
    if (detailElement) {
      detailElement.innerHTML = `
        <div class="empty-message text-red-300">
          도서 정보를 불러오는 중 오류가 발생했습니다.<br>
          <button class="btn-outline mt-4" onclick="loadBookDetail(${bookId})">다시 시도</button>
        </div>
      `;
    }
  } finally {
    toggleLoadingSpinner(false);
  }
}

function renderBookDetail(book, stats) {
  const detailElement = document.getElementById('book-detail');
  if (!detailElement) return;

  detailElement.innerHTML = `
    <div class="glass-panel p-8 space-y-8">
      <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
        <div>
          <p class="text-sm uppercase tracking-widest text-primary-300 mb-2">Book #${book.bookid}</p>
          <h1 class="text-3xl font-bold">${escapeHtml(book.bookname)}</h1>
          <p class="text-slate-300 mt-2">🏢 ${escapeHtml(book.publisher)}</p>
        </div>
        <div class="text-right">
          <p class="text-sm text-slate-300">정가</p>
          <p class="text-3xl font-bold text-primary-200">${formatCurrency(book.price)}</p>
        </div>
      </div>

      <section class="grid md:grid-cols-2 gap-6">
        <form id="order-form" class="glass-panel-secondary p-6 space-y-4">
          <h2 class="text-xl font-semibold">이 도서로 주문하기</h2>
          <p class="text-sm text-slate-400">로그인 상태에서 주문하면 Orders 테이블에 INSERT 됩니다.</p>
          <label class="form-label">판매가 (원)
            <input type="number" id="saleprice" class="input" min="1000" step="100" max="${book.price}" value="${book.price}" required>
          </label>
          <button type="submit" class="btn-primary w-full">주문 페이지로 이동</button>
        </form>

        <div class="glass-panel-secondary p-6 space-y-4">
          <h2 class="text-xl font-semibold">판매 통계</h2>
          <dl class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <dt class="text-slate-400">총 판매 수량</dt>
              <dd class="text-lg font-semibold">${stats.salesCount || 0}건</dd>
            </div>
            <div>
              <dt class="text-slate-400">평균 판매가</dt>
              <dd class="text-lg font-semibold">${stats.avgPrice ? formatCurrency(Math.round(stats.avgPrice)) : '-'} </dd>
            </div>
            <div>
              <dt class="text-slate-400">최고 판매가</dt>
              <dd class="text-lg font-semibold">${stats.maxPrice ? formatCurrency(stats.maxPrice) : '-'}</dd>
            </div>
            <div>
              <dt class="text-slate-400">최저 판매가</dt>
              <dd class="text-lg font-semibold">${stats.minPrice ? formatCurrency(stats.minPrice) : '-'}</dd>
            </div>
          </dl>
          <a href="books.html" class="btn-outline w-full">목록으로 돌아가기</a>
        </div>
      </section>
    </div>
  `;

  const orderForm = document.getElementById('order-form');
  if (orderForm) {
    orderForm.addEventListener('submit', (event) => handleOrderSubmit(event, book));
  }
}

function handleOrderSubmit(event, book) {
  event.preventDefault();
  if (!requireLogin(`book-detail.html?id=${book.bookid}`)) {
    return;
  }

  const saleInput = document.getElementById('saleprice');
  const saleprice = parseInt(saleInput.value, 10);

  if (Number.isNaN(saleprice) || saleprice <= 0) {
    alert('판매가는 0보다 커야 합니다.');
    return;
  }

  if (saleprice > book.price) {
    alert('판매가는 정가를 초과할 수 없습니다.');
    return;
  }

  window.location.href = `order.html?bookid=${book.bookid}&saleprice=${saleprice}`;
}

function valueOrEmpty(id) {
  const input = document.getElementById(id);
  return input ? input.value.trim() : '';
}

function numericOrEmpty(id) {
  const value = valueOrEmpty(id);
  if (!value) return '';
  const parsed = parseInt(value, 10);
  return Number.isNaN(parsed) ? '' : parsed.toString();
}

function escapeHtml(value) {
  if (value === null || value === undefined) return '';
  return value
    .toString()
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function runInitialization() {
  const path = window.location.pathname;

  if (path.includes('books-admin')) { // ✅ 관리자 페이지 먼저 확인
    initBooksPage();
  } else if (path.includes('books.html')) {
    initBooksPage();
  } else if (path.includes('book-detail.html')) {
    initBookDetailPage();
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', runInitialization);
} else {
  runInitialization();
}

// if (document.readyState === 'loading') {
//   document.addEventListener('DOMContentLoaded', () => {
//     if (window.location.pathname.endsWith('books.html')) {
//       initBooksPage();
//     } else if (window.location.pathname.endsWith('book-detail.html')) {
//       initBookDetailPage();
//     }
//   });
// } else {
//   if (window.location.pathname.endsWith('books.html')) {
//     initBooksPage();
//   } else if (window.location.pathname.endsWith('book-detail.html')) {
//     initBookDetailPage();
//   }
// }
