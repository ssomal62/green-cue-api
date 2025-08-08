#!/usr/bin/env python3
"""
스마트팜 메인 실행 스크립트
라즈베리파이에서 실제로 실행됩니다.
"""
import os
import sys
import signal
import logging
import time
from dotenv import load_dotenv

# 환경변수 로드
load_dotenv('.env')

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('smart_farm.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

def signal_handler(signum, frame):
    """시그널 핸들러 (Ctrl+C 등)"""
    logger.info(f"시그널 {signum} 수신. 시스템을 종료합니다.")
    if hasattr(signal_handler, 'controller'):
        signal_handler.controller.stop()
    sys.exit(0)

def main():
    """메인 함수"""
    try:
        logger.info("=== 스마트팜 시스템 시작 ===")
        
        # 시그널 핸들러 등록
        signal.signal(signal.SIGINT, signal_handler)
        signal.signal(signal.SIGTERM, signal_handler)
        
        # 스마트팜 컨트롤러 임포트
        try:
            from smart_farm_controller import SmartFarmController
            logger.info("✓ 스마트팜 컨트롤러 모듈 로드 성공")
        except ImportError as e:
            logger.error(f"스마트팜 컨트롤러 모듈 로드 실패: {e}")
            return 1
        
        # 컨트롤러 생성 및 초기화
        controller = SmartFarmController()
        signal_handler.controller = controller  # 시그널 핸들러에서 접근 가능하도록
        
        logger.info("스마트팜 시스템 초기화 중...")
        if not controller.initialize():
            logger.error("스마트팜 시스템 초기화 실패")
            return 1
        
        logger.info("✓ 스마트팜 시스템 초기화 완료")
        logger.info("스마트팜 시스템을 시작합니다...")
        
        # 시스템 시작
        controller.start()
        
        # 메인 루프
        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            logger.info("사용자에 의해 중단됨 (Ctrl+C)")
        except Exception as e:
            logger.error(f"메인 루프에서 오류 발생: {e}")
        finally:
            logger.info("스마트팜 시스템을 종료합니다...")
            controller.stop()
            logger.info("스마트팜 시스템 종료 완료")
        
        return 0
        
    except Exception as e:
        logger.error(f"스마트팜 시스템 실행 중 오류 발생: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())
