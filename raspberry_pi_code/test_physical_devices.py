#!/usr/bin/env python3
"""
물리적 디바이스 테스트 스크립트
- 팬 동작 테스트
- 조도 센서 테스트
- 온습도 센서 테스트
- 실시간 로그 출력
"""
import os
import time
import logging
import threading
from typing import Dict, Any
from dotenv import load_dotenv

from sensors import DHT22Sensor, LightSensor
from actuators import RelayActuator

# 환경변수 로드
load_dotenv('.env')

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('device_test.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

class PhysicalDeviceTester:
    """물리적 디바이스 테스트 클래스"""

    def __init__(self):
        self.device_id = os.getenv('DEVICE_ID', 'raspberry-pi-001')
        self.sensors = {}
        self.actuators = {}
        self.running = False
        self.monitor_thread = None

        logger.info(f"물리적 디바이스 테스터 초기화 (디바이스 ID: {self.device_id})")

    def initialize(self) -> bool:
        """디바이스 초기화"""
        try:
            # 센서 초기화
            if not self._initialize_sensors():
                logger.error("센서 초기화 실패")
                return False

            # 액추에이터 초기화
            if not self._initialize_actuators():
                logger.error("액추에이터 초기화 실패")
                return False

            logger.info("모든 디바이스 초기화 완료")
            return True

        except Exception as e:
            logger.error(f"디바이스 초기화 실패: {e}")
            return False

    def _initialize_sensors(self) -> bool:
        """센서 초기화"""
        try:
            # DHT22 온습도 센서
            dht22_pin = int(os.getenv('GPIO_DHT22', '4'))
            dht22_sensor = DHT22Sensor(dht22_pin)
            if dht22_sensor.initialize():
                self.sensors['dht22'] = dht22_sensor
                logger.info("✅ DHT22 센서 초기화 성공")
            else:
                logger.warning("❌ DHT22 센서 초기화 실패")

            # TSL2561 조도 센서
            i2c_bus = int(os.getenv('I2C_BUS', '1'))
            tsl_address = int(os.getenv('TSL2561_ADDRESS', '0x39'), 16)
            light_sensor = LightSensor(i2c_bus, tsl_address)
            if light_sensor.initialize():
                self.sensors['light'] = light_sensor
                logger.info("✅ 조도 센서 초기화 성공")
            else:
                logger.warning("❌ 조도 센서 초기화 실패")

            return len(self.sensors) > 0

        except Exception as e:
            logger.error(f"센서 초기화 중 오류: {e}")
            return False

    def _initialize_actuators(self) -> bool:
        """액추에이터 초기화"""
        try:
            # 팬 릴레이
            fan_pin = int(os.getenv('GPIO_FAN', '17'))
            fan_actuator = RelayActuator(fan_pin, "fan")
            if fan_actuator.initialize():
                self.actuators['fan'] = fan_actuator
                logger.info("✅ 팬 액추에이터 초기화 성공")
            else:
                logger.warning("❌ 팬 액추에이터 초기화 실패")

            # LED 릴레이
            led_pin = int(os.getenv('GPIO_LED', '18'))
            led_actuator = RelayActuator(led_pin, "led")
            if led_actuator.initialize():
                self.actuators['led'] = led_actuator
                logger.info("✅ LED 액추에이터 초기화 성공")
            else:
                logger.warning("❌ LED 액추에이터 초기화 실패")

            return len(self.actuators) > 0

        except Exception as e:
            logger.error(f"액추에이터 초기화 중 오류: {e}")
            return False

    def test_fan(self, duration: int = 5):
        """팬 동작 테스트"""
        logger.info(f"🔄 팬 동작 테스트 시작 (지속시간: {duration}초)")

        if 'fan' not in self.actuators:
            logger.error("❌ 팬 액추에이터가 초기화되지 않았습니다")
            return False

        try:
            fan = self.actuators['fan']

            # 팬 켜기
            logger.info("🔌 팬을 켭니다...")
            if fan.execute_command('on'):
                logger.info("✅ 팬이 켜졌습니다 - 물리적으로 팬이 돌고 있는지 확인하세요!")

                # 지정된 시간 동안 대기
                time.sleep(duration)

                # 팬 끄기
                logger.info("🔌 팬을 끕니다...")
                if fan.execute_command('off'):
                    logger.info("✅ 팬이 꺼졌습니다")
                    return True
                else:
                    logger.error("❌ 팬 끄기 실패")
                    return False
            else:
                logger.error("❌ 팬 켜기 실패")
                return False

        except Exception as e:
            logger.error(f"팬 테스트 중 오류: {e}")
            return False

    def test_light_sensor(self, duration: int = 10):
        """조도 센서 테스트"""
        logger.info(f"💡 조도 센서 테스트 시작 (지속시간: {duration}초)")

        if 'light' not in self.sensors:
            logger.error("❌ 조도 센서가 초기화되지 않았습니다")
            return False

        try:
            light_sensor = self.sensors['light']
            start_time = time.time()

            logger.info("🔍 조도 센서 데이터를 읽는 중...")
            logger.info("💡 손전등으로 센서에 빛을 비추거나 어두운 곳에 가려서 값 변화를 확인하세요!")

            while time.time() - start_time < duration:
                data = light_sensor.read_data()
                if data and 'light' in data:
                    lux = data['light']['value']
                    logger.info(f"💡 현재 조도: {lux} lux")
                else:
                    logger.warning("⚠️ 조도 데이터 읽기 실패")

                time.sleep(1)

            logger.info("✅ 조도 센서 테스트 완료")
            return True

        except Exception as e:
            logger.error(f"조도 센서 테스트 중 오류: {e}")
            return False

    def start_monitoring(self):
        """실시간 센서 모니터링 시작"""
        logger.info("📊 실시간 센서 모니터링 시작")
        self.running = True
        self.monitor_thread = threading.Thread(target=self._monitor_loop)
        self.monitor_thread.daemon = True
        self.monitor_thread.start()

    def stop_monitoring(self):
        """실시간 센서 모니터링 중지"""
        logger.info("📊 실시간 센서 모니터링 중지")
        self.running = False
        if self.monitor_thread:
            self.monitor_thread.join()

    def _monitor_loop(self):
        """센서 모니터링 루프"""
        logger.info("🔄 센서 모니터링 루프 시작")

        while self.running:
            try:
                sensor_data = self._read_all_sensors()

                if sensor_data:
                    logger.info(f"📊 센서 데이터: {sensor_data}")
                else:
                    logger.warning("⚠️ 센서 데이터 읽기 실패")

                time.sleep(2)

            except Exception as e:
                logger.error(f"모니터링 루프 중 오류: {e}")
                time.sleep(5)

    def _read_all_sensors(self) -> dict:
        data = {}

        data.update(self._read_dht22())
        data.update(self._read_light())

        return data

    def _read_dht22(self) -> dict:
        result = {}
        dht_sensor = self.sensors.get('dht22')
        if not dht_sensor:
            return result

        dht_data = dht_sensor.read_data()
        if not dht_data:
            return result

        temp = dht_data.get('temperature', {}).get('value')
        hum = dht_data.get('humidity', {}).get('value')

        if temp is not None:
            result['temperature'] = f"{temp}°C"
        if hum is not None:
            result['humidity'] = f"{hum}%"

        return result

    def _read_light(self) -> dict:
        result = {}
        light_sensor = self.sensors.get('light')
        if not light_sensor:
            return result

        light_data = light_sensor.read_data()
        lux = light_data.get('light', {}).get('value') if light_data else None

        if lux is not None:
            result['light'] = f"{lux} lux"

        return result


    def cleanup(self):
        """정리 작업"""
        try:
            self.stop_monitoring()

            # 모든 액추에이터 끄기
            for name, actuator in self.actuators.items():
                try:
                    actuator.execute_command('off')
                    logger.info(f"🔌 {name} 끄기 완료")
                except SystemExit:
                    raise  # ✅ 시스템 종료 예외는 다시 발생시킴
                except Exception as e:
                    logger.warning(f"{name} 끄기 중 예외 발생: {e}")

            # 센서 정리
            for sensor in self.sensors.values():
                if hasattr(sensor, 'cleanup'):
                    sensor.cleanup()

            logger.info("🧹 디바이스 테스터 정리 완료")

        except Exception as e:
            logger.error(f"정리 작업 중 오류: {e}")

def main():
    """메인 테스트 함수"""
    tester = PhysicalDeviceTester()

    try:
        # 디바이스 초기화
        if not tester.initialize():
            logger.error("❌ 디바이스 초기화 실패")
            return

        print("\n" + "="*50)
        print("🔧 물리적 디바이스 테스트 시작")
        print("="*50)

        # 1. 팬 테스트
        print("\n1️⃣ 팬 동작 테스트")
        print("-" * 30)
        tester.test_fan(duration=3)

        # 2. 조도 센서 테스트
        print("\n2️⃣ 조도 센서 테스트")
        print("-" * 30)
        tester.test_light_sensor(duration=5)

        # 3. 실시간 모니터링
        print("\n3️⃣ 실시간 센서 모니터링")
        print("-" * 30)
        print("📊 센서 데이터를 실시간으로 모니터링합니다...")
        print("🛑 Ctrl+C를 눌러서 종료하세요")

        tester.start_monitoring()

        # 무한 대기
        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            print("\n🛑 사용자에 의해 중단됨")

    except Exception as e:
        logger.error(f"테스트 중 오류: {e}")

    finally:
        tester.cleanup()

if __name__ == "__main__":
    main()
