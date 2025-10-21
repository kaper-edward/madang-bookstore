import mysql.connector
from mysql.connector import Error

# --- 데이터베이스 연결 정보 ---
db_config = {
    'host': 'localhost',
    'user': 'madang',
    'password': 'madang',
    'database': 'madangdb'
}

def alter_role_constraint():
    """
    Customer 테이블의 role CHECK 제약 조건을 수정하여
    'customer', 'publisher', 'admin', 'manager' 역할을 허용합니다.
    """

    connection = None
    cursor = None

    try:
        connection = mysql.connector.connect(**db_config)
        cursor = connection.cursor()

        print("기존 제약 조건을 삭제합니다...")
        cursor.execute("ALTER TABLE Customer DROP CONSTRAINT chk_customer_role")
        print("✓ 기존 제약 조건이 삭제되었습니다.")

        print("\n새로운 제약 조건을 추가합니다...")
        cursor.execute("""
            ALTER TABLE Customer
            ADD CONSTRAINT chk_customer_role
            CHECK (role IN ('customer', 'publisher', 'admin', 'manager'))
        """)
        print("✓ 새로운 제약 조건이 추가되었습니다.")
        print("  허용된 역할: 'customer', 'publisher', 'admin', 'manager'")

        connection.commit()

        # 변경 확인
        cursor.execute("SHOW CREATE TABLE Customer")
        result = cursor.fetchone()

        print("\n[변경된 테이블 스키마]")
        print("-" * 80)
        # CHECK 제약 조건만 출력
        create_sql = result[1]
        for line in create_sql.split('\n'):
            if 'chk_customer_role' in line or 'CHECK' in line:
                print(line.strip())
        print("-" * 80)

    except Error as e:
        print(f"\n데이터베이스 오류: {e}")
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
    print("Customer 테이블 Role 제약 조건 수정")
    print("=" * 80)
    print()

    alter_role_constraint()
