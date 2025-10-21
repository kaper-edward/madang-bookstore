import mysql.connector
from mysql.connector import Error

# --- 데이터베이스 연결 정보 ---
db_config = {
    'host': 'localhost',
    'user': 'madang',
    'password': 'madang',
    'database': 'madangdb'
}

def update_customer_roles():
    """
    특정 고객의 역할(role)을 admin 또는 manager로 업데이트합니다.

    Role-Based Access Control (RBAC) 정의:
    - admin: 시스템 관리자 (전체 권한, 사용자 관리, 시스템 설정)
    - manager: 매니저 (도서 관리, 주문 관리, 통계 조회)
    - customer: 일반 고객 (도서 구매, 자신의 주문 조회) [기본값]
    """

    # 역할 할당 정의
    admin_ids = [2]  # 김연아 (박지성은 이미 admin)
    manager_ids = [3, 4, 5]  # 김연경, 추신수, 박세리

    connection = None
    cursor = None

    try:
        connection = mysql.connector.connect(**db_config)
        cursor = connection.cursor()

        # Admin 역할 부여
        if admin_ids:
            admin_query = "UPDATE Customer SET role = 'admin' WHERE custid IN ({})".format(
                ','.join(map(str, admin_ids))
            )
            cursor.execute(admin_query)
            print(f"✓ {cursor.rowcount}명을 admin 역할로 업데이트했습니다.")

        # Manager 역할 부여
        if manager_ids:
            manager_query = "UPDATE Customer SET role = 'manager' WHERE custid IN ({})".format(
                ','.join(map(str, manager_ids))
            )
            cursor.execute(manager_query)
            print(f"✓ {cursor.rowcount}명을 manager 역할로 업데이트했습니다.")

        connection.commit()

        # 결과 확인
        cursor.execute("""
            SELECT role, COUNT(*) as count
            FROM Customer
            GROUP BY role
            ORDER BY
                CASE role
                    WHEN 'admin' THEN 1
                    WHEN 'manager' THEN 2
                    WHEN 'customer' THEN 3
                END
        """)

        print("\n[역할별 분포]")
        print("-" * 40)
        for row in cursor.fetchall():
            role, count = row
            print(f"  {role:10s}: {count:3d}명")
        print("-" * 40)

        # 관리자 계정 목록 표시
        cursor.execute("""
            SELECT custid, name, address, role
            FROM Customer
            WHERE role IN ('admin', 'manager')
            ORDER BY
                CASE role WHEN 'admin' THEN 1 WHEN 'manager' THEN 2 END,
                custid
        """)

        print("\n[관리자 계정 목록]")
        print("-" * 80)
        print(f"{'ID':>4s} | {'이름':10s} | {'주소':30s} | {'역할':10s}")
        print("-" * 80)
        for row in cursor.fetchall():
            custid, name, address, role = row
            print(f"{custid:4d} | {name:10s} | {address:30s} | {role:10s}")
        print("-" * 80)

    except Error as e:
        print(f"데이터베이스 오류: {e}")
        if connection:
            connection.rollback()
    finally:
        if cursor:
            cursor.close()
        if connection and connection.is_connected():
            connection.close()
            print("\nMySQL 연결이 종료되었습니다.")

if __name__ == '__main__':
    print("=" * 80)
    print("마당서점 Role-Based Access Control (RBAC) 설정")
    print("=" * 80)
    print()
    print("역할 정의:")
    print("  - admin   : 시스템 관리자 (전체 권한)")
    print("  - manager : 매니저 (도서 관리, 주문 관리, 통계 조회)")
    print("  - customer: 일반 고객 (도서 구매, 주문 조회)")
    print()
    print("역할 할당 작업을 시작합니다...")
    print()

    update_customer_roles()
