import random
from faker import Faker
import mysql.connector
from mysql.connector import Error
import argparse
from datetime import datetime

# --- 데이터베이스 연결 정보 ---
db_config = {
    'host': 'localhost',
    'user': 'madang',
    'password': 'madang',
    'database': 'madangdb'
}

# Faker 객체 초기화
fake = Faker('ko_KR')

def fetch_prerequisites_from_db():
    """
    Orders 데이터를 생성하는 데 필요한 Book, Customer ID 목록을 DB에서 가져옵니다.
    """
    books = []
    customers = []
    connection = None
    try:
        connection = mysql.connector.connect(**db_config)
        cursor = connection.cursor()

        # 책 정보 (bookid, price) 가져오기
        cursor.execute("SELECT bookid, price FROM Book")
        books = cursor.fetchall() # 결과는 [(1, 7000), (2, 13000), ...] 형태

        # 고객 ID (custid) 가져오기
        cursor.execute("SELECT custid FROM Customer")
        # 결과를 [1, 2, 3, ...] 형태의 간단한 리스트로 변환
        customers = [item[0] for item in cursor.fetchall()]

    except Error as e:
        print(f"데이터베이스 조회 오류: {e}")
    finally:
        if connection and connection.is_connected():
            connection.close()
    
    return books, customers

def generate_fake_orders_data(n, books, customers):
    """
    n개의 가짜 주문 데이터를 생성합니다.
    - books: (bookid, price) 튜플의 리스트
    - customers: custid의 리스트
    """
    if not books or not customers:
        return []

    generated_data = []

    # --- 가중치(인기도) 생성 ---
    # ID 목록을 뒤집어 가중치를 생성 (ID가 작을수록 더 높은 가중치를 가짐)
    book_weights = list(range(len(books), 0, -1))
    customer_weights = list(range(len(customers), 0, -1))

    # random.choices를 사용하여 가중치 기반으로 n개의 항목을 한 번에 선택
    selected_books = random.choices(books, weights=book_weights, k=n)
    selected_customers = random.choices(customers, weights=customer_weights, k=n)

    for i in range(n):
        bookid, price = selected_books[i]
        custid = selected_customers[i]
        
        # 판매가(saleprice) 생성: 정가에서 0~3000원 사이의 금액을 1000원 단위로 할인
        discount = random.choice([0, 1000, 2000, 3000])
        saleprice = max(1000, price - discount) # 최소 판매가는 1000원으로 보정
        
        # 주문일자(orderdate) 생성: 최근 2년 내의 랜덤 날짜
        orderdate = fake.date_between(start_date='-2y', end_date='today')
        
        # orderid를 제외하고 튜플에 추가
        generated_data.append((custid, bookid, saleprice, orderdate))
        
    return generated_data

def insert_orders_to_db(order_data_list):
    """
    주문 데이터 리스트를 받아와서 MySQL 데이터베이스에 삽입합니다.
    """
    if not order_data_list:
        print("삽입할 데이터가 없습니다.")
        return
        
    connection = None
    cursor = None
    # orderid를 제외한 INSERT 쿼리
    query = "INSERT INTO Orders (custid, bookid, saleprice, orderdate) VALUES (%s, %s, %s, %s)"
    
    try:
        connection = mysql.connector.connect(**db_config)
        cursor = connection.cursor()
        cursor.executemany(query, order_data_list)
        connection.commit()
        print(f"성공적으로 {cursor.rowcount}개의 데이터가 Orders 테이블에 삽입되었습니다.")
    except Error as e:
        print(f"데이터베이스 오류: {e}")
        if connection:
            connection.rollback()
    finally:
        if cursor:
            cursor.close()
        if connection and connection.is_connected():
            connection.close()
            print("MySQL 연결이 종료되었습니다.")

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="가짜 주문 데이터를 생성하여 MadangDB에 삽입하는 프로그램")
    parser.add_argument('-n', '--number', type=int, required=True, help="생성할 데이터의 개수 (필수)")
    
    args = parser.parse_args()
    num_to_generate = args.number

    # 1. DB에서 Orders 생성을 위한 기초 데이터(책, 고객) 가져오기
    print("DB에서 책과 고객 정보를 가져옵니다...")
    books, customers = fetch_prerequisites_from_db()

    if not books or not customers:
        print("오류: Orders를 생성하기 위한 책 또는 고객 데이터가 DB에 없습니다. 스크립트를 종료합니다.")
    else:
        # 2. 가짜 주문 데이터 생성
        print(f"{num_to_generate}개의 가짜 주문 데이터를 생성합니다...")
        fake_order_data = generate_fake_orders_data(num_to_generate, books, customers)
        
        for data in fake_order_data:
            print(f"  - 생성됨: (custid:{data[0]}, bookid:{data[1]}, saleprice:{data[2]}, orderdate:{data[3]})")
        
        # 3. 생성된 데이터를 DB에 삽입
        insert_orders_to_db(fake_order_data)
