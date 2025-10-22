const dashboardSortStates = {
  customers: { column: 'totalAmount', direction: 'desc' },
  publishers: { column: 'totalRevenue', direction: 'desc' },
  books: { column: 'salesCount', direction: 'desc' },
  recent: { column: 'orderdate', direction: 'desc' }
};

// Chart.js 인스턴스 저장
let monthlySalesChartInstance = null;
let customerSegmentChartInstance = null;
let publisherRevenueChartInstance = null;

// 월 목록 저장
let availableMonths = [];
let availableYears = [];

// 월별 매출 추이 차트 (Line Chart)
async function loadMonthlySalesChart(year = '') {
  try {
    const response = await fetchAPI('/api/stats?action=monthly&months=12');
    let data = response.data || [];

    // 월 목록 저장 (최신순)
    availableMonths = data.map(d => d.month).reverse();

    // 년도 목록 추출 (중복 제거, 내림차순)
    const years = [...new Set(data.map(d => d.month.substring(0, 4)))].sort().reverse();
    availableYears = years;

    // 년도 선택 시 필터링
    if (year) {
      data = data.filter(d => d.month.startsWith(year));
    }

    // 드롭다운 채우기
    populateMonthSelects();
    populateYearSelect();

    const canvas = document.getElementById('monthly-sales-chart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');

    // 기존 차트 파괴
    if (monthlySalesChartInstance) {
      monthlySalesChartInstance.destroy();
    }

    const labels = data.map(d => d.month);
    const revenues = data.map(d => d.totalRevenue);
    const orderCounts = data.map(d => d.orderCount);

    monthlySalesChartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: '매출액 (원)',
            data: revenues,
            borderColor: '#818cf8',
            backgroundColor: 'rgba(129, 140, 248, 0.1)',
            borderWidth: 2,
            tension: 0.4,
            fill: true,
            yAxisID: 'y'
          },
          {
            label: '주문 수 (건)',
            data: orderCounts,
            borderColor: '#f472b6',
            backgroundColor: 'rgba(244, 114, 182, 0.1)',
            borderWidth: 2,
            tension: 0.4,
            fill: true,
            yAxisID: 'y1'
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: 'index',
          intersect: false
        },
        plugins: {
          datalabels: {
            display: false
          },
          legend: {
            labels: {
              color: '#cbd5e1',
              font: { size: 12 }
            }
          },
          tooltip: {
            backgroundColor: 'rgba(15, 23, 42, 0.9)',
            titleColor: '#cbd5e1',
            bodyColor: '#cbd5e1',
            borderColor: '#475569',
            borderWidth: 1,
            callbacks: {
              label: function(context) {
                let label = context.dataset.label || '';
                if (label) {
                  label += ': ';
                }
                if (context.parsed.y !== null) {
                  if (context.datasetIndex === 0) {
                    label += formatCurrency(context.parsed.y);
                  } else {
                    label += context.parsed.y + '건';
                  }
                }
                return label;
              }
            }
          }
        },
        scales: {
          x: {
            grid: {
              color: 'rgba(148, 163, 184, 0.1)'
            },
            ticks: {
              color: '#94a3b8'
            }
          },
          y: {
            type: 'linear',
            display: true,
            position: 'left',
            grid: {
              color: 'rgba(148, 163, 184, 0.1)'
            },
            ticks: {
              color: '#94a3b8',
              callback: function(value) {
                return formatCurrency(value);
              }
            },
            title: {
              display: true,
              text: '매출액 (원)',
              color: '#818cf8'
            }
          },
          y1: {
            type: 'linear',
            display: true,
            position: 'right',
            grid: {
              drawOnChartArea: false
            },
            ticks: {
              color: '#94a3b8',
              callback: function(value) {
                return value + '건';
              }
            },
            title: {
              display: true,
              text: '주문 수 (건)',
              color: '#f472b6'
            }
          }
        }
      }
    });
  } catch (error) {
    console.error('월별 매출 차트 로드 실패:', error);
  }
}

// 고객 세그먼트 분석 차트 (Doughnut Chart)
async function loadCustomerSegmentChart(month = '') {
  try {
    const endpoint = month
      ? `/api/stats?action=customer-segments&month=${month}`
      : '/api/stats?action=customer-segments';

    const response = await fetchAPI(endpoint);
    const data = response.data || [];

    const canvas = document.getElementById('customer-segment-chart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');

    // 기존 차트 파괴
    if (customerSegmentChartInstance) {
      customerSegmentChartInstance.destroy();
    }

    const labels = data.map(d => d.segment);
    const counts = data.map(d => d.customerCount);

    // 세그먼트별 색상 매핑 (VIP, 우수, 일반, 신규)
    const colorMap = {
      'VIP': '#fbbf24',      // 황금색 (고가치, 프리미엄)
      '우수': '#f472b6',     // 핑크색 (상위)
      '일반': '#64748b',     // 회색 (중간, 일반)
      '신규': '#60a5fa'      // 파란색 (신규)
    };
    const colors = labels.map(label => colorMap[label] || '#94a3b8');

    customerSegmentChartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: counts,
          backgroundColor: colors,
          borderColor: '#0f172a',
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          datalabels: {
            color: '#fff',
            font: {
              size: 14,
              weight: 'bold'
            },
            formatter: function(value, context) {
              const total = context.dataset.data.reduce((a, b) => a + b, 0);
              const percentage = ((value / total) * 100).toFixed(1);
              // 5% 이상만 표시
              return percentage >= 5 ? percentage + '%' : '';
            }
          },
          legend: {
            position: 'right',
            labels: {
              color: '#cbd5e1',
              font: { size: 12 },
              padding: 15,
              generateLabels: function(chart) {
                const data = chart.data;
                const total = data.datasets[0].data.reduce((a, b) => a + b, 0);
                return data.labels.map((label, i) => {
                  const value = data.datasets[0].data[i];
                  const percentage = ((value / total) * 100).toFixed(1);
                  return {
                    text: `${label} (${percentage}%)`,
                    fillStyle: data.datasets[0].backgroundColor[i],
                    hidden: false,
                    index: i
                  };
                });
              }
            }
          },
          tooltip: {
            backgroundColor: 'rgba(15, 23, 42, 0.9)',
            titleColor: '#cbd5e1',
            bodyColor: '#cbd5e1',
            borderColor: '#475569',
            borderWidth: 1,
            callbacks: {
              label: function(context) {
                const label = context.label || '';
                const value = context.parsed || 0;
                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                const percentage = ((value / total) * 100).toFixed(1);
                return `${label}: ${value}명 (${percentage}%)`;
              }
            }
          }
        }
      },
      plugins: [ChartDataLabels]
    });
  } catch (error) {
    console.error('고객 세그먼트 차트 로드 실패:', error);
  }
}

// 출판사별 매출 비교 차트 (Horizontal Bar Chart)
async function loadPublisherRevenueChart(month = '') {
  try {
    const endpoint = month
      ? `/api/stats?action=publishers-by-month&month=${month}`
      : '/api/stats?action=publishers&sortBy=totalRevenue&direction=desc';

    const response = await fetchAPI(endpoint);
    const allData = response.data || [];

    // TOP 10만 표시
    const data = allData.slice(0, 10);

    const canvas = document.getElementById('publisher-revenue-chart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');

    // 기존 차트 파괴
    if (publisherRevenueChartInstance) {
      publisherRevenueChartInstance.destroy();
    }

    const labels = data.map(d => d.publisher);
    const revenues = data.map(d => d.totalRevenue);

    publisherRevenueChartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: '매출액 (원)',
          data: revenues,
          backgroundColor: '#818cf8',
          borderColor: '#6366f1',
          borderWidth: 1
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          datalabels: {
            color: '#cbd5e1',
            anchor: 'end',
            align: 'end',
            offset: 4,
            font: {
              size: 11,
              weight: 'bold'
            },
            formatter: function(value) {
              return formatCurrency(value);
            }
          },
          tooltip: {
            backgroundColor: 'rgba(15, 23, 42, 0.9)',
            titleColor: '#cbd5e1',
            bodyColor: '#cbd5e1',
            borderColor: '#475569',
            borderWidth: 1,
            callbacks: {
              label: function(context) {
                return '매출액: ' + formatCurrency(context.parsed.x);
              }
            }
          }
        },
        scales: {
          x: {
            grid: {
              color: 'rgba(148, 163, 184, 0.1)'
            },
            ticks: {
              color: '#94a3b8',
              callback: function(value) {
                return formatCurrency(value);
              }
            }
          },
          y: {
            grid: {
              display: false
            },
            ticks: {
              color: '#94a3b8',
              font: { size: 11 }
            }
          }
        }
      },
      plugins: [ChartDataLabels]
    });
  } catch (error) {
    console.error('출판사 매출 차트 로드 실패:', error);
  }
}

// 년도 드롭다운 채우기
function populateYearSelect() {
  const yearSelect = document.getElementById('year-select');
  if (!yearSelect) return;

  yearSelect.innerHTML = '<option value="">전체</option>';
  availableYears.forEach(year => {
    const option = document.createElement('option');
    option.value = year;
    option.textContent = `${year}년`;
    yearSelect.appendChild(option);
  });

  // 이벤트 리스너 추가
  yearSelect.addEventListener('change', (e) => {
    loadMonthlySalesChart(e.target.value);
  });
}

// 월 드롭다운 채우기
function populateMonthSelects() {
  const selects = [
    document.getElementById('segment-month-select'),
    document.getElementById('publisher-month-select'),
    document.getElementById('top-customer-month-select'),
    document.getElementById('book-stats-month-select')
  ];

  selects.forEach(select => {
    if (!select) return;

    // 기존 옵션 유지 (전체 기간 또는 빈 옵션)
    const hasAllOption = select.querySelector('option[value=""]');
    select.innerHTML = hasAllOption ? '<option value="">전체 기간</option>' : '';

    // 월 목록 추가
    availableMonths.forEach(month => {
      const option = document.createElement('option');
      option.value = month;
      option.textContent = month;
      select.appendChild(option);
    });

    // 최신 월 선택 (TOP 테이블용 및 출판사 차트용)
    if ((select.id === 'top-customer-month-select' || select.id === 'book-stats-month-select' || select.id === 'publisher-month-select') && availableMonths.length > 0) {
      select.value = availableMonths[0];
    }
  });

  // 이벤트 리스너 추가
  setupMonthSelectListeners();
}

// 월 선택 이벤트 리스너
function setupMonthSelectListeners() {
  const segmentSelect = document.getElementById('segment-month-select');
  const publisherSelect = document.getElementById('publisher-month-select');
  const topCustomerSelect = document.getElementById('top-customer-month-select');
  const bookStatsSelect = document.getElementById('book-stats-month-select');

  if (segmentSelect) {
    segmentSelect.addEventListener('change', (e) => {
      loadCustomerSegmentChart(e.target.value);
    });
  }

  if (publisherSelect) {
    publisherSelect.addEventListener('change', (e) => {
      loadPublisherRevenueChart(e.target.value);
    });
  }

  if (topCustomerSelect) {
    topCustomerSelect.addEventListener('change', (e) => {
      loadTopCustomers(e.target.value);
    });
  }

  if (bookStatsSelect) {
    bookStatsSelect.addEventListener('change', (e) => {
      loadBookStatsByMonth(e.target.value);
    });
  }
}

// 월별 TOP 고객 로드
async function loadTopCustomers(month) {
  const table = document.getElementById('top-customer-table');
  if (!table) return;

  const tbody = table.querySelector('tbody');
  if (!tbody) return;

  if (!month) {
    tbody.innerHTML = '<tr><td colspan="4" class="py-8 text-center text-slate-400">월을 선택하세요</td></tr>';
    return;
  }

  try {
    const response = await fetchAPI(`/api/stats?action=top-customers&month=${month}&limit=10`);
    const customers = response.data || [];

    if (customers.length === 0) {
      tbody.innerHTML = '<tr><td colspan="4" class="py-8 text-center text-slate-400">데이터가 없습니다</td></tr>';
      return;
    }

    tbody.innerHTML = customers.map((customer, index) => `
      <tr>
        <td class="text-center">${index + 1}</td>
        <td>${escapeHtml(customer.name)}</td>
        <td class="text-center">${customer.orderCount}건</td>
        <td class="text-right">${formatCurrency(customer.totalAmount)}</td>
      </tr>
    `).join('');
  } catch (error) {
    tbody.innerHTML = '<tr><td colspan="4" class="py-8 text-center text-red-300">데이터를 불러오지 못했습니다</td></tr>';
  }
}

// 월별 TOP 도서 로드
async function loadBookStatsByMonth(month) {
  const table = document.getElementById('book-stats-table');
  if (!table) return;

  const tbody = table.querySelector('tbody');
  if (!tbody) return;

  if (!month) {
    tbody.innerHTML = '<tr><td colspan="6" class="py-8 text-center text-slate-400">월을 선택하세요</td></tr>';
    return;
  }

  try {
    const response = await fetchAPI(`/api/stats?action=books-by-month&month=${month}`);
    const allBooks = response.data || [];

    if (allBooks.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="py-8 text-center text-slate-400">데이터가 없습니다</td></tr>';
      return;
    }

    // TOP 10만 표시
    const books = allBooks.slice(0, 10);

    tbody.innerHTML = books.map((book, index) => `
      <tr>
        <td class="text-center">${index + 1}</td>
        <td>${escapeHtml(book.bookname)}</td>
        <td>${escapeHtml(book.publisher)}</td>
        <td class="text-right">${formatCurrency(book.price)}</td>
        <td class="text-center">${book.salesCount}건</td>
        <td class="text-right">${formatCurrency(Math.round(book.avgSalePrice))}</td>
      </tr>
    `).join('');
  } catch (error) {
    tbody.innerHTML = '<tr><td colspan="6" class="py-8 text-center text-red-300">데이터를 불러오지 못했습니다</td></tr>';
  }
}

async function initDashboard() {
  renderAdminHeader('dashboard');
  renderAdminFooter();
  await Promise.all([
    loadOverviewStats(),
    loadBestsellers(),
    loadMonthlySalesChart(),
    loadCustomerSegmentChart()
  ]);

  // 초기 출판사 차트 및 TOP 테이블 로드 (최신 월 기준)
  if (availableMonths.length > 0) {
    loadPublisherRevenueChart(availableMonths[0]);
    loadTopCustomers(availableMonths[0]);
    loadBookStatsByMonth(availableMonths[0]);
  } else {
    loadPublisherRevenueChart();
  }
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
    const response = await fetchAPI('/api/stats?action=weekly-bestsellers&limit=5');
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
