#!/usr/bin/env python3
"""
센서 테스트 전용 스크립트
- 온습도 센서
- 조도 센서
"""
import os
import time
import logging
from dotenv import load_dotenv
from sensors import DHT22Sensor, LightSensor

# 환경변수 로드
load_dotenv('.env')

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger(__name__)

def test_sensors():
    """센서 테스트"""
    try:
        print("🔧 센서 테스트 시작")
        print("=" * 40)

        sensors = {}

        # DHT22 온습도 센서 초기화
        print("\n🌡️ DHT22 온습도 센서 초기화...")
        dht22_pin = int(os.getenv('GPIO_DHT22', '4'))
        dht22_sensor = DHT22Sensor(dht22_pin)
        if dht22_sensor.initialize():
            sensors['dht22'] = dht22_sensor
            print("✅ DHT22 센서 초기화 성공")
        else:
            print("❌ DHT22 센서 초기화 실패")

        # TSL2561 조도 센서 초기화
        print("\n💡 TSL2561 조도 센서 초기화...")
        i2c_bus = int(os.getenv('I2C_BUS', '1'))
        tsl_address = int(os.getenv('TSL2561_ADDRESS', '0x39'), 16)
        light_sensor = LightSensor(i2c_bus, tsl_address)
        if light_sensor.initialize():
            sensors['light'] = light_sensor
            print("✅ 조도 센서 초기화 성공")
        else:
            print("❌ 조도 센서 초기화 실패")

        if not sensors:
            print("❌ 초기화된 센서가 없습니다")
            return False

        # 센서 데이터 읽기 테스트
        print("\n📊 센서 데이터 읽기 테스트 (10초간)")
        print("💡 조도 센서에 손전등을 비추거나 어두운 곳에 가려서 값 변화를 확인하세요!")
        print("-" * 40)

        start_time = time.time()
        while time.time() - start_time < 10:
            sensor_data = {}

            # 온습도 데이터 읽기
            if 'dht22' in sensors:
                dht_data = sensors['dht22'].read_data()
                if dht_data:
                    if 'temperature' in dht_data:
                        temp = dht_data['temperature']['value']
                        sensor_data['temperature'] = f"{temp}°C"
                    if 'humidity' in dht_data:
                        hum = dht_data['humidity']['value']
                        sensor_data['humidity'] = f"{hum}%"

            # 조도 데이터 읽기
            if 'light' in sensors:
                light_data = sensors['light'].read_data()
                if light_data and 'light' in light_data:
                    lux = light_data['light']['value']
                    sensor_data['light'] = f"{lux} lux"

            # 데이터 출력
            if sensor_data:
                print(f"📊 {sensor_data}")
            else:
                print("⚠️ 센서 데이터 읽기 실패")

            time.sleep(1)

        print("\n✅ 센서 테스트 완료")
        return True

    except Exception as e:
        print(f"❌ 센서 테스트 중 오류: {e}")
        return False

if __name__ == "__main__":
    test_sensors()
