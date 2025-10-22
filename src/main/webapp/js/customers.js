let customersCache = [];
let customerSort = { column: 'custid', direction: 'asc' };
let customerFilters = { name: '', phone: '', address: '' };
let customerCurrentPage = 1;
let customerPageSize = 10;
let customerTotalPages = 1;
let customerTotalItems = 0;

async function initCustomersAdminPage() {
  renderAdminHeader('customers');
  renderAdminFooter();
  setupCustomerAdminEvents();
  setupCustomerPaginationEvents();
  updateCustomerSortIndicators();
  await loadCustomers();
}

function setupCustomerAdminEvents() {
  document.querySelectorAll('#customer-table thead th.sortable').forEach((th) => {
    th.addEventListener('click', () => {
      const column = th.dataset.column;
      if (!column) return;

      if (customerSort.column === column) {
        customerSort.direction = customerSort.direction === 'asc' ? 'desc' : 'asc';
      } else {
        customerSort.column = column;
        customerSort.direction = 'asc';
      }

      customerCurrentPage = 1;
      updateCustomerSortIndicators();
      loadCustomers();
    });
  });

  const filterForm = document.getElementById('customer-filter-form');
  if (filterForm) {
    filterForm.addEventListener('submit', (event) => {
      event.preventDefault();
      customerFilters = {
        name: valueOrEmpty('filter-name'),
        phone: valueOrEmpty('filter-phone'),
        address: valueOrEmpty('filter-address')
      };
      customerCurrentPage = 1;
      loadCustomers();
    });
  }

  const resetButton = document.getElementById('reset-search');
  if (resetButton) {
    resetButton.addEventListener('click', () => {
      customerFilters = { name: '', phone: '', address: '' };
      ['filter-name', 'filter-phone', 'filter-address'].forEach((id) => {
        const input = document.getElementById(id);
        if (input) input.value = '';
      });
      customerCurrentPage = 1;
      loadCustomers();
    });
  }

  // 모달 이벤트
  const openCustomerCreateModal = document.getElementById('open-customer-create-modal');
  const closeCustomerCreateModal = document.getElementById('close-customer-create-modal');
  const customerCreateModal = document.getElementById('customer-create-modal');

  if (openCustomerCreateModal && customerCreateModal) {
    openCustomerCreateModal.addEventListener('click', () => {
      customerCreateModal.classList.add('active');
    });
  }

  if (closeCustomerCreateModal && customerCreateModal) {
    closeCustomerCreateModal.addEventListener('click', () => {
      customerCreateModal.classList.remove('active');
    });
  }

  if (customerCreateModal) {
    customerCreateModal.addEventListener('click', (e) => {
      if (e.target === customerCreateModal) {
        customerCreateModal.classList.remove('active');
      }
    });
  }

  const createForm = document.getElementById('customer-create-form');
  if (createForm) {
    createForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      const formData = new FormData(createForm);
      const payload = Object.fromEntries(formData.entries());

      try {
        toggleLoadingSpinner(true);
        await fetchAPI('/api/customers?action=create', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        showToast('고객이 등록되었습니다.', 'success');
        createForm.reset();
        // 모달 닫기
        const customerCreateModal = document.getElementById('customer-create-modal');
        if (customerCreateModal) customerCreateModal.classList.remove('active');
        await loadCustomers();
      } catch (error) {
        alert(`고객 등록 중 오류가 발생했습니다: ${error.message}`);
      } finally {
        toggleLoadingSpinner(false);
      }
    });
  }

  const editForm = document.getElementById('customer-edit-form');
  if (editForm) {
    editForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      const formData = new FormData(editForm);
      const payload = Object.fromEntries(formData.entries());
      payload.custid = parseInt(payload.custid, 10);

      try {
        toggleLoadingSpinner(true);
        await fetchAPI('/api/customers?action=update', {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
        showToast('고객 정보가 수정되었습니다.', 'success');
        hideCustomerEditPanel();
        await loadCustomers();
      } catch (error) {
        alert(`고객 수정 중 오류가 발생했습니다: ${error.message}`);
      } finally {
        toggleLoadingSpinner(false);
      }
    });
  }

  const cancelEditBtn = document.getElementById('cancel-edit');
  if (cancelEditBtn) {
    cancelEditBtn.addEventListener('click', hideCustomerEditPanel);
  }

  const deleteBtn = document.getElementById('delete-customer');
  if (deleteBtn) {
    deleteBtn.addEventListener('click', async () => {
      const custIdField = document.getElementById('edit-custid');
      if (!custIdField || !custIdField.value) return;

      if (!confirm('해당 고객을 삭제하시겠습니까? 주문 데이터는 유지됩니다.')) {
        return;
      }

      try {
        toggleLoadingSpinner(true);
        await fetchAPI(`/api/customers?action=delete&id=${custIdField.value}`, { method: 'DELETE' });
        showToast('고객이 삭제되었습니다.', 'success');
        hideCustomerEditPanel();
        await loadCustomers();
      } catch (error) {
        alert(`고객 삭제 중 오류가 발생했습니다: ${error.message}`);
      } finally {
        toggleLoadingSpinner(false);
      }
    });
  }
}

function setupCustomerPaginationEvents() {
  const btnFirst = document.getElementById('customer-btn-first');
  const btnPrev = document.getElementById('customer-btn-prev');
  const btnNext = document.getElementById('customer-btn-next');
  const btnLast = document.getElementById('customer-btn-last');
  const pageSizeSelect = document.getElementById('customer-page-size');

  if (btnFirst) btnFirst.addEventListener('click', () => goToCustomerPage(1));
  if (btnPrev) btnPrev.addEventListener('click', () => goToCustomerPage(customerCurrentPage - 1));
  if (btnNext) btnNext.addEventListener('click', () => goToCustomerPage(customerCurrentPage + 1));
  if (btnLast) btnLast.addEventListener('click', () => goToCustomerPage(customerTotalPages));

  if (pageSizeSelect) {
    pageSizeSelect.addEventListener('change', (e) => {
      customerPageSize = parseInt(e.target.value);
      customerCurrentPage = 1;
      loadCustomers();
    });
  }
}

async function loadCustomers() {
  try {
    toggleLoadingSpinner(true);
    const params = new URLSearchParams({
      action: 'list',
      sortBy: customerSort.column,
      direction: customerSort.direction,
      page: customerCurrentPage,
      pageSize: customerPageSize
    });

    if (customerFilters.name) {
      params.set('name', customerFilters.name);
    }
    if (customerFilters.phone) {
      params.set('phone', customerFilters.phone);
    }
    if (customerFilters.address) {
      params.set('address', customerFilters.address);
    }

    const response = await fetchAPI(`/api/customers?${params.toString()}`);

    if (response.data && response.data.items) {
      customersCache = response.data.items || [];
      customerTotalPages = response.data.totalPages || 1;
      customerTotalItems = response.data.totalItems || 0;
      customerCurrentPage = response.data.page || 1;
    } else {
      customersCache = response.data || [];
    }

    renderCustomerTable();
    updateCustomerPaginationUI();
  } catch (error) {
    const tbody = document.getElementById('customer-table-body');
    if (tbody) {
      tbody.innerHTML = `<tr><td colspan="5" class="py-12 text-center text-red-300">고객 목록을 불러오는 중 오류가 발생했습니다.</td></tr>`;
    }
  } finally {
    toggleLoadingSpinner(false);
  }
}

function renderCustomerTable() {
  const tbody = document.getElementById('customer-table-body');
  if (!tbody) return;

  if (customersCache.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="py-12 text-center text-slate-400">조건에 맞는 고객이 없습니다.</td></tr>`;
    return;
  }

  tbody.innerHTML = customersCache.map((customer) => `
    <tr>
      <td class="whitespace-nowrap">${customer.custid}</td>
      <td>${escapeHtml(customer.name)}</td>
      <td>${escapeHtml(customer.address)}</td>
      <td>${escapeHtml(customer.phone)}</td>
      <td class="space-x-2 whitespace-nowrap">
        <button class="btn-outline" data-action="edit" data-id="${customer.custid}">수정</button>
        <button class="btn-danger" data-action="delete" data-id="${customer.custid}">삭제</button>
      </td>
    </tr>
  `).join('');

  tbody.querySelectorAll('button[data-action="edit"]').forEach((button) => {
    button.addEventListener('click', () => {
      const custId = parseInt(button.dataset.id || '0', 10);
      const customer = customersCache.find((item) => item.custid === custId);
      if (!customer) return;
      showCustomerEditPanel(customer);
    });
  });

  tbody.querySelectorAll('button[data-action="delete"]').forEach((button) => {
    button.addEventListener('click', async () => {
      const custId = parseInt(button.dataset.id || '0', 10);
      await deleteCustomer(custId);
    });
  });
}

async function deleteCustomer(custId) {
  if (!confirm('해당 고객을 삭제하시겠습니까? 주문 데이터는 유지됩니다.')) {
    return;
  }

  try {
    toggleLoadingSpinner(true);
    await fetchAPI(`/api/customers?action=delete&id=${custId}`, { method: 'DELETE' });
    showToast('고객이 삭제되었습니다.', 'success');
    hideCustomerEditPanel();
    await loadCustomers();
  } catch (error) {
    alert(`고객 삭제 중 오류가 발생했습니다: ${error.message}`);
  } finally {
    toggleLoadingSpinner(false);
  }
}

function showCustomerEditPanel(customer) {
  const panel = document.getElementById('customer-edit-panel');
  if (!panel) return;

  document.getElementById('edit-custid').value = customer.custid;
  document.getElementById('edit-name').value = customer.name;
  document.getElementById('edit-address').value = customer.address;
  document.getElementById('edit-phone').value = customer.phone;

  panel.classList.remove('hidden');
  panel.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function hideCustomerEditPanel() {
  const panel = document.getElementById('customer-edit-panel');
  if (panel) {
    panel.classList.add('hidden');
  }
}

function updateCustomerSortIndicators() {
  document.querySelectorAll('#customer-table thead th.sortable').forEach((th) => {
    th.classList.remove('sorted-asc', 'sorted-desc');
    if (th.dataset.column === customerSort.column) {
      th.classList.add(customerSort.direction === 'asc' ? 'sorted-asc' : 'sorted-desc');
    }
  });
}

function goToCustomerPage(page) {
  if (page < 1 || page > customerTotalPages || page === customerCurrentPage) return;
  customerCurrentPage = page;
  loadCustomers();
}

function updateCustomerPaginationUI() {
  const pageInfo = document.getElementById('customer-page-info');
  const itemsInfo = document.getElementById('customer-items-info');
  const btnFirst = document.getElementById('customer-btn-first');
  const btnPrev = document.getElementById('customer-btn-prev');
  const btnNext = document.getElementById('customer-btn-next');
  const btnLast = document.getElementById('customer-btn-last');

  if (pageInfo) pageInfo.textContent = `페이지 ${customerCurrentPage} / ${customerTotalPages}`;
  if (itemsInfo) itemsInfo.textContent = `전체 ${customerTotalItems}건`;

  if (btnFirst) btnFirst.disabled = customerCurrentPage === 1;
  if (btnPrev) btnPrev.disabled = customerCurrentPage === 1;
  if (btnNext) btnNext.disabled = customerCurrentPage === customerTotalPages;
  if (btnLast) btnLast.disabled = customerCurrentPage === customerTotalPages;

  renderCustomerPageNumbers();
}

function renderCustomerPageNumbers() {
  const container = document.getElementById('customer-page-numbers');
  if (!container) return;

  container.innerHTML = '';

  const maxButtons = 5;
  let startPage = Math.max(1, customerCurrentPage - Math.floor(maxButtons / 2));
  let endPage = Math.min(customerTotalPages, startPage + maxButtons - 1);

  if (endPage - startPage < maxButtons - 1) {
    startPage = Math.max(1, endPage - maxButtons + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    const btn = document.createElement('button');
    btn.textContent = i;
    btn.className = i === customerCurrentPage
      ? 'px-3 py-1.5 rounded-lg bg-primary-500 text-white font-medium text-sm'
      : 'px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 transition text-sm font-medium';
    btn.addEventListener('click', () => goToCustomerPage(i));
    container.appendChild(btn);
  }
}

function valueOrEmpty(id) {
  const input = document.getElementById(id);
  return input ? input.value.trim() : '';
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
    if (window.location.pathname.includes('customers-admin')) {
      initCustomersAdminPage();
    }
  });
} else if (window.location.pathname.includes('customers-admin')) {
  initCustomersAdminPage();
}
