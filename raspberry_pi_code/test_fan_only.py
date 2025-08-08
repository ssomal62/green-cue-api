#!/usr/bin/env python3
"""
팬 동작 테스트 전용 스크립트
"""
import os
import time
import logging
from dotenv import load_dotenv
from actuators import RelayActuator

# 환경변수 로드
load_dotenv('.env')

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger(__name__)

def test_fan():
    """팬 동작 테스트"""
    try:
        # 팬 GPIO 핀 설정
        fan_pin = int(os.getenv('GPIO_FAN', '17'))
        
        print(f"🔧 팬 테스트 시작 (GPIO 핀: {fan_pin})")
        print("=" * 40)
        
        # 팬 액추에이터 초기화
        fan = RelayActuator(fan_pin, "fan")
        if not fan.initialize():
            print("❌ 팬 초기화 실패")
            return False
        
        print("✅ 팬 초기화 성공")
        
        # 팬 켜기
        print("\n🔌 팬을 켭니다...")
        if fan.execute_command('on'):
            print("✅ 팬이 켜졌습니다!")
            print("🔍 물리적으로 팬이 돌고 있는지 확인하세요!")
            
            # 5초 동안 켜두기
            for i in range(5, 0, -1):
                print(f"⏰ {i}초 후 팬이 꺼집니다...")
                time.sleep(1)
            
            # 팬 끄기
            print("\n🔌 팬을 끕니다...")
            if fan.execute_command('off'):
                print("✅ 팬이 꺼졌습니다")
                return True
            else:
                print("❌ 팬 끄기 실패")
                return False
        else:
            print("❌ 팬 켜기 실패")
            return False
            
    except Exception as e:
        print(f"❌ 팬 테스트 중 오류: {e}")
        return False

if __name__ == "__main__":
    test_fan()
