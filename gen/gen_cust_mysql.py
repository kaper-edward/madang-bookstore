import random
from faker import Faker
import mysql.connector
from mysql.connector import Error
import argparse

# --- 데이터베이스 연결 정보 ---
db_config = {
    'host': 'localhost',
    'user': 'madang',
    'password': 'madang',
    'database': 'madangdb'
}

# 한국어 데이터 생성을 위한 Faker 객체 초기화
fake = Faker('ko_KR')

def generate_fake_customers_data(n):
    """
    n개의 가짜 고객 데이터를 생성하여 (name, address, phone) 튜플의 리스트로 반환합니다.
    custid는 생성하지 않습니다.
    """
    generated_data = []
    for _ in range(n):
        name = fake.name()
        address = fake.address()
        
        # 원본 데이터에 NULL이 있었던 것을 감안하여 10% 확률로 전화번호를 NULL로 설정
        phone = random.choices([fake.phone_number(), None], weights=[9, 1], k=1)[0]
        
        # custid를 제외하고 튜플에 추가
        generated_data.append((name, address, phone))
        
    return generated_data

def insert_customers_to_db(customer_data_list):
    """
    고객 데이터 리스트를 받아와서 MySQL 데이터베이스에 삽입합니다.
    """
    if not customer_data_list:
        print("삽입할 데이터가 없습니다.")
        return
        
    connection = None
    cursor = None
    # custid를 제외한 INSERT 쿼리
    query = "INSERT INTO Customer (name, address, phone) VALUES (%s, %s, %s)"
    
    try:
        connection = mysql.connector.connect(**db_config)
        cursor = connection.cursor()
        cursor.executemany(query, customer_data_list)
        connection.commit()
        print(f"성공적으로 {cursor.rowcount}개의 데이터가 Customer 테이블에 삽입되었습니다.")
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
    parser = argparse.ArgumentParser(description="가짜 고객 데이터를 생성하여 MadangDB에 삽입하는 프로그램")
    parser.add_argument('-n', '--number', type=int, required=True, help="생성할 데이터의 개수 (필수)")
    
    args = parser.parse_args()
    num_to_generate = args.number

    print(f"{num_to_generate}개의 가짜 고객 데이터를 생성합니다...")
    
    fake_customer_data = generate_fake_customers_data(num_to_generate)
    
    for data in fake_customer_data:
        print(f"  - 생성됨: {data}")
    
    insert_customers_to_db(fake_customer_data)