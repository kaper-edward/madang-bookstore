# 대시보드 개선 계획서

**작성일**: 2025-10-21
**목표**: 데이터 분석 중심의 인터랙티브 대시보드 구축
**예상 소요 시간**: 6~8시간

---

## 📊 1. 현재 상태 분석

### 현재 대시보드 구성

**KPI 카드 (5개)**
- 총 도서 / 총 고객 / 총 주문 / 총 매출 / 평균 판매가
- 상태: ✅ 양호 (숫자만 표시)

**데이터 테이블 (4개)**
- 고객별 매출 통계 (정렬 가능)
- 출판사별 판매 현황 (정렬 가능)
- 도서별 판매 지표 (정렬 가능)
- 최근 주문 (정렬 가능)
- 상태: ✅ 기능 완비

**베스트셀러**
- TOP 5 도서 (카드 형태)
- 상태: ✅ 양호

### 현재 API 엔드포인트

**StatsHandler (/api/stats)**
- `?action=overview` - 전체 통계
- `?action=bestsellers&limit=5` - 베스트셀러
- `?action=customers` - 고객별 통계 (정렬 지원)
- `?action=publishers` - 출판사별 통계 (정렬 지원)
- `?action=books` - 도서별 통계 (정렬 지원)
- `?action=recent&limit=10` - 최근 주문 (정렬 지원)

### 문제점

1. **시각화 부재**: 모든 데이터가 숫자와 텍스트로만 표현
2. **트렌드 분석 불가**: 시간 흐름에 따른 변화 파악 어려움
3. **비교 어려움**: 데이터 간 비율이나 비교가 직관적이지 않음
4. **인터랙티브 부족**: 정적인 정보 나열

---

## 🎯 2. 개선 목표

### 주요 개선 방향

1. **차트 추가**: 데이터 시각화로 직관성 향상
2. **시계열 분석**: 월별 판매 추이 파악
3. **비교 분석**: 출판사/카테고리 간 비교
4. **인터랙티브**: 클릭 시 상세 정보 표시

### 타겟 사용자

- **관리자/매니저**: 비즈니스 의사결정을 위한 인사이트
- **교육용**: SQL 학습자가 집계/그룹화 쿼리 결과 확인

---

## 🛠️ 3. 기술 스택

### Chart.js 선택 이유

**Chart.js 3.9.1** (최신 안정 버전)

**장점:**
- ✅ 무료, 오픈소스 (MIT 라이선스)
- ✅ 가볍고 빠름 (~200KB minified)
- ✅ 사용하기 쉬움 (학습 곡선 낮음)
- ✅ 반응형 지원 (모바일 대응)
- ✅ 다양한 차트 타입 (Line, Bar, Pie, Doughnut, etc.)
- ✅ 커스터마이징 용이
- ✅ 활발한 커뮤니티

**다른 옵션 검토:**
- **D3.js**: 너무 복잡, 학습 곡선 높음 ❌
- **Recharts**: React 전용 ❌
- **ApexCharts**: 좋지만 Chart.js가 더 가벼움

### CDN 사용

```html
<script src="https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js"></script>
```

---

## 📈 4. 추가할 차트 (우선순위별)

### 🔴 Priority 1: 필수 차트

#### 1) 월별 판매 추이 (Line Chart)
**위치**: KPI 카드 아래, 전체 너비
**데이터**: 최근 12개월 매출 및 주문 수
**API**: `GET /api/stats?action=monthly&months=12`

**응답 예시:**
```json
{
  "success": true,
  "data": [
    {"month": "2024-11", "totalRevenue": 150000, "orderCount": 25},
    {"month": "2024-12", "totalRevenue": 180000, "orderCount": 30},
    {"month": "2025-01", "totalRevenue": 200000, "orderCount": 35}
  ]
}
```

**효과:**
- 매출 트렌드 파악
- 계절성 분석 가능
- 성장률 시각화

#### 2) 출판사별 매출 비교 (Horizontal Bar Chart)
**위치**: 출판사별 판매 현황 테이블 대체 or 위에 추가
**데이터**: 출판사별 총 매출 (TOP 10)
**API**: `GET /api/stats?action=publishers&limit=10` (기존 활용)

**효과:**
- 출판사 간 비교 용이
- 시장 점유율 파악
- 주요 파트너 식별

### 🟡 Priority 2: 권장 차트

#### 3) 베스트셀러 판매 분포 (Doughnut Chart)
**위치**: 베스트셀러 섹션 옆
**데이터**: TOP 5 도서의 판매량 비율
**API**: `GET /api/stats?action=bestsellers&limit=5` (기존 활용)

**효과:**
- 도서 간 판매 비율 직관적 표시
- 특정 도서 집중도 파악

#### 4) 고객 세그먼트 (Pie Chart)
**위치**: 고객별 매출 통계 섹션 상단
**데이터**: 고객을 구매 금액별로 세그먼트
**API**: `GET /api/stats?action=customer-segments`

**세그먼트:**
- VIP (100,000원 이상)
- 우수 (50,000~99,999원)
- 일반 (10,000~49,999원)
- 신규 (10,000원 미만)

**효과:**
- 고객 구성 파악
- 마케팅 전략 수립

### 🟢 Priority 3: 선택 차트 (향후 확장)

#### 5) 주간 주문 패턴 (Bar Chart)
**데이터**: 요일별 주문 수
**효과**: 운영 최적화 (인력 배치 등)

#### 6) 가격대별 판매 분포 (Histogram)
**데이터**: 가격 구간별 판매 수량
**효과**: 가격 전략 수립

---

## 🔧 5. 구현 계획

### Phase 1: 백엔드 API 추가 (2시간)

**OrderDAO.java 메서드 추가**

```java
/**
 * 월별 판매 통계 (최근 N개월)
 */
public List<Map<String, Object>> getMonthlySales(int months) throws SQLException
```

**SQL:**
```sql
SELECT
    DATE_FORMAT(orderdate, '%Y-%m') AS month,
    COUNT(*) AS orderCount,
    SUM(saleprice) AS totalRevenue
FROM Orders
WHERE orderdate >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)
GROUP BY DATE_FORMAT(orderdate, '%Y-%m')
ORDER BY month ASC
```

**StatsHandler.java 액션 추가**

```java
if ("monthly".equals(action)) {
    int months = Integer.parseInt(params.getOrDefault("months", "12"));
    List<Map<String, Object>> stats = orderDAO.getMonthlySales(months);
    return successResponse(listMapToJsonArray(stats));
}

if ("customer-segments".equals(action)) {
    List<Map<String, Object>> segments = orderDAO.getCustomerSegments();
    return successResponse(listMapToJsonArray(segments));
}
```

### Phase 2: 프론트엔드 구현 (4시간)

#### 2.1 Chart.js 라이브러리 추가 (10분)

**dashboard.html 수정**
```html
<!-- Chart.js CDN 추가 -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js"></script>
```

#### 2.2 월별 판매 추이 차트 (1시간)

**dashboard.html 차트 컨테이너 추가**
```html
<section class="glass-panel p-6 space-y-4">
  <h2 class="text-xl font-semibold">월별 판매 추이</h2>
  <div class="h-80">
    <canvas id="monthly-sales-chart"></canvas>
  </div>
</section>
```

**dashboard.js 차트 생성 함수**
```javascript
async function loadMonthlySalesChart() {
  try {
    const response = await fetchAPI('/api/stats?action=monthly&months=12');
    const data = response.data || [];

    const labels = data.map(item => item.month);
    const revenues = data.map(item => item.totalRevenue);
    const orders = data.map(item => item.orderCount);

    const ctx = document.getElementById('monthly-sales-chart').getContext('2d');
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: '매출 (원)',
            data: revenues,
            borderColor: 'rgb(99, 102, 241)',
            backgroundColor: 'rgba(99, 102, 241, 0.1)',
            yAxisID: 'y',
          },
          {
            label: '주문 수 (건)',
            data: orders,
            borderColor: 'rgb(236, 72, 153)',
            backgroundColor: 'rgba(236, 72, 153, 0.1)',
            yAxisID: 'y1',
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: 'index',
          intersect: false,
        },
        plugins: {
          legend: {
            labels: {
              color: 'rgb(203, 213, 225)' // text-slate-300
            }
          }
        },
        scales: {
          y: {
            type: 'linear',
            display: true,
            position: 'left',
            ticks: { color: 'rgb(148, 163, 184)' },
            grid: { color: 'rgba(255, 255, 255, 0.05)' }
          },
          y1: {
            type: 'linear',
            display: true,
            position: 'right',
            ticks: { color: 'rgb(148, 163, 184)' },
            grid: { drawOnChartArea: false }
          },
          x: {
            ticks: { color: 'rgb(148, 163, 184)' },
            grid: { color: 'rgba(255, 255, 255, 0.05)' }
          }
        }
      }
    });
  } catch (error) {
    console.error('월별 차트 로딩 실패:', error);
  }
}
```

#### 2.3 출판사 매출 비교 차트 (1시간)

**dashboard.html**
```html
<section class="glass-panel p-6 space-y-4">
  <div class="flex items-center justify-between">
    <h2 class="text-xl font-semibold">출판사별 매출 비교</h2>
  </div>
  <div class="h-96">
    <canvas id="publisher-comparison-chart"></canvas>
  </div>
</section>
```

**dashboard.js**
```javascript
async function loadPublisherComparisonChart() {
  try {
    const response = await fetchAPI('/api/stats?action=publishers&sortBy=totalRevenue&direction=desc');
    const data = (response.data || []).slice(0, 10); // TOP 10

    const labels = data.map(item => item.publisher);
    const revenues = data.map(item => item.totalRevenue);

    const ctx = document.getElementById('publisher-comparison-chart').getContext('2d');
    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: '매출 (원)',
          data: revenues,
          backgroundColor: 'rgba(99, 102, 241, 0.8)',
          borderColor: 'rgb(99, 102, 241)',
          borderWidth: 1
        }]
      },
      options: {
        indexAxis: 'y', // Horizontal bar
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          x: {
            ticks: { color: 'rgb(148, 163, 184)' },
            grid: { color: 'rgba(255, 255, 255, 0.05)' }
          },
          y: {
            ticks: { color: 'rgb(148, 163, 184)' },
            grid: { display: false }
          }
        }
      }
    });
  } catch (error) {
    console.error('출판사 비교 차트 로딩 실패:', error);
  }
}
```

#### 2.4 베스트셀러 분포 차트 (1시간)

**Doughnut Chart 구현**

#### 2.5 초기화 함수 수정 (30분)

```javascript
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
    loadRecentOrders(),
    // 새로 추가
    loadMonthlySalesChart(),
    loadPublisherComparisonChart(),
    loadBestsellerDistributionChart()
  ]);
}
```

### Phase 3: 테스트 및 검증 (2시간)

#### 3.1 기능 테스트
- [ ] 월별 차트 정상 렌더링
- [ ] 출판사 비교 차트 정상 렌더링
- [ ] 베스트셀러 분포 차트 정상 렌더링
- [ ] 데이터 없을 때 에러 처리
- [ ] 반응형 (모바일, 태블릿, 데스크탑)

#### 3.2 성능 테스트
- [ ] API 응답 시간 < 200ms
- [ ] 차트 렌더링 < 500ms
- [ ] 전체 대시보드 로딩 < 2초

#### 3.3 UX 테스트
- [ ] 색상 대비 (다크 테마에 어울리는지)
- [ ] 툴팁 표시 (호버 시 상세 정보)
- [ ] 범례 가독성

---

## 📐 6. 레이아웃 구성

### 최종 대시보드 구조

```
┌─────────────────────────────────────┐
│  Header (네비게이션)                 │
├─────────────────────────────────────┤
│  제목 + 설명                         │
├─────────────────────────────────────┤
│  KPI 카드 (5개) - 기존 유지          │
├─────────────────────────────────────┤
│  📈 월별 판매 추이 (Line Chart)      │  ← NEW
│  - 전체 너비                         │
├─────────────────────────────────────┤
│  출판사별 매출 비교 (Bar Chart)      │  ← NEW
│  - 전체 너비                         │
├─────────────────────────────────────┤
│  ┌───────────┬───────────────────┐  │
│  │ 베스트셀러 │ 베스트셀러 분포   │  │
│  │ 카드 (5개) │ (Doughnut Chart)  │  │ ← 개선
│  └───────────┴───────────────────┘  │
├─────────────────────────────────────┤
│  ┌───────────────┬───────────────┐  │
│  │ 고객별 통계   │ 출판사별 통계 │  │ - 기존 유지
│  │ (테이블)      │ (테이블)      │  │
│  └───────────────┴───────────────┘  │
├─────────────────────────────────────┤
│  ┌───────────────┬───────────────┐  │
│  │ 도서별 통계   │ 최근 주문     │  │ - 기존 유지
│  │ (테이블)      │ (테이블)      │  │
│  └───────────────┴───────────────┘  │
└─────────────────────────────────────┘
```

---

## 📋 7. 작업 체크리스트

### 백엔드 (OrderDAO.java)
- [ ] getMonthlySales(int months) 메서드 추가
- [ ] getCustomerSegments() 메서드 추가 (선택)
- [ ] 컴파일 테스트
- [ ] 단위 테스트 (API 호출)

### 백엔드 (StatsHandler.java)
- [ ] monthly 액션 추가
- [ ] customer-segments 액션 추가 (선택)
- [ ] 컴파일 테스트

### 프론트엔드 (dashboard.html)
- [ ] Chart.js CDN 추가
- [ ] 월별 차트 컨테이너 추가
- [ ] 출판사 차트 컨테이너 추가
- [ ] 베스트셀러 차트 컨테이너 추가

### 프론트엔드 (dashboard.js)
- [ ] loadMonthlySalesChart() 함수 구현
- [ ] loadPublisherComparisonChart() 함수 구현
- [ ] loadBestsellerDistributionChart() 함수 구현
- [ ] initDashboard() 수정 (차트 로딩 추가)

### 테스트
- [ ] 기능 테스트 완료
- [ ] 성능 테스트 완료
- [ ] UX 테스트 완료

### 문서
- [ ] CLAUDE.md 업데이트 (대시보드 차트 언급)
- [ ] 커밋 메시지 작성

---

## 🎨 8. 디자인 가이드

### 색상 팔레트 (Tailwind 기반)

**Primary (차트 주 색상)**
- `rgb(99, 102, 241)` - Indigo-500 (매출, 주요 지표)
- `rgba(99, 102, 241, 0.1)` - Background fill

**Secondary (차트 보조 색상)**
- `rgb(236, 72, 153)` - Pink-500 (주문 수, 보조 지표)
- `rgba(236, 72, 153, 0.1)` - Background fill

**Accent (강조 색상)**
- `rgb(34, 197, 94)` - Green-500 (긍정적 지표)
- `rgb(239, 68, 68)` - Red-500 (부정적 지표)

**Text & Grid**
- `rgb(203, 213, 225)` - Slate-300 (범례, 레이블)
- `rgb(148, 163, 184)` - Slate-400 (축 레이블)
- `rgba(255, 255, 255, 0.05)` - 그리드 라인

### 폰트
- 차트 내 폰트: 시스템 기본 (가독성 우선)
- 크기: 12px (기본), 14px (범례)

---

## 📊 9. 예상 효과

| 항목 | 개선 전 | 개선 후 | 향상도 |
|------|---------|---------|--------|
| 데이터 이해도 | 중간 | 높음 | 150% ↑ |
| 의사결정 속도 | 느림 | 빠름 | 200% ↑ |
| 트렌드 파악 | 불가능 | 즉시 가능 | 무한대 ↑ |
| 사용자 만족도 | 보통 | 매우 높음 | 180% ↑ |
| 교육적 가치 | 높음 | 매우 높음 | 130% ↑ |

---

## 🚀 10. 향후 확장 계획

### Phase 2 (추후)
- [ ] 대시보드 필터링 (기간 선택)
- [ ] 실시간 업데이트 (WebSocket)
- [ ] 대시보드 내보내기 (PDF, PNG)
- [ ] 커스텀 대시보드 (사용자별 위젯 설정)

### Phase 3 (장기)
- [ ] 예측 분석 (머신러닝)
- [ ] 이상 탐지 (매출 급감 알림)
- [ ] A/B 테스트 결과 시각화

---

**작성자**: 마당 서점 개발팀
**최종 검토**: 2025-10-21
**다음 리뷰**: 구현 완료 후
