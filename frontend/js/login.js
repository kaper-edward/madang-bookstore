let loginCustomers = [];
let loginSort = { column: 'name', direction: 'asc' };
let loginFilter = '';
let loginCurrentPage = 1;
let loginPageSize = 10;
let loginTotalPages = 1;
let loginTotalItems = 0;

async function initLoginPage() {
  setupLoginEvents();
  updateLoginSortIndicators();
  await loadLoginCustomers();
}

function setupLoginEvents() {
  const filterForm = document.getElementById('login-filter-form');
  if (filterForm) {
    filterForm.addEventListener('submit', (event) => {
      event.preventDefault();
      const input = document.getElementById('login-filter');
      loginFilter = input ? input.value.trim() : '';
      loginCurrentPage = 1;
      loadLoginCustomers();
    });
  }

  document.querySelectorAll('#login-table thead th.sortable').forEach((th) => {
    th.addEventListener('click', () => {
      const column = th.dataset.column;
      if (!column) return;

      if (loginSort.column === column) {
        loginSort.direction = loginSort.direction === 'asc' ? 'desc' : 'asc';
      } else {
        loginSort.column = column;
        loginSort.direction = 'asc';
      }

      loginCurrentPage = 1;
      updateLoginSortIndicators();
      loadLoginCustomers();
    });
  });

  // 페이지네이션 이벤트 리스너
  const btnFirst = document.getElementById('login-btn-first');
  const btnPrev = document.getElementById('login-btn-prev');
  const btnNext = document.getElementById('login-btn-next');
  const btnLast = document.getElementById('login-btn-last');
  const pageSizeSelect = document.getElementById('login-page-size');

  if (btnFirst) btnFirst.addEventListener('click', () => goToLoginPage(1));
  if (btnPrev) btnPrev.addEventListener('click', () => goToLoginPage(loginCurrentPage - 1));
  if (btnNext) btnNext.addEventListener('click', () => goToLoginPage(loginCurrentPage + 1));
  if (btnLast) btnLast.addEventListener('click', () => goToLoginPage(loginTotalPages));

  if (pageSizeSelect) {
    pageSizeSelect.addEventListener('change', (e) => {
      loginPageSize = parseInt(e.target.value);
      loginCurrentPage = 1;
      loadLoginCustomers();
    });
  }

  const signupForm = document.getElementById('signup-form');
  if (signupForm) {
    signupForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      const formData = new FormData(signupForm);
      const payload = Object.fromEntries(formData.entries());

      try {
        toggleLoadingSpinner(true);
        const response = await fetchAPI('/api/customers?action=create', {
          method: 'POST',
          body: JSON.stringify(payload)
        });

        if (response.success) {
          const customer = response.data;
          setCustomerInfo(customer.custid, customer.name);
          showToast(`${customer.name}님, 환영합니다!`, 'success');
          await loadLoginCustomers();
          signupForm.reset();
          if (!handlePostLoginRedirect()) {
            setTimeout(() => {
              window.location.href = 'index.html';
            }, 800);
          }
        }
      } catch (error) {
        alert(`회원가입 중 오류가 발생했습니다: ${error.message}`);
      } finally {
        toggleLoadingSpinner(false);
      }
    });
  }
}

async function loadLoginCustomers() {
  try {
    toggleLoadingSpinner(true);
    const params = new URLSearchParams({
      action: 'list',
      sortBy: loginSort.column,
      direction: loginSort.direction,
      page: loginCurrentPage,
      pageSize: loginPageSize
    });

    if (loginFilter) {
      params.set('name', loginFilter);
    }

    const response = await fetchAPI(`/api/customers?${params.toString()}`);

    if (response.data && response.data.items) {
      loginCustomers = response.data.items || [];
      loginTotalPages = response.data.totalPages || 1;
      loginTotalItems = response.data.totalItems || 0;
      loginCurrentPage = response.data.page || 1;
    } else {
      loginCustomers = response.data || [];
    }

    renderLoginTable();
    updateLoginPaginationUI();
  } catch (error) {
    const tbody = document.getElementById('login-table-body');
    if (tbody) {
      tbody.innerHTML = `<tr><td colspan="4" class="py-10 text-center text-red-300">고객 목록을 불러오지 못했습니다.</td></tr>`;
    }
  } finally {
    toggleLoadingSpinner(false);
  }
}

function renderLoginTable() {
  const tbody = document.getElementById('login-table-body');
  if (!tbody) return;

  if (loginCustomers.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4" class="py-10 text-center text-slate-400">조건에 맞는 고객이 없습니다.</td></tr>`;
    return;
  }

  tbody.innerHTML = loginCustomers.map((customer) => `
    <tr>
      <td>${escapeHtml(customer.name)}</td>
      <td>${escapeHtml(customer.address)}</td>
      <td>${escapeHtml(customer.phone)}</td>
      <td><button class="btn-primary" data-login-id="${customer.custid}" data-name="${escapeHtml(customer.name)}">로그인</button></td>
    </tr>
  `).join('');

  tbody.querySelectorAll('button[data-login-id]').forEach((button) => {
    button.addEventListener('click', () => {
      const custId = parseInt(button.dataset.loginId || '0', 10);
      const name = button.dataset.name || '';
      setCustomerInfo(custId, name);
      showToast(`${name}님으로 로그인되었습니다.`, 'success');
      if (!handlePostLoginRedirect()) {
        setTimeout(() => {
          window.location.href = 'index.html';
        }, 600);
      }
    });
  });
}

function updateLoginSortIndicators() {
  document.querySelectorAll('#login-table thead th.sortable').forEach((th) => {
    th.classList.remove('sorted-asc', 'sorted-desc');
    if (th.dataset.column === loginSort.column) {
      th.classList.add(loginSort.direction === 'asc' ? 'sorted-asc' : 'sorted-desc');
    }
  });
}

function goToLoginPage(page) {
  if (page < 1 || page > loginTotalPages || page === loginCurrentPage) return;
  loginCurrentPage = page;
  loadLoginCustomers();
}

function updateLoginPaginationUI() {
  const pageInfo = document.getElementById('login-page-info');
  const itemsInfo = document.getElementById('login-items-info');
  const btnFirst = document.getElementById('login-btn-first');
  const btnPrev = document.getElementById('login-btn-prev');
  const btnNext = document.getElementById('login-btn-next');
  const btnLast = document.getElementById('login-btn-last');

  if (pageInfo) pageInfo.textContent = `페이지 ${loginCurrentPage} / ${loginTotalPages}`;
  if (itemsInfo) itemsInfo.textContent = `전체 ${loginTotalItems}건`;

  if (btnFirst) btnFirst.disabled = loginCurrentPage === 1;
  if (btnPrev) btnPrev.disabled = loginCurrentPage === 1;
  if (btnNext) btnNext.disabled = loginCurrentPage === loginTotalPages;
  if (btnLast) btnLast.disabled = loginCurrentPage === loginTotalPages;

  renderLoginPageNumbers();
}

function renderLoginPageNumbers() {
  const container = document.getElementById('login-page-numbers');
  if (!container) return;

  container.innerHTML = '';

  const maxButtons = 5;
  let startPage = Math.max(1, loginCurrentPage - Math.floor(maxButtons / 2));
  let endPage = Math.min(loginTotalPages, startPage + maxButtons - 1);

  if (endPage - startPage < maxButtons - 1) {
    startPage = Math.max(1, endPage - maxButtons + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    const btn = document.createElement('button');
    btn.textContent = i;
    btn.className = i === loginCurrentPage
      ? 'px-3 py-1.5 rounded-lg bg-primary-500 text-white font-medium text-sm'
      : 'px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 transition text-sm font-medium';
    btn.addEventListener('click', () => goToLoginPage(i));
    container.appendChild(btn);
  }
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

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initLoginPage);
} else {
  initLoginPage();
}
