/**
 * API 통신 모듈
 * Servlet API와 통신하는 공통 함수들
 */

// API 기본 URL (Java HttpServer)
//const API_BASE_URL = 'http://localhost:8080';
const API_BASE_URL = '';

/**
 * API 호출 공통 함수
 * @param {string} endpoint - API 엔드포인트
 * @param {Object} options - fetch 옵션
 * @returns {Promise<Object>} - API 응답 데이터
 */
async function fetchAPI(endpoint, options = {}) {
  try {
    const url = API_BASE_URL + endpoint;
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();

    if (!data.success) {
      throw new Error(data.error || '요청 처리 중 오류가 발생했습니다.');
    }

    return data;
  } catch (error) {
    console.error('API Error:', error);
    handleError(error);
    throw error;
  }
}

/**
 * 에러 처리 함수
 * @param {Error} error - 에러 객체
 */
function handleError(error) {
  const errorMessage = error.message || '알 수 없는 오류가 발생했습니다.';

  // 에러 메시지 표시 (간단한 alert 또는 toast)
  if (typeof showToast === 'function') {
    showToast(errorMessage, 'error');
  } else {
    console.error('Error:', errorMessage);
  }
}

/**
 * Toast 메시지 표시 (선택 사항)
 * @param {string} message - 메시지 내용
 * @param {string} type - 메시지 타입 (success, error, info)
 */
function showToast(message, type = 'info') {
  // 간단한 toast 구현
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  toast.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 1rem 1.5rem;
    background-color: ${type === 'error' ? '#dc2626' : type === 'success' ? '#16a34a' : '#1e40af'};
    color: white;
    border-radius: 0.5rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    z-index: 10000;
    animation: slideIn 0.3s ease-out;
  `;

  document.body.appendChild(toast);

  setTimeout(() => {
    toast.style.animation = 'slideOut 0.3s ease-out';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

// CSS 애니메이션 추가
if (!document.getElementById('toast-animations')) {
  const style = document.createElement('style');
  style.id = 'toast-animations';
  style.textContent = `
    @keyframes slideIn {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }
    @keyframes slideOut {
      from {
        transform: translateX(0);
        opacity: 1;
      }
      to {
        transform: translateX(100%);
        opacity: 0;
      }
    }
  `;
  document.head.appendChild(style);
}

/**
 * 로그인 상태 확인
 * @returns {number|null} - 고객 ID 또는 null
 */
function getCustomerId() {
  const custid = localStorage.getItem('custid');
  return custid ? parseInt(custid) : null;
}

/**
 * 고객 정보 가져오기
 * @returns {Object|null} - 고객 정보 객체 또는 null
 */
function getCustomerInfo() {
  const custid = localStorage.getItem('custid');
  const custname = localStorage.getItem('custname');

  if (custid && custname) {
    return {
      custid: parseInt(custid),
      name: custname
    };
  }

  return null;
}

/**
 * 고객 정보 저장
 * @param {number} custid - 고객 ID
 * @param {string} name - 고객명
 */
function setCustomerInfo(custid, name) {
  localStorage.setItem('custid', custid.toString());
  localStorage.setItem('custname', name);
}

/**
 * 로그아웃
 */
function logout() {
  localStorage.removeItem('custid');
  localStorage.removeItem('custname');
  window.location.href = 'index.html';
}

/**
 * 로그인 상태 확인 및 UI 업데이트
 */
function checkLoginStatus() {
  const customer = getCustomerInfo();
  const userInfoElement = document.getElementById('user-info');

  if (customer && userInfoElement) {
    userInfoElement.innerHTML = `
      <div class="flex items-center gap-4">
        <span class="text-secondary">로그인: <strong>${customer.name}</strong>님 👤</span>
        <button onclick="logout()" class="btn-secondary">로그아웃</button>
        <a href="index.html" class="btn-outline">홈</a>
      </div>
    `;
  }
}

/**
 * 로그인 필수 페이지 체크
 * @param {string} redirectUrl - 로그인 후 돌아올 URL
 */
function requireLogin(redirectUrl = null) {
  const customer = getCustomerInfo();

  if (!customer) {
    alert('로그인이 필요한 페이지입니다.');

    if (redirectUrl) {
      localStorage.setItem('redirectAfterLogin', redirectUrl);
    }

    window.location.href = 'customer-login.html';
    return false;
  }

  return true;
}

/**
 * 로그인 후 리다이렉트 처리
 */
function handlePostLoginRedirect() {
  const redirectUrl = localStorage.getItem('redirectAfterLogin');

  if (redirectUrl) {
    localStorage.removeItem('redirectAfterLogin');
    window.location.href = redirectUrl;
    return true;
  }

  return false;
}

/**
 * Debounce 함수 (검색 입력 최적화용)
 * @param {Function} func - 실행할 함수
 * @param {number} delay - 지연 시간 (ms)
 * @returns {Function} - debounced 함수
 */
function debounce(func, delay = 500) {
  let timeout;
  return function (...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), delay);
  };
}

/**
 * 날짜 포맷팅 (YYYY-MM-DD -> YYYY년 MM월 DD일)
 * @param {string} dateString - 날짜 문자열
 * @returns {string} - 포맷팅된 날짜
 */
function formatDate(dateString) {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}년 ${month}월 ${day}일`;
}

/**
 * 숫자를 천 단위 콤마로 포맷팅
 * @param {number} number - 숫자
 * @returns {string} - 포맷팅된 문자열
 */
function formatNumber(number) {
  return number.toLocaleString('ko-KR');
}

/**
 * 원화 포맷팅
 * @param {number} amount - 금액
 * @returns {string} - 포맷팅된 금액 (예: "10,000원")
 */
function formatCurrency(amount) {
  return `${formatNumber(amount)}원`;
}

/**
 * 로딩 스피너 표시/숨김
 * @param {boolean} show - 표시 여부
 */
function toggleLoadingSpinner(show) {
  let spinner = document.getElementById('loading-spinner');

  if (show) {
    if (!spinner) {
      spinner = document.createElement('div');
      spinner.id = 'loading-spinner';
      spinner.className = 'spinner';
      document.body.appendChild(spinner);
    }
    spinner.style.display = 'block';
  } else {
    if (spinner) {
      spinner.style.display = 'none';
    }
  }
}

/**
 * URL 파라미터 가져오기
 * @param {string} param - 파라미터 이름
 * @returns {string|null} - 파라미터 값
 */
function getUrlParameter(param) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(param);
}

/**
 * 빈 상태 메시지 표시
 * @param {HTMLElement} element - 컨테이너 요소
 * @param {string} message - 메시지
 */
function showEmptyState(element, message = '데이터가 없습니다.') {
  element.innerHTML = `
    <div class="empty-message">
      <p>${message}</p>
    </div>
  `;
}

/**
 * 에러 상태 메시지 표시
 * @param {HTMLElement} element - 컨테이너 요소
 * @param {string} message - 에러 메시지
 */
function showErrorState(element, message = '데이터를 불러오는 중 오류가 발생했습니다.') {
  element.innerHTML = `
    <div class="empty-message text-danger">
      <p>⚠️ ${message}</p>
      <button onclick="location.reload()" class="btn-primary mt-4">
        다시 시도
      </button>
    </div>
  `;
}
