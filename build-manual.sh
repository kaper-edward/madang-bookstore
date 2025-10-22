#!/bin/bash
# 수동 WAR 빌드 스크립트 (Maven 없이)
# Servlet 기반 프로젝트용

set -e  # 오류 발생 시 중단

echo "========================================="
echo "  마당 서점 WAR 빌드 스크립트 (수동)"
echo "========================================="
echo ""

# 1. 디렉토리 생성
echo "[1/5] 빌드 디렉토리 생성..."
rm -rf target
mkdir -p target/classes
mkdir -p target/madang/WEB-INF/classes
mkdir -p target/madang/WEB-INF/lib

# 2. Java 소스 컴파일
echo "[2/5] Java 소스 컴파일..."

# Jakarta Servlet API JAR 다운로드 (필요 시)
SERVLET_JAR="target/jakarta.servlet-api-6.1.0.jar"
if [ ! -f "$SERVLET_JAR" ]; then
    echo "  → Jakarta Servlet API 6.1 다운로드 중..."
    wget -q -O "$SERVLET_JAR" "https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.1.0/jakarta.servlet-api-6.1.0.jar"
fi

# Gson JAR 다운로드 (필요 시)
GSON_JAR="target/gson-2.10.1.jar"
if [ ! -f "$GSON_JAR" ]; then
    echo "  → Gson 다운로드 중..."
    wget -q -O "$GSON_JAR" "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
fi

# 컴파일 (모든 Java 파일)
echo "  → 컴파일 중..."
find src/main/java -name "*.java" > sources.txt
javac -d target/classes \
    -cp "$SERVLET_JAR:$GSON_JAR:lib/*" \
    -encoding UTF-8 \
    -source 21 -target 21 \
    @sources.txt

rm sources.txt
echo "  ✓ 컴파일 완료"

# 3. 클래스 파일 복사
echo "[3/5] 클래스 파일 복사..."
cp -r target/classes/* target/madang/WEB-INF/classes/

# 4. 라이브러리 복사
echo "[4/5] 라이브러리 복사..."
cp lib/*.jar target/madang/WEB-INF/lib/
cp "$GSON_JAR" target/madang/WEB-INF/lib/

# 5. webapp 파일 복사
echo "[5/5] 웹 리소스 복사..."
cp -r src/main/webapp/* target/madang/

# 6. WAR 파일 생성
echo ""
echo "WAR 파일 생성 중..."
cd target/madang
jar cvf ../madang.war * > /dev/null 2>&1
cd ../..

echo ""
echo "========================================="
echo "  ✓ 빌드 완료!"
echo "========================================="
echo ""
echo "WAR 파일: target/madang.war"
echo "크기: $(du -h target/madang.war | cut -f1)"
echo ""
echo "다음 단계:"
echo "  1. Tomcat 설치 (docs/tomcat-deployment-guide.md 참조)"
echo "  2. WAR 배포: cp target/madang.war \$CATALINA_HOME/webapps/"
echo "  3. Tomcat 시작: \$CATALINA_HOME/bin/startup.sh"
echo "  4. 접속: http://localhost:8080/madang/"
echo ""
