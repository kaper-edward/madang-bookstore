# Faker 라이브러리 설치가 필요합니다.
# pip install Faker

import random
from faker import Faker

# 한국어 데이터 생성을 위한 Faker 객체 초기화
fake = Faker('ko_KR')

def generate_fake_books(n, start_id=11):
    """
    주어진 패턴에 따라 n개의 가짜 책 데이터를 생성하여 SQL INSERT 문으로 반환합니다.

    Args:
        n (int): 생성할 데이터의 개수
        start_id (int): 시작할 bookid (기본값: 11)

    Returns:
        list: 생성된 SQL INSERT 문 리스트
    """
    
    # 기존 데이터에서 발견된 출판사 목록
    publishers = ['굿스포츠', '나무수', '대한미디어', '이상미디어', '삼성당', 'Pearson']
    
    # 책 이름 생성을 위한 스포츠 관련 키워드
    sports_keywords = ['축구', '농구', '야구', '배구', '골프', '테니스', '피겨', '수영', '올림픽']
    
    # 책 이름 생성을 위한 형식 템플릿
    bookname_templates = [
        lambda kw: f'{kw}의 역사',
        lambda kw: f'{kw} 아는 사람',
        lambda kw: f'{kw}의 이해',
        lambda kw: f'{kw} 바이블',
        lambda kw: f'{kw} 교본',
        lambda kw: f'{kw} 단계별 기술',
        lambda kw: f'즐거운 {kw}',
        lambda kw: f'{kw}를 부탁해',
        lambda kw: f'{kw} 이야기'
    ]

    generated_books = []
    
    print("-- 생성된 가짜 책 데이터 (SQL INSERT 문)")
    for i in range(n):
        bookid = start_id + i
        
        # 책 이름 무작위 생성
        keyword = random.choice(sports_keywords)
        template = random.choice(bookname_templates)
        bookname = template(keyword)
        
        # 출판사 무작위 선택
        publisher = random.choice(publishers)
        
        # 가격 무작위 생성 (1000원 단위)
        price = random.randrange(5000, 50001, 1000)
        
        # SQL INSERT 문 생성
        sql_statement = f"INSERT INTO Book VALUES({bookid}, '{bookname}', '{publisher}', {price});"
        generated_books.append(sql_statement)
        
    return generated_books

if __name__ == '__main__':
    # 생성할 데이터 개수 설정
    num_to_generate = 5
    
    # 함수를 호출하여 가짜 데이터 생성
    fake_book_data = generate_fake_books(num_to_generate, start_id=11)
    
    # 결과 출력
    for statement in fake_book_data:
        print(statement)