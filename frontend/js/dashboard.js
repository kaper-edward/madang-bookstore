const dashboardSortStates = {
  customers: { column: 'totalAmount', direction: 'desc' },
  publishers: { column: 'totalRevenue', direction: 'desc' },
  books: { column: 'salesCount', direction: 'desc' },
  recent: { column: 'orderdate', direction: 'desc' }
};

async function initDashboard() {
  checkLoginStatus();
  setupSortableHeaders();
  updateDashboardSortIndicators();
  await Promise.all([
    loadOverviewStats(),
    loadBestsellers(),
    loadCustomerStats(),
    loadPublisherStats(),
    loadBookStats(),
    loadRecentOrders()
  ]);
}

function setupSortableHeaders() {
  const mapping = {
    'customer-stats-table': 'customers',
    'publisher-stats-table': 'publishers',
    'book-stats-table': 'books',
    'recent-orders-table': 'recent'
  };

  Object.entries(mapping).forEach(([tableId, key]) => {
    const table = document.getElementById(tableId);
    if (!table) return;

    table.querySelectorAll('th.sortable').forEach((th) => {
      th.addEventListener('click', () => {
        const column = th.dataset.column;
        if (!column) return;

        const state = dashboardSortStates[key];
        if (state.column === column) {
          state.direction = state.direction === 'asc' ? 'desc' : 'asc';
        } else {
          state.column = column;
          state.direction = 'asc';
        }

        updateDashboardSortIndicators();

        switch (key) {
          case 'customers':
            loadCustomerStats();
            break;
          case 'publishers':
            loadPublisherStats();
            break;
          case 'books':
            loadBookStats();
            break;
          case 'recent':
            loadRecentOrders();
            break;
          default:
            break;
        }
      });
    });
  });
}

function updateDashboardSortIndicators() {
  const mapping = {
    'customer-stats-table': dashboardSortStates.customers,
    'publisher-stats-table': dashboardSortStates.publishers,
    'book-stats-table': dashboardSortStates.books,
    'recent-orders-table': dashboardSortStates.recent
  };

  Object.entries(mapping).forEach(([tableId, state]) => {
    const table = document.getElementById(tableId);
    if (!table) return;
    table.querySelectorAll('th.sortable').forEach((th) => {
      th.classList.remove('sorted-asc', 'sorted-desc');
      if (th.dataset.column === state.column) {
        th.classList.add(state.direction === 'asc' ? 'sorted-asc' : 'sorted-desc');
      }
    });
  });
}

async function loadOverviewStats() {
  try {
    const response = await fetchAPI('/api/stats?action=overview');
    renderOverviewStats(response.data);
  } catch (error) {
    const element = document.getElementById('overview-stats');
    if (element) {
      element.innerHTML = `<div class="empty-message text-red-300">통계를 불러오지 못했습니다: ${escapeHtml(error.message)}</div>`;
    }
  }
}

function renderOverviewStats(stats) {
  const element = document.getElementById('overview-stats');
  if (!element) return;

  element.innerHTML = `
    <div class="grid md:grid-cols-5 gap-4">
      ${renderStatCard('총 도서', `${stats.totalBooks || 0}권`, '📚')}
      ${renderStatCard('총 고객', `${stats.totalCustomers || 0}명`, '👥')}
      ${renderStatCard('총 주문', `${stats.totalOrders || 0}건`, '🧾')}
      ${renderStatCard('총 매출', formatCurrency(stats.totalRevenue || 0), '💰')}
      ${renderStatCard('평균 판매가', formatCurrency(Math.round(stats.avgSalePrice || 0)), '📊')}
    </div>
  `;
}

function renderStatCard(label, value, icon) {
  return `
    <div class="glass-panel-secondary p-6 rounded-xl">
      <p class="text-sm text-slate-400">${icon} ${label}</p>
      <p class="text-2xl font-semibold mt-2">${value}</p>
    </div>
  `;
}

async function loadBestsellers() {
  try {
    const response = await fetchAPI('/api/stats?action=bestsellers&limit=5');
    renderBestsellers(response.data || []);
  } catch (error) {
    const grid = document.getElementById('bestseller-grid');
    if (grid) {
      grid.innerHTML = `<div class="glass-panel-secondary p-6 text-center text-red-300">베스트셀러 데이터를 불러오지 못했습니다.</div>`;
    }
  }
}

function renderBestsellers(bestsellers) {
  const grid = document.getElementById('bestseller-grid');
  if (!grid) return;

  if (bestsellers.length === 0) {
    grid.innerHTML = `<div class="glass-panel-secondary p-6 text-center text-slate-400">판매 데이터가 없습니다.</div>`;
    return;
  }

  grid.innerHTML = bestsellers.map((book, index) => `
    <div class="glass-panel-secondary p-6 rounded-xl space-y-2">
      <p class="text-sm text-slate-400">${index + 1}위</p>
      <p class="font-semibold">${escapeHtml(book.bookname)}</p>
      <p class="text-sm text-slate-300">${escapeHtml(book.publisher)}</p>
      <p class="text-sm text-primary-200">${book.salesCount}건 판매</p>
    </div>
  `).join('');
}

async function loadCustomerStats() {
  await loadStatsTable('customer-stats-table', '/api/stats?action=customers', dashboardSortStates.customers, (row) => `
    <tr>
      <td>${escapeHtml(row.name)}</td>
      <td>${row.orderCount}</td>
      <td>${formatCurrency(row.totalAmount)}</td>
      <td>${formatCurrency(Math.round(row.avgAmount || 0))}</td>
    </tr>
  `);
}

async function loadPublisherStats() {
  await loadStatsTable('publisher-stats-table', '/api/stats?action=publishers', dashboardSortStates.publishers, (row) => `
    <tr>
      <td>${escapeHtml(row.publisher)}</td>
      <td>${row.bookCount}</td>
      <td>${row.salesCount}</td>
      <td>${formatCurrency(row.totalRevenue)}</td>
    </tr>
  `);
}

async function loadBookStats() {
  await loadStatsTable('book-stats-table', '/api/stats?action=books', dashboardSortStates.books, (row) => `
    <tr>
      <td>${escapeHtml(row.bookname)}</td>
      <td>${escapeHtml(row.publisher)}</td>
      <td>${formatCurrency(row.price)}</td>
      <td>${row.salesCount}</td>
      <td>${formatCurrency(Math.round(row.avgSalePrice || 0))}</td>
    </tr>
  `);
}

async function loadRecentOrders() {
  await loadStatsTable('recent-orders-table', '/api/stats?action=recent&limit=10', dashboardSortStates.recent, (row) => `
    <tr>
      <td>${formatDate(row.orderdate)}</td>
      <td>${escapeHtml(row.customerName)}</td>
      <td>${escapeHtml(row.bookname)}</td>
      <td>${formatCurrency(row.saleprice)}</td>
    </tr>
  `);
}

async function loadStatsTable(tableId, baseEndpoint, sortState, rowTemplate) {
  const table = document.getElementById(tableId);
  if (!table) return;

  const tbody = table.querySelector('tbody');
  if (!tbody) return;

  try {
    const params = new URLSearchParams({
      sortBy: sortState.column,
      direction: sortState.direction
    });

    const separator = baseEndpoint.includes('?') ? '&' : '?';
    const endpoint = `${baseEndpoint}${separator}${params.toString()}`;
    const response = await fetchAPI(endpoint);
    const rows = response.data || [];

    if (rows.length === 0) {
      tbody.innerHTML = `<tr><td colspan="${table.querySelectorAll('th').length}" class="py-8 text-center text-slate-400">데이터가 없습니다.</td></tr>`;
      return;
    }

    tbody.innerHTML = rows.map(rowTemplate).join('');
  } catch (error) {
    tbody.innerHTML = `<tr><td colspan="${table.querySelectorAll('th').length}" class="py-8 text-center text-red-300">데이터를 불러오지 못했습니다.</td></tr>`;
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
  document.addEventListener('DOMContentLoaded', initDashboard);
} else {
  initDashboard();
}
