let booksCache = [];
let bookSort = { column: 'bookid', direction: 'asc' };
let bookFilters = { title: '', publisher: '', minPrice: '', maxPrice: '' };

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
}

async function loadBooks() {
  try {
    toggleLoadingSpinner(true);
    const params = new URLSearchParams({
      action: 'list',
      sortBy: bookSort.column,
      direction: bookSort.direction
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
    booksCache = response.data || [];
    renderBookTable();
    updateBookCount();
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
    countElement.textContent = `총 ${booksCache.length}권`;
  }
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
