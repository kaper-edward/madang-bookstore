#!/usr/bin/env python3
"""
관리자 페이지의 헤더와 footer를 통일하는 스크립트
"""
import re
import os

# admin 디렉토리의 HTML 파일 목록
html_files = [
    'frontend/admin/index.html',
    'frontend/admin/books-admin.html',
    'frontend/admin/customers-admin.html',
]

def update_html_file(filepath):
    """HTML 파일의 헤더와 footer를 업데이트"""
    print(f"Processing {filepath}...")

    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. 헤더를 placeholder로 교체
    header_pattern = r'<header[^>]*>.*?</header>'
    header_replacement = '''<header class="glass-panel border-b border-white/10">
      <!-- 헤더는 JavaScript로 동적 렌더링됩니다 -->
    </header>'''

    content = re.sub(header_pattern, header_replacement, content, flags=re.DOTALL)

    # 2. Footer를 placeholder로 교체
    footer_pattern = r'<footer[^>]*>.*?</footer>'
    footer_replacement = '''<footer class="border-t border-white/10 bg-slate-950/80 backdrop-blur-md mt-20">
      <!-- 푸터는 JavaScript로 동적 렌더링됩니다 -->
    </footer>'''

    content = re.sub(footer_pattern, footer_replacement, content, flags=re.DOTALL)

    # 3. header.js 스크립트 태그 추가 (이미 있으면 추가 안함)
    if 'header.js' not in content:
        # ../js/api.js 다음에 ../js/header.js 추가
        content = content.replace(
            '<script src="../js/api.js"></script>',
            '<script src="../js/api.js"></script>\n  <script src="../js/header.js"></script>'
        )

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"  ✓ Updated {filepath}")

# 각 파일 업데이트
for filepath in html_files:
    if os.path.exists(filepath):
        update_html_file(filepath)
    else:
        print(f"  ✗ File not found: {filepath}")

print("\n✅ All admin files updated!")
print("⚠️  Note: You need to manually add renderAdminHeader() and renderAdminFooter() calls to each page's initialization code.")
