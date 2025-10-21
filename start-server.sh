#!/bin/bash

# 마당 서점 서버 시작 스크립트
# .env 파일에서 환경변수를 로드하고 서버를 실행합니다

# .env 파일이 있으면 로드
if [ -f .env ]; then
    echo "📋 Loading environment variables from .env..."
    export $(cat .env | grep -v '^#' | xargs)
else
    echo "⚠️  .env file not found. Using default values from DBConnection.java"
fi

# 서버 시작
echo "🚀 Starting Madang Server..."
java -cp "bin:lib/*" com.madang.server.MadangServer
