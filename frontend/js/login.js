let loginCustomers = [];
let loginSort = { column: 'name', direction: 'asc' };
let loginFilter = '';

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

      updateLoginSortIndicators();
      loadLoginCustomers();
    });
  });

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
      direction: loginSort.direction
    });

    if (loginFilter) {
      params.set('name', loginFilter);
    }

    const response = await fetchAPI(`/api/customers?${params.toString()}`);
    loginCustomers = response.data || [];
    renderLoginTable();
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
