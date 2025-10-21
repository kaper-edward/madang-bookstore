let ordersCache = [];
let orderSort = { column: 'orderdate', direction: 'desc' };
let loggedCustomerId = null;

async function initOrderPage() {
  checkLoginStatus();

  if (!requireLogin()) {
    return;
  }

  loggedCustomerId = getCustomerId();
  const bookid = parseInt(getUrlParameter('bookid'), 10);
  const saleprice = parseInt(getUrlParameter('saleprice'), 10);

  if (!bookid || !saleprice) {
    alert('잘못된 접근입니다. 도서 정보가 없습니다.');
    window.location.href = 'books.html';
    return;
  }

  await loadOrderSummary(loggedCustomerId, bookid, saleprice);
}

async function loadOrderSummary(custid, bookid, saleprice) {
  try {
    toggleLoadingSpinner(true);
    const [customerResponse, bookResponse] = await Promise.all([
      fetchAPI(`/api/customers?action=detail&id=${custid}`),
      fetchAPI(`/api/books?action=detail&id=${bookid}`)
    ]);

    renderOrderSummary(customerResponse.data, bookResponse.data, saleprice);
  } catch (error) {
    const summaryElement = document.getElementById('order-summary');
    if (summaryElement) {
      summaryElement.innerHTML = `<div class="empty-message text-red-300">주문 정보를 불러오지 못했습니다.</div>`;
    }
  } finally {
    toggleLoadingSpinner(false);
  }
}

function renderOrderSummary(customer, book, saleprice) {
  const summaryElement = document.getElementById('order-summary');
  if (!summaryElement) return;

  const discount = Math.max(0, book.price - saleprice);
  const today = new Date().toISOString().split('T')[0];

  summaryElement.innerHTML = `
    <div class="grid md:grid-cols-2 gap-6">
      <div class="glass-panel-secondary p-6 space-y-3">
        <h2 class="text-xl font-semibold">👤 주문자</h2>
        <p><span class="text-slate-400">이름</span> ${escapeHtml(customer.name)}</p>
        <p><span class="text-slate-400">주소</span> ${escapeHtml(customer.address)}</p>
        <p><span class="text-slate-400">전화</span> ${escapeHtml(customer.phone)}</p>
      </div>
      <div class="glass-panel-secondary p-6 space-y-3">
        <h2 class="text-xl font-semibold">📖 주문 도서</h2>
        <p class="text-lg font-semibold">${escapeHtml(book.bookname)}</p>
        <p class="text-slate-300">${escapeHtml(book.publisher)}</p>
        <p class="font-semibold">정가: ${formatCurrency(book.price)}</p>
        <p class="text-success font-semibold">구매가: ${formatCurrency(saleprice)}${discount > 0 ? ` (할인 ${formatCurrency(discount)})` : ''}</p>
        <p class="text-slate-400 text-sm">주문일: ${formatDate(today)}</p>
      </div>
    </div>
    <div class="flex flex-col md:flex-row md:justify-between md:items-center gap-3 pt-6 border-t border-white/10">
      <div class="text-slate-400 text-sm">
        주문 완료 시 콘솔에 INSERT SQL이 출력됩니다.
      </div>
      <div class="flex gap-3">
        <button id="confirm-btn" class="btn-primary">주문 완료</button>
        <button id="cancel-btn" class="btn-outline">돌아가기</button>
      </div>
    </div>
  `;

  document.getElementById('confirm-btn').addEventListener('click', () => confirmOrder(customer.custid, book.bookid, saleprice));
  document.getElementById('cancel-btn').addEventListener('click', () => history.back());
}

async function confirmOrder(custid, bookid, saleprice) {
  try {
    toggleLoadingSpinner(true);
    const response = await fetchAPI('/api/orders?action=create', {
      method: 'POST',
      body: JSON.stringify({ custid, bookid, saleprice })
    });

    if (response.success) {
      showToast('주문이 완료되었습니다!', 'success');
      setTimeout(() => {
        window.location.href = 'my-orders.html';
      }, 1000);
    }
  } catch (error) {
    alert(`주문 처리 중 오류가 발생했습니다: ${error.message}`);
  } finally {
    toggleLoadingSpinner(false);
  }
}

// -------------------------
// My orders page
// -------------------------

async function initMyOrdersPage() {
  checkLoginStatus();

  if (!requireLogin()) {
    return;
  }

  loggedCustomerId = getCustomerId();
  setupOrderSortHeaders();
  updateOrderSortIndicators();
  await loadOrdersAndStats();
}

function setupOrderSortHeaders() {
  document.querySelectorAll('#order-table thead th.sortable').forEach((th) => {
    th.addEventListener('click', () => {
      const column = th.dataset.column;
      if (!column) return;

      if (orderSort.column === column) {
        orderSort.direction = orderSort.direction === 'asc' ? 'desc' : 'asc';
      } else {
        orderSort.column = column;
        orderSort.direction = 'asc';
      }

      updateOrderSortIndicators();
      loadOrdersAndStats();
    });
  });
}

async function loadOrdersAndStats() {
  if (!loggedCustomerId) return;

  try {
    toggleLoadingSpinner(true);
    const [ordersResponse, statsResponse] = await Promise.all([
      fetchAPI(`/api/orders?action=list&custid=${loggedCustomerId}&sortBy=${orderSort.column}&direction=${orderSort.direction}`),
      fetchAPI(`/api/orders?action=stats&custid=${loggedCustomerId}`)
    ]);

    ordersCache = ordersResponse.data || [];
    renderOrderStats(statsResponse.data || {});
    renderOrderTable();
  } catch (error) {
    console.error('주문 정보를 불러오는 중 오류', error);
  } finally {
    toggleLoadingSpinner(false);
  }
}

function renderOrderStats(stats) {
  const statsElement = document.getElementById('order-stats');
  if (!statsElement) return;

  statsElement.innerHTML = `
    <h2 class="text-xl font-semibold">주문 요약</h2>
    <div class="space-y-3">
      <div class="stat-pill">
        <span class="label">총 주문 수</span>
        <span class="value">${stats.orderCount || 0}건</span>
      </div>
      <div class="stat-pill">
        <span class="label">총 구매 금액</span>
        <span class="value">${formatCurrency(stats.totalAmount || 0)}</span>
      </div>
      <div class="stat-pill">
        <span class="label">평균 구매 금액</span>
        <span class="value">${formatCurrency(Math.round(stats.avgAmount || 0))}</span>
      </div>
    </div>
  `;
}

function renderOrderTable() {
  const tbody = document.getElementById('order-table-body');
  if (!tbody) return;

  if (ordersCache.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="py-12 text-center text-slate-400">주문 내역이 없습니다.</td></tr>`;
    return;
  }

  tbody.innerHTML = ordersCache.map((order) => `
    <tr>
      <td>${order.orderid}</td>
      <td>${formatDate(order.orderdate)}</td>
      <td>${escapeHtml(order.bookname)}</td>
      <td>${escapeHtml(order.publisher)}</td>
      <td>${formatCurrency(order.saleprice)}</td>
      <td class="space-x-2 whitespace-nowrap">
        <button class="btn-outline" data-action="edit" data-id="${order.orderid}">금액 수정</button>
        <button class="btn-danger" data-action="delete" data-id="${order.orderid}">취소</button>
      </td>
    </tr>
  `).join('');

  tbody.querySelectorAll('button[data-action="edit"]').forEach((button) => {
    button.addEventListener('click', () => {
      const orderId = parseInt(button.dataset.id || '0', 10);
      const order = ordersCache.find((item) => item.orderid === orderId);
      if (order) {
        showOrderEditPanel(order);
      }
    });
  });

  tbody.querySelectorAll('button[data-action="delete"]').forEach((button) => {
    button.addEventListener('click', async () => {
      const orderId = parseInt(button.dataset.id || '0', 10);
      await cancelOrder(orderId);
    });
  });
}

function showOrderEditPanel(order) {
  const panel = document.getElementById('order-edit-panel');
  if (!panel) return;

  document.getElementById('edit-orderid').value = order.orderid;
  document.getElementById('edit-custid').value = order.custid;
  document.getElementById('edit-max-price').value = order.listPrice || order.saleprice;
  document.getElementById('edit-current-price').value = formatCurrency(order.saleprice);

  const saleInput = document.getElementById('edit-saleprice');
  if (saleInput) {
    saleInput.max = order.listPrice || order.saleprice;
    saleInput.value = order.saleprice;
  }

  const help = document.getElementById('order-edit-help');
  if (help) {
    const maxPrice = order.listPrice || order.saleprice;
    help.textContent = `정가 ${formatCurrency(maxPrice)} 이하로 입력하세요.`;
  }

  panel.classList.remove('hidden');
  panel.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function hideOrderEditPanel() {
  const panel = document.getElementById('order-edit-panel');
  if (panel) panel.classList.add('hidden');
}

async function cancelOrder(orderId) {
  if (!confirm('이 주문을 취소하시겠습니까?')) {
    return;
  }

  try {
    toggleLoadingSpinner(true);
    await fetchAPI(`/api/orders?action=delete&id=${orderId}&custid=${loggedCustomerId}`, { method: 'DELETE' });
    showToast('주문이 취소되었습니다.', 'success');
    hideOrderEditPanel();
    await loadOrdersAndStats();
  } catch (error) {
    alert(`주문 취소 중 오류가 발생했습니다: ${error.message}`);
  } finally {
    toggleLoadingSpinner(false);
  }
}

function updateOrderSortIndicators() {
  document.querySelectorAll('#order-table thead th.sortable').forEach((th) => {
    th.classList.remove('sorted-asc', 'sorted-desc');
    if (th.dataset.column === orderSort.column) {
      th.classList.add(orderSort.direction === 'asc' ? 'sorted-asc' : 'sorted-desc');
    }
  });
}

// Event listeners for order edit panel buttons
if (document.getElementById('order-edit-form')) {
  document.getElementById('order-edit-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);
    const payload = Object.fromEntries(formData.entries());
    payload.orderid = parseInt(payload.orderid, 10);
    payload.custid = parseInt(payload.custid, 10);
    payload.saleprice = parseInt(payload.saleprice, 10);

    const maxPrice = parseInt(document.getElementById('edit-max-price').value, 10);
    if (payload.saleprice > maxPrice) {
      alert(`구매가는 정가 ${formatCurrency(maxPrice)}를 초과할 수 없습니다.`);
      return;
    }

    try {
      toggleLoadingSpinner(true);
      await fetchAPI('/api/orders?action=update', {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      showToast('주문 금액이 수정되었습니다.', 'success');
      hideOrderEditPanel();
      await loadOrdersAndStats();
    } catch (error) {
      alert(`주문 수정 중 오류가 발생했습니다: ${error.message}`);
    } finally {
      toggleLoadingSpinner(false);
    }
  });
}

if (document.getElementById('cancel-order-edit')) {
  document.getElementById('cancel-order-edit').addEventListener('click', hideOrderEditPanel);
}

if (document.getElementById('delete-order')) {
  document.getElementById('delete-order').addEventListener('click', async () => {
    const orderId = parseInt(document.getElementById('edit-orderid').value || '0', 10);
    if (orderId) {
      await cancelOrder(orderId);
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
  document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname.endsWith('order.html')) {
      initOrderPage();
    } else if (window.location.pathname.endsWith('my-orders.html')) {
      initMyOrdersPage();
    }
  });
} else {
  if (window.location.pathname.endsWith('order.html')) {
    initOrderPage();
  } else if (window.location.pathname.endsWith('my-orders.html')) {
    initMyOrdersPage();
  }
}
