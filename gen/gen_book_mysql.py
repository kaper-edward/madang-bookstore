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

def generate_fake_books_data(n):
    """
    n개의 가짜 책 데이터를 생성하여 (bookname, publisher, price) 튜플의 리스트로 반환합니다.
    """
    publishers = ['굿스포츠', '나무수', '대한미디어', '이상미디어', '삼성당', 'Pearson']
    sports_keywords = ['축구', '농구', '야구', '배구', '골프', '테니스', '피겨', '수영', '올림픽']
    bookname_templates = [
        lambda kw: f'{kw}의 역사', lambda kw: f'{kw} 아는 사람', lambda kw: f'{kw}의 이해',
        lambda kw: f'{kw} 바이블', lambda kw: f'{kw} 교본', lambda kw: f'{kw} 단계별 기술',
        lambda kw: f'즐거운 {kw}', lambda kw: f'{kw}를 부탁해', lambda kw: f'{kw} 이야기'
    ]
    
    generated_data = []
    for _ in range(n):
        keyword = random.choice(sports_keywords)
        template = random.choice(bookname_templates)
        bookname = template(keyword)
        publisher = random.choice(publishers)
        price = random.randrange(5000, 50001, 1000)
        generated_data.append((bookname, publisher, price))
        
    return generated_data

def insert_books_to_db(book_data_list):
    """
    데이터 리스트를 받아와서 MySQL 데이터베이스에 삽입합니다.
    """
    if not book_data_list:
        print("삽입할 데이터가 없습니다.")
        return
        
    connection = None
    cursor = None
    query = "INSERT INTO Book (bookname, publisher, price) VALUES (%s, %s, %s)"
    
    try:
        connection = mysql.connector.connect(**db_config)
        cursor = connection.cursor()
        cursor.executemany(query, book_data_list)
        connection.commit()
        print(f"성공적으로 {cursor.rowcount}개의 데이터가 Book 테이블에 삽입되었습니다.")
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
    # ===== CLI 인자 처리 부분을 수정했습니다 =====
    parser = argparse.ArgumentParser(description="가짜 책 데이터를 생성하여 MadangDB에 삽입하는 프로그램")
    parser.add_argument('-n', '--number', type=int, required=True, help="생성할 데이터의 개수 (필수)")
    
    args = parser.parse_args()
    num_to_generate = args.number

    print(f"{num_to_generate}개의 가짜 데이터를 생성합니다...")
    
    fake_book_data = generate_fake_books_data(num_to_generate)
    
    for data in fake_book_data:
        print(f"  - 생성됨: {data}")
    
    insert_books_to_db(fake_book_data)